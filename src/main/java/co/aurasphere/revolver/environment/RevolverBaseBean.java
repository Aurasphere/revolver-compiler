package co.aurasphere.revolver.environment;

import co.aurasphere.revolver.registry.RevolverRegistry;

public class RevolverBaseBean {

	protected static final RevolverRegistry registry = RevolverRegistry
			.getInstance();

	protected static final RevolverCompilationEnvironment environment = RevolverCompilationEnvironment
			.getInstance();
	
	protected static final Messenger messenger(){
		return environment.getMessenger();
	}

}
