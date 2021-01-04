package co.aurasphere.revolver.model;

import javax.inject.Named;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang.StringUtils;

import co.aurasphere.revolver.RevolverCompilationEnvironment;

public abstract class BaseRevolverRegistryEntry<T extends Element> {

	protected String name;

	// Actual element under context. For methods, it's the return type.
	protected T element;

	protected TypeMirror typeMirror;

	public BaseRevolverRegistryEntry(T element) {
		this.element = element;
		this.name = element == null ? null : getElementName(element);
		this.typeMirror = element == null ? null : element.asType();

		// This happens if the type is a primitive, which is not supported. In this case
		// you can use the Object counterpart (e.g. int -> Integer)
		if (this.element != null && this.typeMirror == null) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"Primitive types component are not supported. Use the wrapper counterpart (e.g. int -> Integer)",
					element);
		}
	}

	private String getElementName(Element e) {
		// If a named annotation is not found, then the component provider getter will
		// be for the class name.
		Named named = e.getAnnotation(Named.class);
		if (named == null) {
			return e.asType().toString().replaceAll("[\\.<>\\[\\]\\(\\)]", "_");
		} else {
			// If an empty named annotation is found, then the getter will
			// look for the variable name.
			if (named.value() == null || named.value().isEmpty()) {
				return e.getSimpleName().toString();
			} else {
				// If the named annotation has a value, the getter will look
				// for that value.
				return named.value();
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public String getterMethodName() {
		return "get" + StringUtils.capitalize(name);
	}

	public boolean isNamed() {
		return this.element != null && this.element.getAnnotation(Named.class) != null;
	}

	public abstract Element getType();

	public T getElement() {
		return element;
	}

	public TypeMirror getTypeMirror() {
		return typeMirror;
	}
	
	public abstract ExecutableElement getCreatorExecutableElement();

}