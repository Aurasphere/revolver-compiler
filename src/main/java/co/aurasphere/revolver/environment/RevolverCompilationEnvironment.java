package co.aurasphere.revolver.environment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import co.aurasphere.revolver.annotation.processor.ComponentProviderProcessor;
import co.aurasphere.revolver.annotation.processor.InjectorProcessor;
import co.aurasphere.revolver.codegen.CodegenController;

public class RevolverCompilationEnvironment {

	private Set<Class<?>> registeredProcessors;

	private List<ProcessingEnvironment> processingEnvironments;

	private Elements elementUtils;

	private Types typeUtils;

	private CollectionElementUtils collectionElementUtils;

	private boolean componentProviderProcessed;

	private boolean injectorProcessed;

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
		ProcessingEnvironment env = processingEnvironments.get(0);
		return new Messenger(env);
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
		
		if(klass.equals(InjectorProcessor.class)){
			this.injectorProcessed = true;
		}
		
		if(klass.equals(ComponentProviderProcessor.class)){
			this.componentProviderProcessed = true;
		}
		
		registeredProcessors.remove(klass);
		if (registeredProcessors.isEmpty()) {
			InjectorValidator.validate();
			ComponentProviderValidator.validate();
			CircularDependenciesValidator.validate();

			// If one of the two classes has not been generated (because no
			// annotation managed are processed) I generate it manually.
			Filer filer = processingEnvironments.get(0).getFiler();
			if(!componentProviderProcessed){
				CodegenController.generateComponentProvider(filer);
			}
			if(!injectorProcessed){
				CodegenController.generateInjector(filer);
			}

		}
	}

}
