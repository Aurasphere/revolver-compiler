package co.aurasphere.revolver.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.RevolverCompilationEnvironment;
import co.aurasphere.revolver.annotation.DefaultConstructor;

public class ClassRegistryEntry extends BaseRevolverRegistryEntry {

	private TypeElement typeElement;

	private ExecutableElement constructor;

	private List<FieldRegistryEntry> constructorParams;

	private List<ClassRegistryEntry> fieldsToInject;

//	public ClassRegistryEntry(TypeElement typeElement, ExecutableElement constructor) {
//		this(typeElement);
//		this.constructor = constructor;
//		this.constructorParams = new ArrayList<FieldRegistryEntry>();
//		for (VariableElement e : constructor.getParameters()) {
//			this.constructorParams.add(new FieldRegistryEntry(e, typeElement));
//			// Adds the constructor params to the fields to inject for latter
//			// validation (circular dependencies).
////			this.fieldsToInject.add(new ClassRegistryEntry(variableElementToTypeElement(e)));
//		}
//	}

	private Set<TypeElement> dependencies = new HashSet<>();

	// Very simple implementation. Performance can be improved by checking all
	// classes together in the end instead.
	public void findCircularDependencyRecursively(TypeElement e) {
		ExecutableElement constructor = getDefaultConstructor(e);
		for (VariableElement parameter : constructor.getParameters()) {
			TypeElement dependency = variableElementToTypeElement(parameter);
			if (dependencies.contains(dependency)) {
				throw new IllegalArgumentException("Circular dependency detected");
			}
			dependencies.add(dependency);
			findCircularDependencyRecursively(dependency);
		}
	}

	public ClassRegistryEntry(TypeElement typeElement) {
		super(typeElement);
		this.typeElement = typeElement;
		this.constructorParams = new ArrayList<FieldRegistryEntry>();

		// Detects circular dependencies in constructors.
		try {
			findCircularDependencyRecursively(typeElement);
		} catch (IllegalArgumentException e) {
			// TODO: improve this message and log better
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR, "Circular dependency detected!", typeElement);
			return;
		}

		this.constructor = getDefaultConstructor(typeElement);
		for (VariableElement e : this.constructor.getParameters()) {
			this.constructorParams.add(new FieldRegistryEntry(e, typeElement));
		}
	}

	@Override
	public TypeElement getType() {
		return this.typeElement;
	}

	public List<FieldRegistryEntry> getConstructorParameters() {
		return this.constructorParams;
	}

	/**
	 * Lazily initialized to prevent circular dependencies.
	 * 
	 * @return
	 */
	public List<ClassRegistryEntry> getFieldsToInject() {
		if (this.fieldsToInject == null) {
			this.fieldsToInject = new ArrayList<ClassRegistryEntry>();
			if (typeElement != null) {
				List<? extends Element> members = typeElement.getEnclosedElements();
				List<VariableElement> fields = ElementFilter.fieldsIn(members);

				// Adds the constructor params to the fields to inject for
				// latter
				// validation (circular dependencies).
				if (this.constructor != null) {
					for (VariableElement e : this.constructor.getParameters()) {
						this.fieldsToInject.add(new ClassRegistryEntry(variableElementToTypeElement(e)));
					}
				}
				// Adds the field to inject for latter validation (circular
				// dependencies).
				for (VariableElement f : fields) {
					if (f.getAnnotation(Inject.class) != null) {
						this.fieldsToInject.add(new ClassRegistryEntry(variableElementToTypeElement(f)));
					}
				}
			}
		}
		return this.fieldsToInject;
	}

	// TODO: RevolverDefaultConstructor on private constructor should probably
	// throw exception. Multiple annotation should do the same. Not sure, seems
	// kinda optimal like this though.
	private ExecutableElement getDefaultConstructor(TypeElement klass) {
		ExecutableElement defaultConstructor = null;

		// Gets all members of this class.
		List<? extends Element> allMembers = klass.getEnclosedElements();
		// Gets the constructors for the current class.
		List<ExecutableElement> constructors = ElementFilter.constructorsIn(allMembers);

		// Finds the default constructor.
		for (ExecutableElement e : constructors) {
			// Checks only public constructors.
			if (e.getModifiers().contains(Modifier.PUBLIC)) {
				// The @DefaultConstructor has priority. Stop checking
				// if one is found.
				if (e.getAnnotation(DefaultConstructor.class) != null) {
					defaultConstructor = e;
					break;
				}
				// Next in priority order there's a constructor without
				// parameters.
				if (e.getParameters().isEmpty()) {
					defaultConstructor = e;
				}
				// Lastly, if it haven't found anything else, I get the first
				// constructor I find.
				if (defaultConstructor == null) {
					defaultConstructor = e;
				}
			}
		}
		// If there's no public constructor, an exception is thrown.
		if (defaultConstructor == null) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"No public constructor found for class [" + klass + "]");
			return null;
		}
		if (defaultConstructor.isVarArgs()) {
			// TODO: future release? inject all implementation known, same for
			// lists and array
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"Varargs constructors not supported yet. Please specify a different constructor for class [" + klass
							+ "]");
			return null;
		}
		return defaultConstructor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constructorParams == null) ? 0 : constructorParams.hashCode());
		result = prime * result + ((typeElement == null) ? 0 : typeElement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassRegistryEntry other = (ClassRegistryEntry) obj;
		if (constructorParams == null) {
			if (other.constructorParams != null)
				return false;
		} else if (!constructorParams.equals(other.constructorParams))
			return false;
		if (typeElement == null) {
			if (other.typeElement != null)
				return false;
		} else if (!typeElement.equals(other.typeElement))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassInfo [typeElement=" + typeElement + ", constructor=" + constructor + ", constructorParams="
				+ constructorParams + ", fieldsToInject=" + fieldsToInject + "]";
	}

}