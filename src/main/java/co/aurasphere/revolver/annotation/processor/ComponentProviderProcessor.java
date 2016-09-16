package co.aurasphere.revolver.annotation.processor;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import co.aurasphere.revolver.annotation.Component;
import co.aurasphere.revolver.annotation.Constant;
import co.aurasphere.revolver.annotation.Controller;
import co.aurasphere.revolver.annotation.Generator;
import co.aurasphere.revolver.annotation.Model;
import co.aurasphere.revolver.annotation.Repository;
import co.aurasphere.revolver.annotation.RevolverContext;
import co.aurasphere.revolver.annotation.Service;
import co.aurasphere.revolver.annotation.Utility;
import co.aurasphere.revolver.annotation.View;
import co.aurasphere.revolver.annotation.processor.internal.RevolverConstantAnnotationProcessorInternal;
import co.aurasphere.revolver.annotation.processor.internal.RevolverContextAnnotationProcessorInternal;
import co.aurasphere.revolver.annotation.processor.internal.RevolverGeneratorAnnotationProcessorInternal;
import co.aurasphere.revolver.codegen.CodegenController;

/**
 * Generates RevolverComponentProvider.java.
 * 
 * @author Donato
 * @date 15/ago/2016
 */
@SupportedAnnotationTypes(value = {
		"co.aurasphere.revolver.annotation.RevolverContext",
		"co.aurasphere.revolver.annotation.Controller",
		"co.aurasphere.revolver.annotation.Service",
		"co.aurasphere.revolver.annotation.View",
		"co.aurasphere.revolver.annotation.Repository",
		"co.aurasphere.revolver.annotation.Utility",
		"co.aurasphere.revolver.annotation.Component",
		"co.aurasphere.revolver.annotation.Model",

		"co.aurasphere.revolver.annotation.Constant",
		"co.aurasphere.revolver.annotation.Generator" })
public class ComponentProviderProcessor extends BaseProcessor {

	private RevolverContextAnnotationProcessorInternal contextAnnotationProcessor;

	private RevolverGeneratorAnnotationProcessorInternal generatorAnnotationProcessor;

	private RevolverConstantAnnotationProcessorInternal constantAnnotationProcessor;

	public ComponentProviderProcessor() {
		contextAnnotationProcessor = new RevolverContextAnnotationProcessorInternal();
		generatorAnnotationProcessor = new RevolverGeneratorAnnotationProcessorInternal();
		constantAnnotationProcessor = new RevolverConstantAnnotationProcessorInternal();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		environment.register(processingEnv, ComponentProviderProcessor.class);

		@SuppressWarnings("unchecked")
		Set<Element> contextComponents = (Set<Element>) roundEnv
				.getElementsAnnotatedWith(RevolverContext.class);

		// Adds the components under the context before processing them.
		contextComponents.addAll(roundEnv
				.getElementsAnnotatedWith(Controller.class));
		contextComponents.addAll(roundEnv
				.getElementsAnnotatedWith(Service.class));
		contextComponents.addAll(roundEnv.getElementsAnnotatedWith(View.class));
		contextComponents.addAll(roundEnv
				.getElementsAnnotatedWith(Repository.class));
		contextComponents.addAll(roundEnv
				.getElementsAnnotatedWith(Component.class));
		contextComponents.addAll(roundEnv
				.getElementsAnnotatedWith(Utility.class));
		contextComponents
				.addAll(roundEnv.getElementsAnnotatedWith(Model.class));

		contextAnnotationProcessor.processInternal(contextComponents);
		generatorAnnotationProcessor.processInternal(roundEnv
				.getElementsAnnotatedWith(Generator.class));
		constantAnnotationProcessor.processInternal(roundEnv
				.getElementsAnnotatedWith(Constant.class));

		// Performs validation.
		if (roundEnv.processingOver()) {
			environment.terminate(ComponentProviderProcessor.class);
			CodegenController.generateComponentProvider(processingEnv
					.getFiler());
		}

		// No more processing for this annotation.
		environment.terminateRound(processingEnv);
		return true;
	}

}
