package co.aurasphere.revolver.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import co.aurasphere.revolver.registry.ClassInfo;

public class CircularDependenciesValidator extends RevolverBaseBean {

	private static HashMap<String, ClassInfo> traversedClasses;

	private static HashMap<String, ClassInfo> validClasses;

	static boolean validate() {
		Set<ClassInfo> classesToCheck = registry.getClassesToInject();
		traversedClasses = new HashMap<String, ClassInfo>();
		validClasses = new HashMap<String, ClassInfo>();
		for (ClassInfo info : classesToCheck) {
			if(checkClassesRecursively(info) == false){
				return false;
			}
		}
		return true;
	}

	private static boolean checkClassesRecursively(ClassInfo classToCheck) {
		List<ClassInfo> childElements = classToCheck.getFieldsToInject();

		String className = classToCheck.getType().toString();

		// If the class is in the validClasses map, then I've already checked
		// it.
		if (validClasses.containsKey(className)) {
			return true;
		}

		// If the class is already on the checkedMap, a circular dependency has
		// been found. Throws an error.
		if (traversedClasses.containsKey(className)) {
			messenger().error(ErrorMessageBundle.CIRCULAR_DEPENDENCY_E,
					className, traversedClasses.get(className).getType());
			return false;
		}

		// Adds the class to the checked classes map.
		traversedClasses.put(className, classToCheck);

		// If there are no children, then the class is valid.
		if (childElements.isEmpty()) {
			validClasses.put(className, classToCheck);
			return true;
		}

		// Checks each child. If any child is invalid then the class is invalid.
		// The error has already been thrown so I just return false.
		for (ClassInfo info : childElements) {
			if (checkClassesRecursively(info) == false) {
				return false;
			}
		}
		// If I've not returned yet, then the class is valid. I remove it from
		// the traversedClasses since I'm going to check a different branch and
		// puts the class in the valid list.
		traversedClasses.remove(className);
		validClasses.put(className, classToCheck);
		return true;
	}

}
