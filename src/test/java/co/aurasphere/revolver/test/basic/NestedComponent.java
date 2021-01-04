package co.aurasphere.revolver.test.basic;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NestedComponent {

	@Inject
	private EmptyComponent component;
	
	public EmptyComponent getComponent() {
		return this.component;
	}
	
	// Needed to work.
	public void setComponent(EmptyComponent component) {
		this.component = component;
	}
}