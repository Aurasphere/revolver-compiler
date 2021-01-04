package co.aurasphere.revolver.test.constructor;

import javax.inject.Inject;
import javax.inject.Singleton;

import co.aurasphere.revolver.test.basic.EmptyComponent;

@Singleton
public class InjectConstructorComponent {

	private EmptyComponent component;
	
	public InjectConstructorComponent() {}
	
	@Inject
	public InjectConstructorComponent(EmptyComponent component) {
		this.component = component;
	}

	public EmptyComponent getComponent() {
		return component;
	}
	
}