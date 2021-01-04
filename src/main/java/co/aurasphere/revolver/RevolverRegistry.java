package co.aurasphere.revolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.model.BaseRevolverRegistryEntry;
import co.aurasphere.revolver.model.ClassRegistryEntry;
import co.aurasphere.revolver.model.CollectionType;
import co.aurasphere.revolver.model.FieldRegistryEntry;
import co.aurasphere.revolver.model.MethodRegistryEntry;

public class RevolverRegistry {

	private HashMap<ClassRegistryEntry, List<FieldRegistryEntry>> injectionMap = new HashMap<ClassRegistryEntry, List<FieldRegistryEntry>>();
	private List<ClassRegistryEntry> managedClasses = new ArrayList<ClassRegistryEntry>();
	private List<MethodRegistryEntry> managedMethods = new ArrayList<MethodRegistryEntry>();

	public void addManagedClass(TypeElement managedClass) {
		this.managedClasses.add(new ClassRegistryEntry(managedClass));
	}

	public void addFieldToInject(VariableElement fieldToInject) {
		FieldRegistryEntry fieldInfo = new FieldRegistryEntry(fieldToInject);
		ClassRegistryEntry parentClass = new ClassRegistryEntry((TypeElement) fieldToInject.getEnclosingElement());

		// Checks that a setter exists if the field is not public.
		if (!fieldInfo.isPublic()) {
			// Looks for a public setter with exactly one argument of type to
			// set and java bean convention name (e.g. String name -> public
			// setName(String))
			boolean validSetterExists = ElementFilter.methodsIn(parentClass.getType().getEnclosedElements())
					.parallelStream()
					.anyMatch(m -> m.getSimpleName().toString().equals(fieldInfo.setterName())
							&& m.getModifiers().contains(Modifier.PUBLIC) && m.getParameters().size() == 1
							&& RevolverCompilationEnvironment.INSTANCE.getTypeUtils()
									.isAssignable(m.getParameters().get(0).asType(), fieldInfo.getTypeMirror()));
			if (!validSetterExists) {
				// If I haven't found the setter I throw an error.
				RevolverCompilationEnvironment.INSTANCE
						.printMessage(
								Kind.ERROR, "No setter found for field. Expected signature: [public "
										+ fieldInfo.setterName() + "(" + fieldInfo.getType().asType() + ")]",
								fieldInfo.getElement());
			}
		}

		// Adds the field to the injection map.
		List<FieldRegistryEntry> fieldsList = this.injectionMap.get(parentClass);
		if (fieldsList == null) {
			fieldsList = new ArrayList<FieldRegistryEntry>();
			this.injectionMap.put(parentClass, fieldsList);
		}
		fieldsList.add(fieldInfo);
	}

	public void addManagedMethod(ExecutableElement element, Element returnType) {
		this.managedMethods.add(new MethodRegistryEntry(element, returnType));
	}

	public List<MethodRegistryEntry> getManagedMethods() {
		return this.managedMethods;
	}

	public Set<ClassRegistryEntry> getClassesToInject() {
		return injectionMap.keySet();
	}

	public Collection<BaseRevolverRegistryEntry<?>> getManagedElementsList() {
		return Stream.concat(this.managedClasses.parallelStream(), this.managedMethods.parallelStream())
				.collect(Collectors.toList());
	}

	public List<FieldRegistryEntry> getDeduplicatedCollectionsToInject() {
		return injectionMap.values().parallelStream().flatMap(l -> l.stream())
				.filter(e -> e.getCollectionType() != CollectionType.SINGLE).distinct().collect(Collectors.toList());
	}

	public HashMap<ClassRegistryEntry, List<FieldRegistryEntry>> getInjectionMap() {
		return injectionMap;
	}

	public List<FieldRegistryEntry> getRequiredFields() {
		Stream<FieldRegistryEntry> methodArgsToInject = this.managedMethods.stream()
				.flatMap(m -> m.getParametersFieldInfo().stream());
		Stream<FieldRegistryEntry> fieldsToInject = this.injectionMap.values().stream()
				.flatMap(f -> f.stream());
		Stream<FieldRegistryEntry> constructorArgsToInject = this.managedClasses.stream()
				.flatMap(c -> c.getConstructorParameters().stream());
		return Stream.concat(methodArgsToInject, Stream.concat(fieldsToInject, constructorArgsToInject))
				.filter(f -> f.getCollectionType() == CollectionType.SINGLE).collect(Collectors.toList());
	}

	public List<ClassRegistryEntry> getManagedClasses() {
		return managedClasses;
	}

}