package co.aurasphere.revolver.test.method;

import javax.inject.Singleton;

@Singleton
public class MethodComponent {
	
	@Singleton
	public String returnsAComponentString() {
		return "A";
	}
	
	@Singleton
	public Integer returnsAComponentInt() {
		return 5;
	}
	
	@Singleton
	public SingletonMethodComponent returnsAComponentSingleton() {
		return new SingletonMethodComponent();
	}
	
	@Singleton
	public static Long returnsAComponentStatic() {
		return 10L;
	}
	
	@Singleton
	public final Character returnsAComponentFinal() {
		return 'b';
	}

}