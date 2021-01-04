package co.aurasphere.revolver.test.breaking;

import javax.inject.Inject;
import javax.inject.Singleton;

import co.aurasphere.revolver.test.basic.EmptyComponent;
import co.aurasphere.revolver.test.collections.CollectionTestInterface;

public class BasicInjectionBreakingTest {

	@Singleton
	public abstract class AbstractClassComponent {
	}

	@Singleton
	public enum EnumComponent {
	}

	@Singleton
	private class PrivateClassComponent {
	}

	@Singleton
	public static class NoSetterComponent {

		@Inject
		private EmptyComponent privateField;

		@Inject
		protected EmptyComponent protectedField;

		@Inject
		EmptyComponent friendlyField;

	}

	@Singleton
	public static class PrivateConstructor {
		private PrivateConstructor() {
		}
	}

	@Singleton
	public static class PrivateConstructorInject {
		@Inject
		private PrivateConstructorInject() {
		}
	}

	@Singleton
	public static class VarargsConstructorComponent {
		public VarargsConstructorComponent(CollectionTestInterface... varargs) {
		}
	}

	@Singleton
	public static class ArrayConstructorComponent {
		public ArrayConstructorComponent(CollectionTestInterface[] array) {
		}
	}

	@Singleton
	public static class PrimitiveConstructorComponent {
		public PrimitiveConstructorComponent(int arg) {
		}
	}

}