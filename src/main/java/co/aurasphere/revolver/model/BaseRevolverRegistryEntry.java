package co.aurasphere.revolver.model;

import java.lang.annotation.Annotation;

import javax.inject.Named;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.apache.commons.lang.StringUtils;

import co.aurasphere.revolver.RevolverCompilationEnvironment;

public abstract class BaseRevolverRegistryEntry {

	protected String name;

	protected boolean named;

	protected Element element;

	protected TypeMirror typeMirror;

	public BaseRevolverRegistryEntry(Element element) {
		this.named = isAnnotatedWith(element, Named.class);
		this.element = element;
		this.name = element == null ? null : getElementName(element);
		this.typeMirror = element == null ? null : element.asType();
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

	protected boolean isAnnotatedWith(Element element, Class<? extends Annotation> annotation) {
		return element != null && element.getAnnotation(annotation) != null;
	}

	public String getName() {
		return this.name;
	}

	public String getterMethodName() {
		return "get" + StringUtils.capitalize(name);
	}

	public boolean isNamed() {
		return this.named;
	}

	public abstract Element getType();

	public Element getElement() {
		return element;
	}

	public TypeMirror getTypeMirror() {
		return typeMirror;
	}

	protected TypeElement variableElementToTypeElement(VariableElement element) {
		return (TypeElement) RevolverCompilationEnvironment.INSTANCE.getTypeUtils().asElement(element.asType());
	}

}
