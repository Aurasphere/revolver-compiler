package co.aurasphere.revolver;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.model.BaseRevolverRegistryEntry;
import co.aurasphere.revolver.model.ClassRegistryEntry;
import co.aurasphere.revolver.model.CollectionType;
import co.aurasphere.revolver.model.FieldRegistryEntry;

public class Validator {

	// Checks that all the classes requested for injection are available.
	public boolean validateState() {
		// Gets the registry entry to validate.
		List<FieldRegistryEntry> requiredFields = RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getRequiredFields();
		Collection<BaseRevolverRegistryEntry<?>> managedElements = RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getManagedElementsList();

		// Checks if the getters for the required fields exists.
		for (FieldRegistryEntry f : requiredFields) {
			// If the component is named, a strict match is required. Otherwise, any
			// assignable class is good enough.
			Optional<BaseRevolverRegistryEntry<?>> injectionCandidate = null;
			if (f.isNamed()) {
				injectionCandidate = managedElements.parallelStream()
						.filter(e -> f.getterMethodName().equals(e.getterMethodName())).findFirst();
			} else {
				injectionCandidate = managedElements.parallelStream()
						.filter(e -> RevolverCompilationEnvironment.INSTANCE.getTypeUtils()
								.isAssignable(e.getTypeMirror(), f.getTypeMirror()))
						.findFirst();
			}

			// If the required field is missing, prints an error.
			if (!injectionCandidate.isPresent()) {
				RevolverCompilationEnvironment.INSTANCE
						.printMessage(Kind.ERROR,
								"Required component [" + f.getName() + "] for [" + f.getElement().getEnclosingElement()
										+ "] not found under Revolver context. Are you missing an annotation?",
								f.getElement());
				return false;
			}

			// Finds circular dependencies.
			BaseRevolverRegistryEntry<?> registryEntry = injectionCandidate.get();
			try {
				findCircularDependencyRecursively(registryEntry);
			} catch (IllegalArgumentException e) {
				RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR, "Circular dependency detected",
						registryEntry.getElement());
			}

			// If the return types mismatch, prints an error.
			TypeMirror requiredFieldType = f.getTypeMirror();
			TypeMirror managedElementReturnType = injectionCandidate.get().getTypeMirror();
			if (!managedElementReturnType.equals(requiredFieldType)) {
				RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
						"Mismatching type for component. Expected [" + requiredFieldType + "] but was ["
								+ managedElementReturnType + "] from class ["
								+ injectionCandidate.get().getType().getEnclosingElement() + "].",
						f.getElement());
				return false;
			}
		}
		return true;
	}

	private void findCircularDependencyRecursively(BaseRevolverRegistryEntry<?> e) throws IllegalArgumentException {
		findCircularDependencyRecursively(e, new HashSet<>());
	}

	// Very simple implementation. This validation needs to be performed at the end
	// in order to check the collection injection.
	private void findCircularDependencyRecursively(BaseRevolverRegistryEntry<?> e, Set<TypeElement> dependencies)
			throws IllegalArgumentException {
		ExecutableElement creatorElement = e.getCreatorExecutableElement();

		// Nothing to check.
		if (creatorElement == null)
			return;

		for (VariableElement parameter : creatorElement.getParameters()) {
			FieldRegistryEntry fre = new FieldRegistryEntry(parameter);
			TypeMirror typeMirror;
			if (fre.getCollectionType() == CollectionType.SINGLE) {
				typeMirror = parameter.asType();
			} else {
				typeMirror = fre.getCollectionElementTypeMirror();
			}
			TypeElement dependency = (TypeElement) RevolverCompilationEnvironment.INSTANCE.getTypeUtils()
					.asElement(typeMirror);

			// Nothing to check if the element is an interface.
			if (dependency.getKind().isInterface()) {
				return;
			}

			if (dependencies.contains(dependency)) {
				throw new IllegalArgumentException("Circular dependency detected");
			}
			dependencies.add(dependency);
			findCircularDependencyRecursively(new ClassRegistryEntry(dependency));
		}
	}
}