package co.aurasphere.revolver.model;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.apache.commons.lang.StringUtils;

import co.aurasphere.revolver.RevolverCompilationEnvironment;

public class FieldRegistryEntry extends BaseRevolverRegistryEntry<VariableElement> {

	private CollectionType collectionType;
	
	public FieldRegistryEntry(VariableElement variableElement) {
		super(variableElement);
		
		Types typeUtils = RevolverCompilationEnvironment.INSTANCE.getTypeUtils();
		Elements elementUtils = RevolverCompilationEnvironment.INSTANCE.getElementUtils();
		// Checks the erasure to leverage generic parameterized collections.
		TypeMirror erasure = typeUtils.erasure(this.getTypeMirror());
		if (this.getTypeMirror().getKind().equals(TypeKind.ARRAY)) {
			this.collectionType = CollectionType.ARRAY;
		} else if (typeUtils.isAssignable(erasure, elementUtils.getTypeElement("java.util.Set").asType())) {
			this.collectionType = CollectionType.SET;
		} else if (typeUtils.isAssignable(erasure, elementUtils.getTypeElement("java.util.List").asType())) {
			this.collectionType = CollectionType.LIST;
		} else if (typeUtils.isAssignable(erasure, elementUtils.getTypeElement("java.util.Queue").asType())) {
			this.collectionType = CollectionType.QUEUE;
		} else {
			this.collectionType = CollectionType.SINGLE;
		}
	}

	@Override
	public VariableElement getType() {
		return this.element;
	}

	public boolean isPublic() {
		return this.element.getModifiers().contains(Modifier.PUBLIC);
	}

	public boolean isPrimitive() {
		return this.element.asType().getKind().isPrimitive();
	}

	public String setterName() {
		return "set" + StringUtils.capitalize(this.element.toString());
	}

	public TypeMirror getCollectionElementTypeMirror() {
		if (this.getTypeMirror() instanceof ArrayType) {
			return ((ArrayType) this.getTypeMirror()).getComponentType();
		} else if (this.getTypeMirror() instanceof DeclaredType) {
			List<? extends TypeMirror> typeArguments = ((DeclaredType) this.getTypeMirror()).getTypeArguments();
			if (!typeArguments.isEmpty()) {
				return typeArguments.get(0);
			}
		}
		return null;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((element == null) ? 0 : element.hashCode());
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
		FieldRegistryEntry other = (FieldRegistryEntry) obj;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		return true;
	}

	@Override
	public ExecutableElement getCreatorExecutableElement() {
		return null;
	}

	public CollectionType getCollectionType() {
		return collectionType;
	}

}