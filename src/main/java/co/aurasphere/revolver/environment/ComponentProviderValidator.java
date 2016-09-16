package co.aurasphere.revolver.environment;

import java.util.HashMap;
import java.util.List;

import javax.lang.model.type.TypeMirror;

import co.aurasphere.revolver.registry.CollectionType;
import co.aurasphere.revolver.registry.FieldInfo;
import co.aurasphere.revolver.registry.RevolverRegistryEntry;

public class ComponentProviderValidator extends RevolverBaseBean{

	static boolean validate() {
		// Gets the registry entry to validate.
		List<FieldInfo> requiredFields = registry.getRequiredFields();
		HashMap<String, RevolverRegistryEntry> managedElements = registry
				.getManagedElements();

		// Checks if the getters for the required fields exists.
		if (requiredFields != null) {
			for (FieldInfo f : requiredFields) {
				
				// No need to validate collections.
				if (f.getCollectionType().equals(CollectionType.SIMPLE)) {
					RevolverRegistryEntry entry = managedElements.get(f
							.getterMethodName());
					// If the required field is missing, prints an error.
					String requiredFieldName = f.getName();
					String parentClassName = f.getParentClass().toString();
					if (entry == null) {
						messenger()
								.error(ErrorMessageBundle.REQUIRED_COMPONENT_NOT_FOUND_E,
										requiredFieldName, parentClassName);
						return false;
					}

					// If the return types mismatch, prints an error.
					TypeMirror requiredFieldType = f.getTypeMirror();
					TypeMirror managedElementReturnType = entry.getTypeMirror();
					if (!managedElementReturnType.equals(requiredFieldType)) {
						messenger()
								.error(ErrorMessageBundle.REQUIRED_COMPONENT_TYPE_MISMATCH_E,
										requiredFieldName, parentClassName,
										requiredFieldType,
										managedElementReturnType,
										entry.getType().getEnclosingElement());
						return false;
					}
				}
			}
		}
		return true;
	}

}
