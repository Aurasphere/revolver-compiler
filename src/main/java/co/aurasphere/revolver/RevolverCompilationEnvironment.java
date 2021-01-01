package co.aurasphere.revolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.gson.Gson;

import co.aurasphere.revolver.annotation.processor.ComponentProviderProcessor;
import co.aurasphere.revolver.annotation.processor.InjectorProcessor;

@SuppressWarnings("restriction")
public enum RevolverCompilationEnvironment {
	INSTANCE;

	private Set<Class<?>> registeredProcessors;
	private List<ProcessingEnvironment> processingEnvironments;
	private Types typeUtils;
	private boolean componentProviderProcessed;
	private boolean injectorProcessed;
	private CodeGenerator codeGenerator;
	private Validator validator;
	private RevolverRegistry registry;

	private RevolverCompilationEnvironment() {
		this.codeGenerator = new CodeGenerator();
		this.validator = new Validator();
		this.processingEnvironments = new ArrayList<ProcessingEnvironment>();
		this.registeredProcessors = new HashSet<Class<?>>();
		this.registry = new RevolverRegistry();
	}

	public void register(ProcessingEnvironment processingEnv, Class<?> klass) {
		this.registeredProcessors.add(klass);
		this.processingEnvironments.add(processingEnv);

		if (this.typeUtils == null) {
			this.typeUtils = processingEnv.getTypeUtils();
		}

//		if (this.registry == null) {
//			FileObject registryState = null;
//			try {
//				registryState = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT,
//						"co.aurasphere.revolver", "RegistryState");
//				Reader rsis = registryState.openReader(false);
//				Gson gson = new Gson();
//				this.registry = gson.fromJson(rsis, RevolverRegistry.class);
//			} catch (FileNotFoundException e) {
//				// File doesn't exist, initialize registry.
//				System.out.println("INITIALIZING REGISTRY FIRST TIME");
//				this.registry = new RevolverRegistry();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	public void terminateRound(ProcessingEnvironment processingEnv) {
		processingEnvironments.remove(processingEnv);
	}

	public void printMessage(Diagnostic.Kind kind, String msg) {
		processingEnvironments.get(0).getMessager().printMessage(kind, msg);
	}
	
	public void printMessage(Diagnostic.Kind kind, String msg, Element element) {
		processingEnvironments.get(0).getMessager().printMessage(kind, msg, element);
	}

	/**
	 * Starts the validation as soon as each processor ended.Injector validation
	 * must be done before component provider.
	 */
	public void terminate(Class<?> klass, ProcessingEnvironment processingEnv) {
		Filer filer = processingEnv.getFiler();
		Types typeUtils = processingEnv.getTypeUtils();

		if (klass.equals(InjectorProcessor.class)) {
			System.out.println("GENERATING INJECTOR FROM CLASS REMOVE");
			try {
				FileObject loadedFile = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT,
						"co.aurasphere.revolver", "Revolver");
				System.out.println("LOADED FILE: " + loadedFile.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.injectorProcessed = true;
			codeGenerator.generateInjector(filer);
		}

		if (klass.equals(ComponentProviderProcessor.class)) {
			System.out.println("GENERATING PROVIDER FROM CLASS REMOVE");
			this.componentProviderProcessed = true;
			codeGenerator.generateComponentProvider(filer, typeUtils);
		}

		registeredProcessors.remove(klass);
		if (registeredProcessors.isEmpty()) {
			validator.validateInjector(typeUtils);
			validator.validateComponentProvider();

			// If one of the two classes has not been generated (because no
			// annotation managed are processed) I generate it manually.
			if (!componentProviderProcessed) {
				codeGenerator.generateComponentProvider(filer, typeUtils);
			}
			if (!injectorProcessed) {
				codeGenerator.generateInjector(filer);
			}

			// Save registry state.
//			FileObject registryState = null;
//			try {
//				registryState = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,
//						"co.aurasphere.revolver", "RegistryState");
//				Writer rsis = registryState.openWriter();
//				Gson gson = new Gson();
//				gson.toJson(this.registry, rsis);
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	public RevolverRegistry getRegistry() {
		return registry;
	}

	// TODO Remove
	public Types getTypeUtils() {
		return this.typeUtils;
	}

}