package co.aurasphere.revolver.test.circulardependency;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ComponentTwo {

	@Inject
	private ComponentThree componentThree;
	
	public ComponentThree getComponentThree() {
		return this.componentThree;
	}

	public void setComponentThree(ComponentThree componentThree) {
		this.componentThree = componentThree;
	}
}