package co.aurasphere.revolver.annotation.processor;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.RevolverCompilationEnvironment;
import co.aurasphere.revolver.annotation.Component;
import co.aurasphere.revolver.annotation.DefaultConstructor;

/**
 * Generates RevolverComponentProvider.java.
 * 
 * @author Donato Rimenti
 */
@SuppressWarnings("restriction")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("co.aurasphere.revolver.annotation.Component")
public class ComponentProviderProcessor extends AbstractProcessor {

	private boolean firstRound = true;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		if (firstRound) {
			RevolverCompilationEnvironment.INSTANCE.register(processingEnv, ComponentProviderProcessor.class);
			firstRound = false;
		}

		RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.NOTE,
				"Starting Revolver Component Provider generation");
		processAnnotations(roundEnv.getElementsAnnotatedWith(Component.class), processingEnv);

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
				addClassToContext((TypeElement) e);
			} else if (e.getKind() == ElementKind.METHOD) {

				// A generator method can be added only if the enclosing instance is added
				// explicitly too.
				Element enclosingElement = e.getEnclosingElement();
				if (enclosingElement.getAnnotation(Component.class) == null) {
					RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
							"@Component can only be on a method of a class annotated with @Component", e);
				} else {
					addMethodToContext((ExecutableElement) e, processingEnv.getTypeUtils());
				}
			}
		}
	}

	protected void addClassToContext(TypeElement klass) {
		ElementKind kind = klass.getKind();
		Set<Modifier> modifiers = klass.getModifiers();

		// Checks that we're dealing with a public, not abstract, class.
		if (kind == ElementKind.ENUM || kind == ElementKind.INTERFACE || modifiers.contains(Modifier.ABSTRACT)
				|| !modifiers.contains(Modifier.PUBLIC)) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"@Component can only be placed on a public, not abstract, class. Check the following type: ["
							+ klass + "]");
		} else {
			RevolverCompilationEnvironment.INSTANCE.getRegistry().addManagedClass(klass);
		}
	}

	private void addMethodToContext(ExecutableElement e, Types typeUtils) {
		// Generator methods must be public, static and must not return void.
		if (e.getReturnType().getKind() == TypeKind.VOID || !e.getModifiers().contains(Modifier.PUBLIC)) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"@Component methods must be public and must not return void. Check method [" + e + "] in type ["
							+ e.getEnclosingElement() + "].");
		} else {
			Element returnElement = typeUtils.asElement(e.getReturnType());
			RevolverCompilationEnvironment.INSTANCE.getRegistry().addManagedMethod(e, returnElement);
		}
	}

}