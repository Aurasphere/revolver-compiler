package co.aurasphere.revolver.test.breaking;

import javax.inject.Singleton;

import co.aurasphere.revolver.test.basic.EmptyComponent;

@Singleton
public class MethodInjectionBreakingTest {
	
	@Singleton
	private EmptyComponent returnsAComponentPrivate() {
		return new EmptyComponent();
	}
	
	@Singleton
	protected EmptyComponent returnsAComponentProtected() {
		return new EmptyComponent();
	}
	
	@Singleton
	EmptyComponent returnsAComponentFriendly() {
		return new EmptyComponent();
	}
	
	@Singleton
	public void returnsAComponentVoid() {
	}
	
	public class BreakingComponent {
		
		@Singleton
		public EmptyComponent returnsAComponentWithoutClassAnnotation() {
			return new EmptyComponent();
		}
	}

}