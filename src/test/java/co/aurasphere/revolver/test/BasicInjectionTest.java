package co.aurasphere.revolver.test;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import co.aurasphere.revolver.Revolver;
import co.aurasphere.revolver.test.basic.EmptyComponent;
import co.aurasphere.revolver.test.basic.NestedComponent;

public class BasicInjectionTest {

	@Inject
	public EmptyComponent component;

	@Inject
	public NestedComponent nested;
	
	@Inject
	public StaticNestedComponent staticNested;
	
	@Singleton
	public static class StaticNestedComponent{
		@Inject
		public NestedComponent nested;
	}
	
	@Before
	public void setup() {
		Revolver.inject(this);
	}
	
	@Test
	public void testComponentInjection() {
		Assert.assertNotNull(component);
	}
	
	@Test
	public void testNestedComponent() {
		Assert.assertNotNull(nested);
		Assert.assertSame(component, nested.getComponent());
	}
	
	@Test
	public void testStaticNestedComponent() {
		Assert.assertNotNull(staticNested);
		Assert.assertSame(nested, staticNested.nested);
	}
}