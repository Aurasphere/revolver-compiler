package co.aurasphere.revolver.registry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import co.aurasphere.revolver.annotation.Generator;

public class ClassInfo extends RevolverRegistryEntry {

	private TypeElement typeElement;

	private boolean singleton;

	private boolean generator;

	private ExecutableElement constructor;

	private List<FieldInfo> constructorParams;

	private List<ClassInfo> fieldsToInject;

	public ClassInfo(TypeElement typeElement, ExecutableElement constructor) {
		this(typeElement);
		this.constructor = constructor;
		this.constructorParams = new ArrayList<FieldInfo>();
		for (VariableElement e : constructor.getParameters()) {
			this.constructorParams.add(new FieldInfo(e, typeElement));
			// Adds the constructor params to the fields to inject for latter
			// validation (circular dependencies).
			this.fieldsToInject.add(new ClassInfo(
					variableElementToTypeElement(e)));
		}
	}

	public ClassInfo(TypeElement typeElement) {
		super(typeElement);
		this.typeElement = typeElement;
		this.singleton = isAnnotatedWith(typeElement, Singleton.class);
		this.generator = isAnnotatedWith(typeElement, Generator.class);
	}

	@Override
	public TypeElement getType() {
		return this.typeElement;
	}

	public List<FieldInfo> getConstructorParameters() {
		return this.constructorParams;
	}

	public boolean isSingleton() {
		return this.singleton;
	}

	public boolean isGenerator() {
		return this.generator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((constructorParams == null) ? 0 : constructorParams
						.hashCode());
		result = prime * result + (generator ? 1231 : 1237);
		result = prime * result + (singleton ? 1231 : 1237);
		result = prime * result
				+ ((typeElement == null) ? 0 : typeElement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassInfo other = (ClassInfo) obj;
		if (constructorParams == null) {
			if (other.constructorParams != null)
				return false;
		} else if (!constructorParams.equals(other.constructorParams))
			return false;
		if (generator != other.generator)
			return false;
		if (singleton != other.singleton)
			return false;
		if (typeElement == null) {
			if (other.typeElement != null)
				return false;
		} else if (!typeElement.equals(other.typeElement))
			return false;
		return true;
	}

	/**
	 * Lazily initialized to prevent circular dependencies.
	 * 
	 * @return
	 */
	public List<ClassInfo> getFieldsToInject() {
		if (this.fieldsToInject == null) {
			this.fieldsToInject = new ArrayList<ClassInfo>();
			if (typeElement != null) {
				List<? extends Element> members = typeElement
						.getEnclosedElements();
				List<VariableElement> fields = ElementFilter.fieldsIn(members);

				// Adds the constructor params to the fields to inject for
				// latter
				// validation (circular dependencies).
				if (this.constructor != null) {
					for (VariableElement e : this.constructor.getParameters()) {
						this.fieldsToInject.add(new ClassInfo(
								variableElementToTypeElement(e)));
					}
				}
				// Adds the field to inject for latter validation (circular
				// dependencies).
				for (VariableElement f : fields) {
					if (f.getAnnotation(Inject.class) != null) {
						this.fieldsToInject.add(new ClassInfo(
								variableElementToTypeElement(f)));
					}
				}
			}
		}
		return this.fieldsToInject;
	}

}
