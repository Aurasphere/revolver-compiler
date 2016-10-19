package co.aurasphere.revolver.annotation.processor.internal;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import co.aurasphere.revolver.annotation.DefaultConstructor;
import co.aurasphere.revolver.environment.ErrorMessageBundle;
import co.aurasphere.revolver.environment.RevolverBaseBean;
import co.aurasphere.revolver.registry.FieldInfo;
import co.aurasphere.revolver.registry.RevolverRegistry;

public abstract class BaseRevolverAnnotationProvessorInternal extends RevolverBaseBean {

	protected void addClassToContext(TypeElement klass) {
		if (isClassValid(klass)) {
			ExecutableElement constructor = getDefaultConstructor(klass);
			if (constructor != null) {
				registry.addManagedClass(klass,
						constructor);
			}
		}
	}

	/**
	 * Method that checks if a class is not an enum or an interface and that is
	 * not abstract. If all these conditions are met then the class is valid,
	 * else an exception is thrown.
	 * 
	 * @param className
	 *            the class fully qualified name.
	 */
	private boolean isClassValid(TypeElement klass) {
		ElementKind kind = klass.getKind();
		Set<Modifier> modifiers = klass.getModifiers();
		if (kind == ElementKind.ENUM) {
			messenger().error(ErrorMessageBundle.INVALID_CLASS_ENUM_E, klass);
			return false;
		}
		if (kind == ElementKind.INTERFACE) {
			messenger().error(ErrorMessageBundle.INVALID_CLASS_INTERFACE_E,
					klass);
			return false;
		}
		if (modifiers.contains(Modifier.ABSTRACT)) {
			messenger().error(ErrorMessageBundle.INVALID_CLASS_ABSTRACT_E,
					klass);
			return false;
		}
		if (!modifiers.contains(Modifier.PUBLIC)) {
			messenger().error(ErrorMessageBundle.INVALID_CLASS_NOT_PUBLIC_E,
					klass);
			return false;
		}
		return true;
	}

	// TODO: RevolverDefaultConstructor on private constructor should probably
	// throw exception. Multiple annotation should do the same. Not sure, seems
	// kinda optimal like this though.
	private ExecutableElement getDefaultConstructor(TypeElement klass) {
		ExecutableElement defaultConstructor = null;

		// Gets all members of this class.
		List<? extends Element> allMembers = klass.getEnclosedElements();
		// Gets the constructors for the current class.
		List<ExecutableElement> constructors = ElementFilter
				.constructorsIn(allMembers);

		// Finds the default constructor.
		for (ExecutableElement e : constructors) {
			// Checks only public constructors.
			if (e.getModifiers().contains(Modifier.PUBLIC)) {
				// The @DefaultConstructor has priority. Stop checking
				// if one is found.
				if (e.getAnnotation(DefaultConstructor.class) != null) {
					defaultConstructor = e;
					break;
				}
				// Next in priority order there's a constructor without
				// parameters.
				if (e.getParameters().isEmpty()) {
					defaultConstructor = e;
				}
				// Lastly, if it haven't found anything else, I get the first
				// constructor I find.
				if (defaultConstructor == null) {
					defaultConstructor = e;
				}
			}
		}
		// If there's no public constructor, an exception is thrown.
		if (defaultConstructor == null) {
			messenger()
					.error(ErrorMessageBundle.NO_PUBLIC_CONSTRUCTOR_E, klass);
			return null;
		}
		if (defaultConstructor.isVarArgs()) {
			// TODO: future release? inject all implementation known, same for
			// lists and array
			messenger().error(ErrorMessageBundle.VARARGS_NOT_SUPPORTED, klass);
			return null;
		}
		return defaultConstructor;
	}
	

	/**
	 * Adds a constant to Revolver context.
	 * 
	 * @param e
	 * @param hardValidation
	 */
	protected void addFieldToContext(VariableElement e, boolean hardValidation) {
		if (isFieldValid(e, hardValidation)) {
			RevolverRegistry.getInstance().addManagedConstant(e);
		}
	}

	private boolean isFieldValid(VariableElement e, boolean throwException) {
		Set<Modifier> modifier = e.getModifiers();
		String fullFieldName = new FieldInfo(e).qualifiedName();

		// Fields must be final.
		if (!modifier.contains(Modifier.FINAL)) {
			if (throwException) {
				messenger().error(ErrorMessageBundle.INVALID_FIELD_NOT_FINAL_E,
						fullFieldName);
			}
			return false;
		}
		// Fields must be primitive or String. e.getConstantValue() will return
		// null if this condition is not met.
		Object fieldValue = e.getConstantValue();
		if (fieldValue == null) {
			if (throwException) {
				messenger().error(
						ErrorMessageBundle.INVALID_CONSTANT_NOT_PRIMITIVE_E,
						fullFieldName);
			}
			return false;
		}
		return true;
	}
	
	
}
