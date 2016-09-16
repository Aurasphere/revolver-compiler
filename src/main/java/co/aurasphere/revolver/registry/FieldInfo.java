package co.aurasphere.revolver.registry;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import co.aurasphere.revolver.utils.StringUtils;

public class FieldInfo extends RevolverRegistryEntry {

	private TypeElement parentClass;

	private VariableElement variableElement;

	private boolean publicAttr;

	private boolean string;

	private boolean primitive;

	private CollectionType collectionType;

	private TypeMirror collectionElementTypeMirror;

	public FieldInfo(VariableElement variableElement, TypeElement parentClass) {
		super(variableElement);
		this.variableElement = variableElement;
		this.parentClass = parentClass;
		this.publicAttr = variableElement.getModifiers().contains(
				Modifier.PUBLIC);

		// If the field is primitive or a String I treat it as
		// it was annotated with @Named (if it isnt't already).
		if (!this.named) {
			if ("java.lang.String".equals(this.typeMirror.toString())) {
				this.name = this.variableElement.getSimpleName().toString();
				this.string = true;
			} else if (this.typeMirror.getKind().isPrimitive()) {
				this.name = this.variableElement.getSimpleName().toString();
				this.primitive = true;
			}
		}

		this.collectionType = CollectionType.SIMPLE;
		this.typeMirror = this.variableElement.asType();

		// Sets the element type mirror if this is a collection on an array.
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

	public FieldInfo(VariableElement e) {
		this(e, (TypeElement) e.getEnclosingElement());
	}

	public FieldInfo(VariableElement e, CollectionType collectionType) {
		this(e);
		this.collectionType = collectionType;
	}

	public TypeElement getParentClass() {
		return this.parentClass;
	}

	@Override
	public VariableElement getType() {
		return this.variableElement;
	}

	public boolean isPublicAttr() {
		return this.publicAttr;
	}

	public String setterName() {
		return "set"
				+ StringUtils.firstCharUppercase(this.variableElement
						.toString());
	}

	public ClassInfo getParentClassInfo() {
		return new ClassInfo(this.parentClass);
	}

	public String qualifiedName() {
		return this.parentClass.toString() + "." + variableElement.toString();
	}

	public String setterSignature() {
		return "public " + setterName() + "("
				+ variableElement.asType().toString() + ")";
	}

	public CollectionType getCollectionType() {
		return this.collectionType;
	}

	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;
	}

	public TypeMirror getCollectionElementTypeMirror() {
		return this.collectionElementTypeMirror;
	}

	public boolean isString() {
		return string;
	}

	public boolean isPrimitive() {
		return primitive;
	}

}
