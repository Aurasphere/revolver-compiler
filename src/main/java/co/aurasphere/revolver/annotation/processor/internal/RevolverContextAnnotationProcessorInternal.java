package co.aurasphere.revolver.annotation.processor.internal;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import co.aurasphere.revolver.annotation.DefaultConstructor;
import co.aurasphere.revolver.environment.ErrorMessageBundle;

public class RevolverContextAnnotationProcessorInternal extends
		BaseComponentProviderAnnotationProcessorInternal {

	public void processInternal(Set<? extends Element> elements) {
		for (Element e : elements) {
			// There are only classes.
			processClass((TypeElement) e);
		}
	}

	private void processClass(TypeElement klass) {
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

}
