package co.aurasphere.revolver.model;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import co.aurasphere.revolver.RevolverCompilationEnvironment;

public class ClassRegistryEntry extends BaseRevolverRegistryEntry<TypeElement> {

	private ExecutableElement constructor;

	private List<FieldRegistryEntry> constructorParams = new ArrayList<>();

	public ClassRegistryEntry(TypeElement typeElement) {
		super(typeElement);
		this.constructor = getDefaultConstructor(typeElement);
		if (constructor != null) {
			for (VariableElement e : this.constructor.getParameters()) {
				this.constructorParams.add(new FieldRegistryEntry(e));
			}
		}
	}

	private ExecutableElement getDefaultConstructor(TypeElement klass) {
		ExecutableElement defaultConstructor = null;

		// Gets all members of this class.
		List<? extends Element> allMembers = klass.getEnclosedElements();
		// Gets the constructors for the current class.
		List<ExecutableElement> constructors = ElementFilter.constructorsIn(allMembers);
		
		// Finds the default constructor.
		for (ExecutableElement e : constructors) {

			// The @Inject has priority. Stop checking
			// if one is found.
			if (e.getAnnotation(Inject.class) != null) {
				if (!e.getModifiers().contains(Modifier.PUBLIC)) {
					RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
							"@Inject can be only placed on public constructors", e);
				}
				defaultConstructor = e;
				break;
			}

			// Checks other public constructors.
			if (e.getModifiers().contains(Modifier.PUBLIC)) {

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
					"No public constructor found for class [" + klass + "]", klass);
			return null;
		}
		return defaultConstructor;
	}

	@Override
	public TypeElement getType() {
		return this.element;
	}

	public List<FieldRegistryEntry> getConstructorParameters() {
		return this.constructorParams;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constructorParams == null) ? 0 : constructorParams.hashCode());
		result = prime * result + ((element == null) ? 0 : element.hashCode());
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
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassInfo [typeElement=" + element + ", constructor=" + constructor + ", constructorParams="
				+ constructorParams + "]";
	}

	@Override
	public ExecutableElement getCreatorExecutableElement() {
		return this.constructor;
	}

}