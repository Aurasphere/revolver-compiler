package co.aurasphere.revolver.environment;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import co.aurasphere.revolver.registry.CollectionType;
import co.aurasphere.revolver.registry.FieldInfo;

public class CollectionElementUtils {

	private TypeMirror SET_TYPE;

	private TypeMirror LIST_TYPE;

	private TypeMirror QUEUE_TYPE;

	private Types typeUtils;

	private Elements elementUtils;
	
	CollectionElementUtils(Elements elementUtils, Types typeUtils){
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;

		this.SET_TYPE = this.elementUtils.getTypeElement("java.util.Set")
				.asType();
		this.LIST_TYPE = elementUtils.getTypeElement("java.util.List")
				.asType();
		this.QUEUE_TYPE = elementUtils.getTypeElement("java.util.Queue")
				.asType();
	}
	
	public CollectionType getCollectionType(FieldInfo field) {

		TypeMirror fieldTypeMirror = field.getTypeMirror();

		// Checks the erasure to leverage generic parameterized collections.
		TypeMirror erasure = this.typeUtils.erasure(fieldTypeMirror);
		if (fieldTypeMirror.getKind().equals(TypeKind.ARRAY)) {
			return CollectionType.ARRAY;
		}
		if (this.typeUtils.isAssignable(erasure, this.SET_TYPE)) {
			return CollectionType.SET;
		}
		if (this.typeUtils.isAssignable(erasure, this.LIST_TYPE)) {
			return CollectionType.LIST;
		}
		if (this.typeUtils.isAssignable(erasure, this.QUEUE_TYPE)) {
			return CollectionType.QUEUE;
		}
		return CollectionType.SIMPLE;
	}

}
