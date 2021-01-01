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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.RevolverCompilationEnvironment;
import co.aurasphere.revolver.model.CollectionType;
import co.aurasphere.revolver.model.FieldRegistryEntry;

/**
 * Generates Revolver.java.
 * 
 * @author Donato Rimenti
 * @date 15/ago/2016
 */
@SuppressWarnings("restriction")
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
		
		RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.NOTE, "Starting Revolver Injector generation");

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
				RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
						"@Inject not supported on constructor. Annotate the class [" + className
								+ "] with @RevolverContext or call Revolver.inject(this) instead.", e);
			} else if (e.getKind() == ElementKind.METHOD) {
				RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
						"@Inject not supported on methods. Annotate the class [" + className
								+ "] with @RevolverContext or call Revolver.inject(this) instead.", e);
			} else if (e.getKind() == ElementKind.FIELD) {

				// Adds the field to the context.
				// Fields must not be final.
				VariableElement field = (VariableElement) e;
				FieldRegistryEntry fieldInfo = new FieldRegistryEntry(field);
				if (field.getModifiers().contains(Modifier.FINAL)) {
					RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
							"Fields annotated with @Inject can't be final: [" + fieldInfo.qualifiedName() + "]");
				} else {
					// If the field is an array or a collection I'll keep
					// separated to inject all instances later.
					CollectionType collectionType = getCollectionType(fieldInfo, processingEnv.getElementUtils(),
							processingEnv.getTypeUtils());

					// Adds the field to inject.
					RevolverCompilationEnvironment.INSTANCE.getRegistry().addFieldToInject(field, collectionType);
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

	private CollectionType getCollectionType(FieldRegistryEntry field, Elements elementUtils, Types typeUtils) {
		TypeMirror fieldTypeMirror = field.getTypeMirror();

		// Checks the erasure to leverage generic parameterized collections.
		TypeMirror erasure = typeUtils.erasure(fieldTypeMirror);
		if (fieldTypeMirror.getKind().equals(TypeKind.ARRAY)) {
			return CollectionType.ARRAY;
		}
		if (typeUtils.isAssignable(erasure, elementUtils.getTypeElement("java.util.Set").asType())) {
			return CollectionType.SET;
		}
		if (typeUtils.isAssignable(erasure, elementUtils.getTypeElement("java.util.List").asType())) {
			return CollectionType.LIST;
		}
		if (typeUtils.isAssignable(erasure, elementUtils.getTypeElement("java.util.Queue").asType())) {
			return CollectionType.QUEUE;
		}
		return CollectionType.SINGLE;
	}

}