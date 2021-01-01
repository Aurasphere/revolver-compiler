package co.aurasphere.revolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import co.aurasphere.revolver.model.BaseRevolverRegistryEntry;
import co.aurasphere.revolver.model.ClassRegistryEntry;
import co.aurasphere.revolver.model.CollectionType;
import co.aurasphere.revolver.model.FieldRegistryEntry;
import co.aurasphere.revolver.model.MethodRegistryEntry;

public class RevolverRegistry {

	// Fields needed for injection and used for validation. TODO: shouldn't be
	// classes?
	private List<FieldRegistryEntry> requiredFields = new ArrayList<FieldRegistryEntry>();

	// Maps with getterName, returnType.
	private HashMap<ClassRegistryEntry, List<FieldRegistryEntry>> injectionMap = new HashMap<ClassRegistryEntry, List<FieldRegistryEntry>>();
	private HashMap<String, ClassRegistryEntry> managedClasses = new HashMap<String, ClassRegistryEntry>();
	private List<MethodRegistryEntry> managedMethods = new ArrayList<MethodRegistryEntry>();

	public void addManagedClass(TypeElement managedClass) {
		ClassRegistryEntry info = new ClassRegistryEntry(managedClass);
		this.managedClasses.put(info.getterMethodName(), info);

		// Registers the constructor arguments as required for validation.
		for (FieldRegistryEntry f : info.getConstructorParameters()) {
			addRequiredField(f);
		}
	}

	public void addFieldToInject(VariableElement fieldToInject, CollectionType collectionType) {
		FieldRegistryEntry fieldInfo = new FieldRegistryEntry(fieldToInject);
		ClassRegistryEntry parentClass = new ClassRegistryEntry((TypeElement) fieldToInject.getEnclosingElement());

		List<FieldRegistryEntry> fieldsList = this.injectionMap.get(parentClass);
		if (fieldsList == null) {
			fieldsList = new ArrayList<FieldRegistryEntry>();
			this.injectionMap.put(parentClass, fieldsList);
		}
		fieldsList.add(fieldInfo);
		fieldInfo.setCollectionType(collectionType);

		// Requires the field for validation if it's not a collection.
		if (collectionType == CollectionType.SINGLE) {
			addRequiredField(fieldInfo);
		}
	}

	public void addRequiredField(FieldRegistryEntry field) {
		this.requiredFields.add(field);
	}

	/**
	 * Adds a generator method under Revolver context. The method is registered by
	 * adding the return type to the managed elements list. The method arguments are
	 * added to the required field list in order to validate them later.
	 * 
	 * @param element    the method to add.
	 * @param returnType the element returned by the method.
	 */
	public void addManagedMethod(ExecutableElement element, Element returnType) {
		MethodRegistryEntry methodInfo = new MethodRegistryEntry(element, returnType);
		this.managedMethods.add(methodInfo);

		// Registers the method arguments as required for validation.
		for (FieldRegistryEntry f : methodInfo.getParametersFieldInfo()) {
			addRequiredField(f);
		}
	}

	public List<MethodRegistryEntry> getManagedMethods() {
		return this.managedMethods;
	}

	public Set<ClassRegistryEntry> getClassesToInject() {
		return injectionMap.keySet();
	}

	public Collection<BaseRevolverRegistryEntry> getManagedElementsList() {
		return Stream.concat(this.managedClasses.values().stream(), this.managedMethods.stream())
				.collect(Collectors.toList());
	}

	public List<FieldRegistryEntry> getDeduplicatedCollectionsToInject() {
		return injectionMap.values().parallelStream().flatMap(l -> l.parallelStream())
				.filter(e -> e.getCollectionType() != CollectionType.SINGLE)
				.distinct()
				.collect(Collectors.toList());
	}

	public HashMap<ClassRegistryEntry, List<FieldRegistryEntry>> getInjectionMap() {
		return injectionMap;
	}

	public List<FieldRegistryEntry> getRequiredFields() {
		return requiredFields;
	}

	public Collection<ClassRegistryEntry> getManagedClasses() {
		return managedClasses.values();
	}

}