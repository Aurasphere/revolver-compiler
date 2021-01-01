package co.aurasphere.revolver.model;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class MethodRegistryEntry extends BaseRevolverRegistryEntry {

	private TypeMirror returnTypeMirror;

	private ExecutableElement executableElement;

	private List<? extends VariableElement> parameters;

	private List<FieldRegistryEntry> parametersFieldInfo;

	private TypeElement parentClass;

	private ClassRegistryEntry parentClassInfo;

	private Element returnType;

	private String methodName;
	
	private boolean singleton;
	
	public MethodRegistryEntry(ExecutableElement element, Element returnType) {
		// The super constructor is called with the return type because that's
		// the element put under the context.
		super(returnType);

		this.executableElement = element;
		this.returnTypeMirror = element.getReturnType();
		this.returnType = returnType;

		this.parentClass = (TypeElement) element.getEnclosingElement();
		this.parentClassInfo = new ClassRegistryEntry(parentClass);

		this.parameters = element.getParameters();
		this.parametersFieldInfo = new ArrayList<FieldRegistryEntry>();
		for (VariableElement v : this.parameters) {
			parametersFieldInfo.add(new FieldRegistryEntry(v, this.parentClass));
		}
		
		this.methodName = element.getSimpleName().toString();
		this.singleton = isAnnotatedWith(element, Singleton.class);

	}

	@Override
	public ExecutableElement getType() {
		return this.executableElement;
	}

	public Element getReturnType() {
		return this.returnType;
	}

	public ExecutableElement getExecutableElement() {
		return this.executableElement;
	}

	public List<? extends VariableElement> getParameters() {
		return this.parameters;
	}

	public TypeElement getParentClass() {
		return this.parentClass;
	}

	public ClassRegistryEntry getParentClassInfo() {
		return this.parentClassInfo;
	}

	public List<FieldRegistryEntry> getParametersFieldInfo() {
		return this.parametersFieldInfo;
	}

	public TypeMirror getReturnTypeMirror() {
		return this.returnTypeMirror;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
