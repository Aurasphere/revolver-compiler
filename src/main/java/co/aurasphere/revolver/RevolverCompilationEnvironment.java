package co.aurasphere.revolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public enum RevolverCompilationEnvironment {
	INSTANCE;

	private Set<Class<?>> registeredProcessors;
	private List<ProcessingEnvironment> processingEnvironments;
	private Types typeUtils;
	private Elements elementUtils;
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

		if (this.elementUtils == null) {
			this.elementUtils = processingEnv.getElementUtils();
		}
	}

	public void terminateRound(ProcessingEnvironment processingEnv) {
		processingEnvironments.remove(processingEnv);
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
		registeredProcessors.remove(klass);
		if (registeredProcessors.isEmpty()) {
			validator.validateState();
			codeGenerator.generateComponentProvider(filer, typeUtils);
			codeGenerator.generateInjector(filer);
		}
	}

	public RevolverRegistry getRegistry() {
		return registry;
	}

	public Types getTypeUtils() {
		return this.typeUtils;
	}

	public Elements getElementUtils() {
		return elementUtils;
	}

}