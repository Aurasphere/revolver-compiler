	package co.aurasphere.revolver.annotation.processor.internal;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class RevolverContextAnnotationProcessorInternal extends
		BaseRevolverAnnotationProvessorInternal {

	public void processInternal(Set<? extends Element> elements) {
		for (Element e : elements) {
			// There are only classes.
			addClassToContext((TypeElement) e);
		}
	}

}
