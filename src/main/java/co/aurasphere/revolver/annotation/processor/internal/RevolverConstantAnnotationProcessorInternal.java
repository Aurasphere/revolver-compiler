package co.aurasphere.revolver.annotation.processor.internal;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import co.aurasphere.revolver.environment.ErrorMessageBundle;
import co.aurasphere.revolver.registry.FieldInfo;
import co.aurasphere.revolver.registry.RevolverRegistry;

public class RevolverConstantAnnotationProcessorInternal extends
		BaseComponentProviderAnnotationProcessorInternal {

	public void processInternal(Set<? extends Element> elements) {
		for (Element e : elements) {
			// There are only fields.
			processField((VariableElement) e);
		}
	}

	private void processField(VariableElement e) {
		if (isFieldValid(e)) {
			RevolverRegistry.getInstance().addManagedConstant(e);
		}
	}

	private boolean isFieldValid(VariableElement e) {
		Set<Modifier> modifier = e.getModifiers();
		String fullFieldName = new FieldInfo(e).qualifiedName();
		
		// Fields must be final.
		if (!modifier.contains(Modifier.FINAL)) {
			messenger().error(ErrorMessageBundle.INVALID_FIELD_NOT_FINAL_E, fullFieldName);
			return false;
		}
		// Fields must be primitive or String. e.getConstantValue() will return
		// null if this condition is not met.
		Object fieldValue = e.getConstantValue();
		if (fieldValue == null) {
			messenger().error(ErrorMessageBundle.INVALID_CONSTANT_NOT_PRIMITIVE_E, fullFieldName);
			return false;
		}
		return true;
	}
}
