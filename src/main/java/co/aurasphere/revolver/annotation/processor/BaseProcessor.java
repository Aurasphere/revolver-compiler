package co.aurasphere.revolver.annotation.processor;

import javax.annotation.processing.AbstractProcessor;

import co.aurasphere.revolver.environment.Messenger;
import co.aurasphere.revolver.environment.RevolverCompilationEnvironment;
import co.aurasphere.revolver.registry.RevolverRegistry;

public abstract class BaseProcessor extends AbstractProcessor {

	protected static final RevolverRegistry registry = RevolverRegistry
			.getInstance();

	protected static final RevolverCompilationEnvironment environment = RevolverCompilationEnvironment
			.getInstance();
	
	protected static final Messenger messenger(){
		return environment.getMessenger();
	}

}