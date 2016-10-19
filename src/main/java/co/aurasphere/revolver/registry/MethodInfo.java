package co.aurasphere.revolver.registry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class MethodInfo extends RevolverRegistryEntry {

	private TypeMirror returnTypeMirror;

	private ExecutableElement executableElement;

	private List<? extends VariableElement> parameters;

	private List<FieldInfo> parametersFieldInfo;

	private TypeElement parentClass;

	private ClassInfo parentClassInfo;

	private Element returnType;

	private String methodName;
	
	private boolean singleton;
	
	public MethodInfo(ExecutableElement element, Element returnType) {
		// The super constructor is called with the return type because that's
		// the element put under the context.
		super(returnType);

		this.executableElement = element;
		this.returnTypeMirror = element.getReturnType();
		this.returnType = returnType;

		this.parentClass = (TypeElement) element.getEnclosingElement();
		this.parentClassInfo = new ClassInfo(parentClass);

		this.parameters = element.getParameters();
		this.parametersFieldInfo = new ArrayList<FieldInfo>();
		for (VariableElement v : this.parameters) {
			parametersFieldInfo.add(new FieldInfo(v, this.parentClass));
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

	public ClassInfo getParentClassInfo() {
		return this.parentClassInfo;
	}

	public List<FieldInfo> getParametersFieldInfo() {
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
