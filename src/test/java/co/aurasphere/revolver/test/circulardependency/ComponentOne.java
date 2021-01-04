package co.aurasphere.revolver.test.circulardependency;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ComponentOne {

	@Inject
	private ComponentTwo componentTwo;
	
	@Inject
	private ComponentThree componentThree;

	public ComponentTwo getComponentTwo() {
		return componentTwo;
	}

	public void setComponentTwo(ComponentTwo componentTwo) {
		this.componentTwo = componentTwo;
	}

	public ComponentThree getComponentThree() {
		return componentThree;
	}

	public void setComponentThree(ComponentThree componentThree) {
		this.componentThree = componentThree;
	}
}