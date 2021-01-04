package co.aurasphere.revolver.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.RevolverCompilationEnvironment;

/**
 * Generates RevolverComponentProvider.java.
 * 
 * @author Donato Rimenti
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("javax.inject.Singleton")
public class ComponentProviderProcessor extends AbstractProcessor {

	private boolean firstRound = true;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		if (firstRound) {
			RevolverCompilationEnvironment.INSTANCE.register(processingEnv, ComponentProviderProcessor.class);
			firstRound = false;
		}

		RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.NOTE,
				"Starting Revolver Component Provider generation", null);
		processAnnotations(roundEnv.getElementsAnnotatedWith(Singleton.class), processingEnv);

		// Terminates the single round or all the processing.
		if (roundEnv.processingOver()) {
			RevolverCompilationEnvironment.INSTANCE.terminate(ComponentProviderProcessor.class, processingEnv);
			// No more processing for this annotation.
			RevolverCompilationEnvironment.INSTANCE.terminateRound(processingEnv);
		}

		return true;
	}

	public void processAnnotations(Set<? extends Element> elements, ProcessingEnvironment processingEnv) {
		for (Element e : elements) {
			// There are two possible cases: classes and methods.
			if (e.getKind() == ElementKind.CLASS) {
				TypeElement klass = (TypeElement) e;
				ElementKind kind = klass.getKind();
				Set<Modifier> modifiers = klass.getModifiers();

				// Checks that we're dealing with a public, not abstract, class.
				if (kind == ElementKind.ENUM || kind == ElementKind.INTERFACE || modifiers.contains(Modifier.ABSTRACT)
						|| !modifiers.contains(Modifier.PUBLIC)
						|| klass.getEnclosingElement().getKind() != ElementKind.PACKAGE
								&& !modifiers.contains(Modifier.STATIC)) {
					RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
							"@Singleton can only be placed on a public, not abstract, class. Nested classes are supported only if static.",
							klass);
				} else {
					RevolverCompilationEnvironment.INSTANCE.getRegistry().addManagedClass(klass);
				}
			} else if (e.getKind() == ElementKind.METHOD) {

				// A generator method can be added only if the enclosing instance is added
				// explicitly too.
				Element enclosingElement = e.getEnclosingElement();
				if (enclosingElement.getAnnotation(Singleton.class) == null) {
					RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
							"@Singleton can only be on a method of a class annotated with @Singleton", e);
				} else {

					// Generator methods must be public, static and must not return void. We don't
					// check abstract since the enclosing class must be annotated and that's already
					// checked.
					ExecutableElement method = (ExecutableElement) e;
					if (method.getReturnType().getKind() == TypeKind.VOID
							|| !e.getModifiers().contains(Modifier.PUBLIC)) {
						RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
								"@Singleton methods must be public and must not return void", e);
					} else {
						Element returnElement = processingEnv.getTypeUtils().asElement(method.getReturnType());
						RevolverCompilationEnvironment.INSTANCE.getRegistry().addManagedMethod(method, returnElement);
					}
				}
			}
		}
	}

}