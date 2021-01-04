package co.aurasphere.revolver.test;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import co.aurasphere.revolver.Revolver;
import co.aurasphere.revolver.test.method.SingletonMethodComponent;

public class MethodInjectionTest {

	@Inject
	public String constantString;

	@Inject
	public Integer constantInt;
	
	@Inject
	public SingletonMethodComponent singletonComponentOne;
	
	@Inject
	public SingletonMethodComponent singletonComponentTwo;
	
	@Inject
	public Long constantStatic;
	
	@Inject
	public Character constantFinal;
	
	@Before
	public void setup() {
		Revolver.inject(this);
	}
	
	@Test
	public void testStringInjection() {
		Assert.assertNotNull(constantString);
		Assert.assertEquals("A", constantString);
	}
	
	@Test
	public void testIntInjection() {
		Assert.assertNotNull(constantInt);
		Assert.assertEquals((Integer) 5, constantInt);
	}
	
	@Test
	public void testSingletonInjection() {
		Assert.assertNotNull(singletonComponentOne);
		Assert.assertNotNull(singletonComponentTwo);
		Assert.assertSame(singletonComponentOne, singletonComponentTwo);
	}
	
	@Test
	public void testStaticInjection() {
		Assert.assertNotNull(constantStatic);
		Assert.assertEquals((Long) 10L, constantStatic);
	}
	
	@Test
	public void testFinalInjection() {
		Assert.assertNotNull(constantFinal);
		Assert.assertEquals((Character) 'b', constantFinal);
	}
}