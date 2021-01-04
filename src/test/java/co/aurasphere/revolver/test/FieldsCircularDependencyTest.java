package co.aurasphere.revolver.test;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import co.aurasphere.revolver.Revolver;
import co.aurasphere.revolver.test.circulardependency.ComponentOne;
import co.aurasphere.revolver.test.circulardependency.SelfInjectingComponent;

public class FieldsCircularDependencyTest {

	@Inject
	public ComponentOne componentOne;
	
	@Inject
	public SelfInjectingComponent selfInjectingComponent;
	
	@Before
	public void setup() {
		Revolver.inject(this);
	}
	
	@Test
	public void checkInjection() {
		Assert.assertNotNull(componentOne);
		Assert.assertNotNull(componentOne.getComponentTwo());
		Assert.assertNotNull(componentOne.getComponentThree());
		Assert.assertNotNull(componentOne.getComponentTwo().getComponentThree());
		Assert.assertNotNull(componentOne.getComponentTwo().getComponentThree().getComponentOne());
		Assert.assertSame(componentOne, componentOne.getComponentTwo().getComponentThree().getComponentOne());
		Assert.assertSame(componentOne.getComponentThree(), componentOne.getComponentTwo().getComponentThree());
	}
	
	@Test
	public void testSelfInjection() {
		Assert.assertNotNull(selfInjectingComponent.self);
		Assert.assertSame(selfInjectingComponent, selfInjectingComponent.self);
	}
}