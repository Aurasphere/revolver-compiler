package co.aurasphere.revolver.test.circulardependency;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ComponentThree {

	@Inject
	private ComponentOne componentOne;
	
	public ComponentOne getComponentOne() {
		return this.componentOne;
	}

	public void setComponentOne(ComponentOne componentOne) {
		this.componentOne = componentOne;
	}
}