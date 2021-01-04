package co.aurasphere.revolver.test;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import co.aurasphere.revolver.Revolver;
import co.aurasphere.revolver.test.constructor.CollectionConstructorComponent;
import co.aurasphere.revolver.test.constructor.InjectConstructorComponent;
import co.aurasphere.revolver.test.constructor.MultipleArgsConstructorComponent;
import co.aurasphere.revolver.test.constructor.NoPublicConstructorComponent;

public class ConstructorInjectionTest {

	@Inject
	public InjectConstructorComponent component;

	@Inject
	public NoPublicConstructorComponent noPublicComponent;

	@Inject
	public CollectionConstructorComponent collectionComponent;
	
	@Inject
	public MultipleArgsConstructorComponent multipleArgsComponent;

	@Before
	public void setup() {
		Revolver.inject(this);
	}

	@Test
	public void testDefaultConstructorComponent() {
		Assert.assertNotNull(component);
		Assert.assertNotNull(component.getComponent());
	}

	@Test
	public void testNoPublicConstructorComponent() {
		Assert.assertNotNull(noPublicComponent);
		Assert.assertNotNull(noPublicComponent.getComponent());
	}

	@Test
	public void testCollectionInjection() {
		Assert.assertNotNull(collectionComponent);
		Assert.assertNotNull(collectionComponent.getCollection());
		Assert.assertEquals(3, collectionComponent.getCollection().size());
	}
	
	@Test
	public void testMultipleArgsInjection() {
		Assert.assertNotNull(multipleArgsComponent);
		Assert.assertNotNull(multipleArgsComponent.getComponentOne());
		Assert.assertNotNull(multipleArgsComponent.getComponentTwo());
	}

}