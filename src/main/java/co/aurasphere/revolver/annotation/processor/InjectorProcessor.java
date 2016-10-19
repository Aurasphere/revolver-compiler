package co.aurasphere.revolver.annotation.processor;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import co.aurasphere.revolver.codegen.CodegenController;
import co.aurasphere.revolver.environment.ErrorMessageBundle;
import co.aurasphere.revolver.registry.CollectionType;
import co.aurasphere.revolver.registry.FieldInfo;

/**
 * Generates Revolver.java.
 * 
 * @author Donato Rimenti
 * @date 15/ago/2016
 */

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes(value = { "javax.inject.Inject" })
public class InjectorProcessor extends BaseProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		environment.register(processingEnv, InjectorProcessor.class);
		messenger().note("Starting Revolver Injector generation");

		for (Element e : roundEnv.getElementsAnnotatedWith(Inject.class)) {
			String className = e.getEnclosingElement().toString();
			// Three possible cases: methods, constructors or fields.
			if (e.getKind() == ElementKind.CONSTRUCTOR) {
				// Constructors injection can only be performed by annotating
				// the class or calling the method Revolver.inject(this). TODO:
				// should be able to do in this way as well. Take the
				// constructor and the enclosing element (class) and put that
				// under the context. Not a good idea though because it makes
				// reading the code harder.
				messenger().error(ErrorMessageBundle.INJECT_ON_CONSTRUCTOR_E,
						className);
			} else if (e.getKind() == ElementKind.METHOD) {

				// TODO: future release, not sure if feasible. Can be done using
				// a proxy. Are there any other ways? Not sure... L'inject del
				// metodo di dagger avviene chiamanndo il metodo dopo il
				// costruttore. Da vedere se vale la pena o combinare in qualche
				// modo con AOP.
				ExecutableElement exeElement = (ExecutableElement) e;

			} else if (e.getKind() == ElementKind.FIELD) {

				// Adds the field to the context.
				// Fields must not be final.
				VariableElement field = (VariableElement) e;
				FieldInfo fieldInfo = new FieldInfo(field);
				if (field.getModifiers().contains(Modifier.FINAL)) {
					messenger().error(ErrorMessageBundle.INVALID_FIELD_FINAL_E,
							fieldInfo.qualifiedName());
				} else {
					// If the field is an array or a collection I'll keep
					// separated to inject all instances later.
					CollectionType collectionType = environment
							.getCollectionElementUtils().getCollectionType(
									fieldInfo);
					if (!collectionType.equals(CollectionType.SIMPLE)) {
						registry.addCollectionToInject(field, collectionType);
					}

					// Adds the field to inject.
					registry.addFieldToInject(field, collectionType);
				}
			}
		}

		// Performs validation.
		if (roundEnv.processingOver()) {
			environment.terminate(InjectorProcessor.class);
			CodegenController.generateInjector(processingEnv.getFiler());
		}

		// No more processing for this annotation.
		environment.terminateRound(processingEnv);
		return true;
	}
}
