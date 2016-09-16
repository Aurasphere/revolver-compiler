package co.aurasphere.revolver.environment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class RevolverCompilationEnvironment {

	private Set<Class<?>> registeredProcessors;

	private List<ProcessingEnvironment> processingEnvironments;

	private Elements elementUtils;

	private Types typeUtils;

	private CollectionElementUtils collectionElementUtils;

	private static RevolverCompilationEnvironment instance;

	private RevolverCompilationEnvironment() {
		this.processingEnvironments = new ArrayList<ProcessingEnvironment>();
		this.registeredProcessors = new HashSet<Class<?>>();
	}

	public static RevolverCompilationEnvironment getInstance() {
		if (instance == null) {
			instance = new RevolverCompilationEnvironment();
		}
		return instance;
	}

	public void register(ProcessingEnvironment processingEnv, Class<?> klass) {
		this.registeredProcessors.add(klass);
		this.processingEnvironments.add(processingEnv);

		if (this.elementUtils == null) {
			this.elementUtils = processingEnv.getElementUtils();
		}

		if (this.typeUtils == null) {
			this.typeUtils = processingEnv.getTypeUtils();
		}

	}

	public void terminateRound(ProcessingEnvironment processingEnv) {
		processingEnvironments.remove(processingEnv);
	}

	public Messenger getMessenger() {
		for (ProcessingEnvironment env : processingEnvironments) {
			if (env != null) {
				return new Messenger(env);
			}
		}
		return null;
	}

	public Elements getElementUtils() {
		return this.elementUtils;
	}

	public Types getTypeUtils() {
		return this.typeUtils;
	}

	public CollectionElementUtils getCollectionElementUtils() {
		if (this.collectionElementUtils == null) {
			this.collectionElementUtils = new CollectionElementUtils(
					elementUtils, typeUtils);
		}
		return this.collectionElementUtils;
	}

	/**
	 * Starts the validation as soon as each processor ended.Injector validation
	 * must be done before component provider.
	 */
	public void terminate(Class<?> klass) {
		registeredProcessors.remove(klass);
		if (registeredProcessors.isEmpty()) {
			InjectorValidator.validate();
			ComponentProviderValidator.validate();
		}
	}

}
