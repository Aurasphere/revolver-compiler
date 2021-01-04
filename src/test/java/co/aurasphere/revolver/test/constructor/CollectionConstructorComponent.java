package co.aurasphere.revolver.test.constructor;

import java.util.List;

import javax.inject.Singleton;

import co.aurasphere.revolver.test.collections.CollectionTestInterface;

@Singleton
public class CollectionConstructorComponent {

	private List<CollectionTestInterface> collection;

	public CollectionConstructorComponent(List<CollectionTestInterface> collection) {
		this.collection = collection;
	}

	public List<CollectionTestInterface> getCollection() {
		return collection;
	}

	public void setCollection(List<CollectionTestInterface> collection) {
		this.collection = collection;
	}
	}