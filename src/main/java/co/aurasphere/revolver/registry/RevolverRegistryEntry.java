package co.aurasphere.revolver.registry;

import java.lang.annotation.Annotation;

import javax.inject.Named;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import co.aurasphere.revolver.utils.StringUtils;

public abstract class RevolverRegistryEntry {

	protected String name;

	protected boolean named;

	protected Element element;

	protected TypeMirror typeMirror;
	
	public RevolverRegistryEntry(Element element) {
		this.named = isAnnotatedWith(element, Named.class);
		this.name = getElementName(element);
		this.element = element;
		this.typeMirror = element.asType();
	}

	protected String getElementName(Element e) {
		// If a named annotation is not found, then the getter will look for
		// the class name.
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

	protected boolean isAnnotatedWith(Element element,
			Class<? extends Annotation> annotation) {
		return element.getAnnotation(annotation) != null;
	}

	public String getName() {
		return this.name;
	}

	public String getterMethodName() {
		return "get" + StringUtils.firstCharUppercase(name);
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

}
