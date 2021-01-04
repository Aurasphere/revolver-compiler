package co.aurasphere.revolver.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.RevolverCompilationEnvironment;

/**
 * Generates Revolver.java.
 * 
 * @author Donato Rimenti
 * @date 15/ago/2016
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("javax.inject.Inject")
public class InjectorProcessor extends AbstractProcessor {

	private boolean firstRound = true;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		if (firstRound) {
			RevolverCompilationEnvironment.INSTANCE.register(processingEnv, InjectorProcessor.class);
			firstRound = false;
		}

		RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.NOTE, "Starting Revolver Injector generation", null);

		for (Element e : roundEnv.getElementsAnnotatedWith(Inject.class)) {
			String className = e.getEnclosingElement().toString();
			// Three possible cases: methods, constructors or fields.
			if (e.getKind() == ElementKind.CONSTRUCTOR) {
				// Skips the constructors since they'll be picked up later when processing the
				// annotated classes.
			} else if (e.getKind() == ElementKind.METHOD) {
				RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
						"@Inject not supported on methods. Annotate the class [" + className
								+ "] with @Singleton or call Revolver.inject(this) instead.",
						e);
			} else if (e.getKind() == ElementKind.FIELD) {

				// Adds the field to the context.
				// Fields must not be final.
				VariableElement field = (VariableElement) e;
				if (field.getModifiers().contains(Modifier.FINAL)) {
					RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
							"Fields annotated with @Inject can't be final", e);
				} else {
					// Adds the field to inject.
					RevolverCompilationEnvironment.INSTANCE.getRegistry().addFieldToInject(field);
				}
			}
		}

		// Terminates the single round or all the processing.
		if (roundEnv.processingOver()) {
			RevolverCompilationEnvironment.INSTANCE.terminate(InjectorProcessor.class, processingEnv);
			// No more processing for this annotation.
			RevolverCompilationEnvironment.INSTANCE.terminateRound(processingEnv);
		}

		return true;
	}

}