package co.aurasphere.revolver.annotation.processor.internal;

import static co.aurasphere.revolver.environment.ErrorMessageBundle.INVALID_RETURN_TYPE_VOID_E;
import static co.aurasphere.revolver.environment.ErrorMessageBundle.INVALID_GENERATOR_METHOD_NOT_PUBLIC_E;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;

import co.aurasphere.revolver.environment.ErrorMessageBundle;
import co.aurasphere.revolver.registry.FieldInfo;
import co.aurasphere.revolver.registry.MethodInfo;

public class RevolverGeneratorAnnotationProcessorInternal extends
		BaseRevolverAnnotationProvessorInternal {

	public void processInternal(Set<? extends Element> elements) {
		for (Element e : elements) {
			// There are two possible cases: classes and methods.
			if (e.getKind() == ElementKind.CLASS) {
				processClass((TypeElement) e);
			} else if (e.getKind() == ElementKind.METHOD) {
				addMethodToContext((ExecutableElement) e, true);
			}
		}

	}

	private void processClass(TypeElement klass) {

		// Adds this class as provided.
		addClassToContext(klass);
		
		List<? extends Element> members = klass.getEnclosedElements();
		List<VariableElement> fields = ElementFilter.fieldsIn(members);
		List<ExecutableElement> methods = ElementFilter.methodsIn(members);
		
		// Adds any possible constant and method to the context.
		for (VariableElement e : fields) {
			addFieldToContext(e, false);
		}
		
		for(ExecutableElement m : methods){
			addMethodToContext(m, false);
		}
		// TODO Auto-generated method stub
		// Se una classe è annotata con @Generator:
		// - tutte le sue costanti final diventano @Constant
		// - tutti i metodi pubblici e non void diventano @Generator
		// - tutte le sottoclassi publi
		// Tutto questo va loggato come si deve a livello di warning e info.
	}

	private void addMethodToContext(ExecutableElement e, boolean hardValidation) {
		if (isMethodValid(e, hardValidation)) {
			Element returnElement = environment.getTypeUtils().asElement(
					e.getReturnType());
			registry.addManagedMethod(e, returnElement);
		}
	}

	private boolean isMethodValid(ExecutableElement e, boolean throwException) {
		// Generator methods can't return void.
		if (e.getReturnType().getKind() == TypeKind.VOID) {
			if (throwException) {
				messenger().error(INVALID_RETURN_TYPE_VOID_E, e,
						e.getEnclosingElement());
			}
			return false;
		}
		// Generator methods must be public.
		if (!e.getModifiers().contains(Modifier.PUBLIC)) {
			if (throwException) {
				messenger()
						.error(ErrorMessageBundle.INVALID_GENERATOR_METHOD_NOT_PUBLIC_E,
								e, e.getEnclosingElement());
			}
			return false;
		}
		return true;
	}
	
	protected void log(ErrorMessageBundle message, boolean throwException, Object... args){
		if(throwException){
			messenger().error(message, args);
		} else {
			messenger().warning(message, args);
		}
	}

}
