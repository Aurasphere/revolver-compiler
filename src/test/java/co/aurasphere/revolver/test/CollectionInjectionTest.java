package co.aurasphere.revolver.test;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import co.aurasphere.revolver.Revolver;
import co.aurasphere.revolver.test.collections.CollectionTestInterface;

public class CollectionInjectionTest {
	
	public static final int EXPECTED_COLLECTION_SIZE = 3;
	
	@Inject
	public CollectionTestInterface[] elementsAsArray;
	
	@Inject
	public List<CollectionTestInterface> elementsAsList;
	
	@Inject
	public Set<CollectionTestInterface> elementsAsSet;
	
	@Inject
	public Queue<CollectionTestInterface> elementsAsQueue;
	
	@Before
	public void setup() {
		Revolver.inject(this);
	}
	
	@Test
	public void testArrayInjection() {
		Assert.assertNotNull(elementsAsArray);
		Assert.assertEquals(EXPECTED_COLLECTION_SIZE, elementsAsArray.length);
	}
	
	@Test
	public void testListInjection() {
		Assert.assertNotNull(elementsAsList);
		Assert.assertEquals(EXPECTED_COLLECTION_SIZE, elementsAsList.size());
	}
	
	@Test
	public void testSetInjection() {
		Assert.assertNotNull(elementsAsSet);
		Assert.assertEquals(EXPECTED_COLLECTION_SIZE, elementsAsSet.size());
	}
	
	@Test
	public void testQueueInjection() {
		Assert.assertNotNull(elementsAsQueue);
		Assert.assertEquals(EXPECTED_COLLECTION_SIZE, elementsAsQueue.size());
	}
	
}