package co.aurasphere.revolver.model;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class MethodRegistryEntry extends BaseRevolverRegistryEntry<Element> {

	private ExecutableElement executableElement;

	private List<FieldRegistryEntry> parametersFieldInfo;

	private ClassRegistryEntry parentClassInfo;
	
	public MethodRegistryEntry(ExecutableElement element, Element returnType) {
		// The super constructor is called with the return type because that's
		// the element put under the context.
		super(returnType);

		this.executableElement = element;
		this.parentClassInfo = new ClassRegistryEntry((TypeElement) element.getEnclosingElement());
		this.parametersFieldInfo = new ArrayList<FieldRegistryEntry>();
		for (VariableElement v : element.getParameters()) {
			parametersFieldInfo.add(new FieldRegistryEntry(v));
		}
	}

	@Override
	public ExecutableElement getType() {
		return this.executableElement;
	}

	public ExecutableElement getExecutableElement() {
		return this.executableElement;
	}

	public ClassRegistryEntry getParentClassInfo() {
		return this.parentClassInfo;
	}

	public List<FieldRegistryEntry> getParametersFieldInfo() {
		return this.parametersFieldInfo;
	}

	public TypeMirror getReturnTypeMirror() {
		return this.executableElement.getReturnType();
	}

	public String getMethodName() {
		return this.executableElement.getSimpleName().toString();
	}

	@Override
	public ExecutableElement getCreatorExecutableElement() {
		return this.executableElement;
	}

}