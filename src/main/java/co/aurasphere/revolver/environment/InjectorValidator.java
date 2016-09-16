package co.aurasphere.revolver.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

import co.aurasphere.revolver.registry.ClassInfo;
import co.aurasphere.revolver.registry.FieldInfo;

public class InjectorValidator extends RevolverBaseBean {

	static boolean validate() {
		// Gets the registry entry to validate.
		Set<ClassInfo> classesToInject = registry.getClassesToInject();
		HashMap<ClassInfo, List<FieldInfo>> injectionMap = registry
				.getInjectionMap();

		// For each field to inject, if it's not public, I check if the
		// constructor exists.
		List<FieldInfo> fields;
		for (ClassInfo c : classesToInject) {
			fields = injectionMap.get(c);
			for (FieldInfo f : fields) {
				if (!f.isPublicAttr()) {
					checkSetterExist(c, f);
				}
			}
		}
		return true;
	}

	/**
	 * Checks if a setter called by the injector exists, in case the field to
	 * inject is private.
	 * 
	 * @return
	 */
	private static boolean checkSetterExist(ClassInfo klass, FieldInfo variable) {
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
					messenger().error(
							ErrorMessageBundle.SETTER_AND_FIELD_NOT_PUBLIC_E,
							methodName, variable.getType(), klass.getType());
					return false;
				}
				methodFound = m;
				break;
			}
		}
		// If I haven't found the setter I throw an error.
		if (methodFound == null) {
			messenger().error(ErrorMessageBundle.SETTER_NOT_FOUND_E,
					variable.qualifiedName(), variable.setterSignature());
			return false;
		}

		// Checks the arguments.
		return checkSettersArgumentsNumberAndTypes(methodFound, variable);
	}

	/**
	 * Checks that the number and type of arguments of the setters matches. Same
	 * for provider constructors.
	 */
	private static boolean checkSettersArgumentsNumberAndTypes(
			ExecutableElement method, FieldInfo field) {
		// TODO at the moment the setters must have only one argument and the
		// type must match. For the future would like to inject other components
		// as well.
		TypeMirror fieldType = field.getTypeMirror();
		Types typeUtils = environment.getTypeUtils();
		List<? extends VariableElement> args = method.getParameters();
		if (args.size() != 1 || !typeUtils.isAssignable(args.get(0).asType(), fieldType)) {
			messenger().error(ErrorMessageBundle.SETTER_ARGS_MISMATCH_E,
					field.qualifiedName(), fieldType);
			return false;
		}

		// If the argument matches, I register it as needed for the
		// ComponentProviderValidator.
		registry.addRequiredField(field);

		return true;
	}

}
