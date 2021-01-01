package co.aurasphere.revolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.model.BaseRevolverRegistryEntry;
import co.aurasphere.revolver.model.ClassRegistryEntry;
import co.aurasphere.revolver.model.CollectionType;
import co.aurasphere.revolver.model.FieldRegistryEntry;

public class Validator {

	public boolean validateInjector(Types typeUtils) {
		// Gets the registry entry to validate.
		Set<ClassRegistryEntry> classesToInject = RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getClassesToInject();
		HashMap<ClassRegistryEntry, List<FieldRegistryEntry>> injectionMap = RevolverCompilationEnvironment.INSTANCE
				.getRegistry().getInjectionMap();

		// For each field to inject, if it's not public, I check if the
		// constructor exists.
		List<FieldRegistryEntry> fields;
		for (ClassRegistryEntry c : classesToInject) {
			fields = injectionMap.get(c);
			for (FieldRegistryEntry f : fields) {
				if (!f.isPublic()) {
					checkSetterExist(c, f, typeUtils);
				}
			}
		}
		return true;
	}

	/**
	 * Checks if a setter called by the injector exists, in case the field to inject
	 * is private.
	 * 
	 * @return
	 */
	private boolean checkSetterExist(ClassRegistryEntry klass, FieldRegistryEntry variable, Types typeUtils) {
		List<? extends Element> elements = klass.getType().getEnclosedElements();
		List<ExecutableElement> methods = ElementFilter.methodsIn(elements);

		ExecutableElement methodFound = null;
		String methodName = null;
		for (ExecutableElement m : methods) {
			// Looks for a method with the expected setter name.
			methodName = m.getSimpleName().toString();
			if (methodName.equals(variable.setterName())) {
				// TODO: e se ci sono più setters con firme diverse?
				// The setter must be public.
				if (!m.getModifiers().contains(Modifier.PUBLIC)) {
					RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
							"Setter [" + methodName + "] or field [" + variable.getType() + "] in class ["
									+ klass.getType() + "] must be public.");
					return false;
				}
				methodFound = m;
				break;
			}
		}
		// If I haven't found the setter I throw an error.
		if (methodFound == null) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR, "No setter found for field ["
					+ variable.qualifiedName() + "]. Expected signature: [" + variable.setterSignature() + "]");
			return false;
		}

		// Checks the arguments.
		return checkSettersArgumentsNumberAndTypes(methodFound, variable, typeUtils);
	}

	/**
	 * Checks that the number and type of arguments of the setters matches. Same for
	 * provider constructors. Only one argument is allowed as per JavaBeans
	 * convention.
	 */
	private boolean checkSettersArgumentsNumberAndTypes(ExecutableElement method, FieldRegistryEntry field,
			Types typeUtils) {
		TypeMirror fieldType = field.getTypeMirror();
		List<? extends VariableElement> args = method.getParameters();
		if (args.size() != 1 || !typeUtils.isAssignable(args.get(0).asType(), fieldType)) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"Setter arguments doesn't match for field [" + field.qualifiedName()
							+ "]. Expected only 1 argument with type [" + fieldType + "].");
			return false;
		}

		// If the argument matches, I register it as needed for the
		// ComponentProviderValidator.
		RevolverCompilationEnvironment.INSTANCE.getRegistry().addRequiredField(field);

		return true;
	}

	// Checks that all the classes requested for injection are available.
	public boolean validateComponentProvider() {
		// Gets the registry entry to validate.
		List<FieldRegistryEntry> requiredFields = RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getRequiredFields();
		Collection<BaseRevolverRegistryEntry> managedElements = RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getManagedElementsList();

		// Checks if the getters for the required fields exists.
		if (requiredFields != null) {
			for (FieldRegistryEntry f : requiredFields) {

				// No need to validate collections, they can be empty.
				if (f.getCollectionType() == CollectionType.SINGLE) {
					// TODO: here you can also count how many occurrencies
					Optional<BaseRevolverRegistryEntry> injectableFields = managedElements.parallelStream()
							.filter(e -> f.getterMethodName().equals(e.getterMethodName())).findFirst();

					// If the required field is missing, prints an error.
					String requiredFieldName = f.getName();
					String parentClassName = f.getParentClass().toString();
					if (!injectableFields.isPresent()) {
						RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
								"Required component [" + requiredFieldName + "] for class [" + parentClassName
										+ "] not found under Revolver context. Are you missing an annotation?");
						return false;
					}

					// If the return types mismatch, prints an error.
					TypeMirror requiredFieldType = f.getTypeMirror();
					TypeMirror managedElementReturnType = injectableFields.get().getTypeMirror();
					if (!managedElementReturnType.equals(requiredFieldType)) {
						RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
								"Mismatching type for component [" + requiredFieldName + "] in class ["
										+ parentClassName + "]: expected [" + requiredFieldType + "] but got ["
										+ managedElementReturnType + "] from class ["
										+ injectableFields.get().getType().getEnclosingElement() + "].");
						return false;
					}
				}
			}
		}
		return true;
	}
}