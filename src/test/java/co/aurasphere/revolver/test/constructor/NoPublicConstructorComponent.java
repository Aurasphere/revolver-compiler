package co.aurasphere.revolver.test.constructor;

import javax.inject.Singleton;

import co.aurasphere.revolver.test.basic.EmptyComponent;

@Singleton
public class NoPublicConstructorComponent {

	private EmptyComponent component;
	
	public NoPublicConstructorComponent(EmptyComponent component) {
		this.component = component;
	}

	public EmptyComponent getComponent() {
		return component;
	}
	
}