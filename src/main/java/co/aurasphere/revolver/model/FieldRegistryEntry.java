package co.aurasphere.revolver.model;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.apache.commons.lang.StringUtils;

public class FieldRegistryEntry extends BaseRevolverRegistryEntry {

	private TypeElement parentClass;

	private VariableElement variableElement;

	private CollectionType collectionType;

	private TypeMirror collectionElementTypeMirror;

	public FieldRegistryEntry(VariableElement variableElement, TypeElement parentClass) {
		super(variableElement);
		this.variableElement = variableElement;
		this.parentClass = parentClass;
		this.collectionType = CollectionType.SINGLE;

		// Sets the element type mirror if this is a collection or an array.
		if (this.typeMirror instanceof ArrayType) {
			this.collectionElementTypeMirror = ((ArrayType) this.typeMirror)
					.getComponentType();
		} else if (this.typeMirror instanceof DeclaredType) {
			List<? extends TypeMirror> typeArguments = ((DeclaredType) this.typeMirror)
					.getTypeArguments();
			if (!typeArguments.isEmpty()) {
				this.collectionElementTypeMirror = typeArguments.get(0);
			}
		}
	}

	public FieldRegistryEntry(VariableElement e) {
		this(e, (TypeElement) e.getEnclosingElement());
	}

	public TypeElement getParentClass() {
		return this.parentClass;
	}

	@Override
	public VariableElement getType() {
		return this.variableElement;
	}

	public boolean isPublic() {
		return variableElement.getModifiers().contains(
				Modifier.PUBLIC);
	}

	public String setterName() {
		return "set"
				+ StringUtils.capitalize(this.variableElement
						.toString());
	}

	public String qualifiedName() {
		return this.parentClass.toString() + "." + variableElement.toString();
	}

	public String setterSignature() {
		return "public " + setterName() + "("
				+ variableElement.asType().toString() + ")";
	}

	public TypeMirror getCollectionElementTypeMirror() {
		return this.collectionElementTypeMirror;
	}

	public CollectionType getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collectionElementTypeMirror == null) ? 0 : collectionElementTypeMirror.hashCode());
		result = prime * result + ((variableElement == null) ? 0 : variableElement.hashCode());
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
		if (collectionElementTypeMirror == null) {
			if (other.collectionElementTypeMirror != null)
				return false;
		} else if (!collectionElementTypeMirror.equals(other.collectionElementTypeMirror))
			return false;
		if (variableElement == null) {
			if (other.variableElement != null)
				return false;
		} else if (!variableElement.equals(other.variableElement))
			return false;
		return true;
	}

}