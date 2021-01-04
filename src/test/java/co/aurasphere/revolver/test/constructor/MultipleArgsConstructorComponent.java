package co.aurasphere.revolver.test.constructor;

import javax.inject.Singleton;

import co.aurasphere.revolver.test.basic.EmptyComponent;
import co.aurasphere.revolver.test.basic.NestedComponent;

@Singleton
public class MultipleArgsConstructorComponent {

	private EmptyComponent componentOne;
	private NestedComponent componentTwo;

	public MultipleArgsConstructorComponent(EmptyComponent componentOne, NestedComponent componentTwo) {
		this.componentOne = componentOne;
		this.componentTwo = componentTwo;
	}

	public EmptyComponent getComponentOne() {
		return componentOne;
	}

	public void setComponentOne(EmptyComponent componentOne) {
		this.componentOne = componentOne;
	}

	public NestedComponent getComponentTwo() {
		return componentTwo;
	}

	public void setComponentTwo(NestedComponent componentTwo) {
		this.componentTwo = componentTwo;
	}

}