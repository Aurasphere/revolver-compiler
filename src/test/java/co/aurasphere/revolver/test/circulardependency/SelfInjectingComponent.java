package co.aurasphere.revolver.test.circulardependency;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SelfInjectingComponent {

	@Inject
	public SelfInjectingComponent self;
}