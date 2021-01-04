package co.aurasphere.revolver.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.squareup.javapoet.ClassName;

public enum CollectionType {
	SINGLE(null), ARRAY(null), SET(HashSet.class), LIST(ArrayList.class), QUEUE(LinkedList.class);

	private ClassName collectionClass;

	private CollectionType(Class<?> defaultImplementationClass) {
		this.collectionClass = defaultImplementationClass == null ? null : ClassName.get(defaultImplementationClass);
	}

	public ClassName getCollectionClass() {
		return collectionClass;
	}
}