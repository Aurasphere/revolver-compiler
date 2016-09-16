package co.aurasphere.revolver.annotation.processor.internal;

import static co.aurasphere.revolver.environment.ErrorMessageBundle.INVALID_RETURN_TYPE_VOID_E;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import co.aurasphere.revolver.registry.FieldInfo;
import co.aurasphere.revolver.registry.MethodInfo;

public class RevolverGeneratorAnnotationProcessorInternal extends
		BaseComponentProviderAnnotationProcessorInternal {

	public void processInternal(Set<? extends Element> elements) {
		for (Element e : elements) {
			// There are two possible cases: classes and methods.
			if (e.getKind() == ElementKind.CLASS) {
				processClass((TypeElement) e);
			} else if (e.getKind() == ElementKind.METHOD) {
				processMethod((ExecutableElement) e);
			}
		}

	}

	private void processClass(TypeElement klass) {

		List<? extends Element> members = klass.getEnclosedElements();
		for (Element e : members) {
			if (e.getKind() == null) {

			}
		}
		// TODO Auto-generated method stub
		// Se una classe è annotata con @Generator:
		// - tutte le sue costanti final diventano @Constant
		// - tutti i metodi pubblici e non void diventano @Generator
		// - tutte le sottoclassi publi
		// Tutto questo va loggato come si deve a livello di warning e info.
	}

	private void processMethod(ExecutableElement e) {
		if (isMethodValid(e)) {
			Element returnElement = environment.getTypeUtils().asElement(
					e.getReturnType());
			registry.addManagedMethod(e, returnElement);
		}
	}

	private boolean isMethodValid(ExecutableElement e) {
		// Generator method can't return void.
		if (e.getReturnType().getKind() == TypeKind.VOID) {
			messenger().error(INVALID_RETURN_TYPE_VOID_E, e,
					e.getEnclosingElement());
		}
		return true;
	}

}
