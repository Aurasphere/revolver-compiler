package co.aurasphere.revolver.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import co.aurasphere.revolver.annotation.Constant;

public class RevolverRegistry {

	private static RevolverRegistry instance;

	/**
	 * List that contains all the constant fields managed by Revolver. A
	 * constants is a final primitive or String field annotated with
	 * {@link Constant}.
	 */
	private List<FieldInfo> managedConstants;

	private HashMap<ClassInfo, List<FieldInfo>> injectionMap;

	/**
	 * Maps with getterName, returnType.
	 */
	private List<FieldInfo> requiredFields;

	private HashMap<String, ClassInfo> managedClasses;

	private HashMap<String, RevolverRegistryEntry> managedElements;

	private List<FieldInfo> collectionsToInject;

	private List<MethodInfo> managedMethods;

	private RevolverRegistry() {
		this.managedConstants = new ArrayList<FieldInfo>();
		this.injectionMap = new HashMap<ClassInfo, List<FieldInfo>>();
		this.requiredFields = new ArrayList<FieldInfo>();
		this.managedClasses = new HashMap<String, ClassInfo>();
		this.managedElements = new HashMap<String, RevolverRegistryEntry>();
		this.collectionsToInject = new ArrayList<FieldInfo>();
		this.managedMethods = new ArrayList<MethodInfo>();
	}

	public static RevolverRegistry getInstance() {
		if (instance == null) {
			instance = new RevolverRegistry();
		}
		return instance;
	}

	public List<FieldInfo> getManagedConstants() {
		return managedConstants;
	}

	public void addManagedConstant(VariableElement managedConstant) {
		FieldInfo info = new FieldInfo(managedConstant);
		this.managedElements.put(info.getterMethodName(), info);
		this.managedConstants.add(info);
	}

	public Collection<ClassInfo> getManagedClasses() {
		return managedClasses.values();
	}

	public void addManagedClass(TypeElement managedClass,
			ExecutableElement constructor) {
		ClassInfo info = new ClassInfo(managedClass, constructor);
		this.managedElements.put(info.getterMethodName(), info);
		this.managedClasses.put(info.getterMethodName(), info);

		// Registers the constructor arguments as required for validation.
		for (FieldInfo f : info.getConstructorParameters()) {
			addRequiredField(f);
		}

	}

	public Set<ClassInfo> getClassesToInject() {
		return injectionMap.keySet();
	}

	public void addFieldToInject(FieldInfo fieldInfo) {
		ClassInfo parentClass = fieldInfo.getParentClassInfo();

		List<FieldInfo> fieldsList = getInjectionMap().get(parentClass);
		if (fieldsList == null) {
			fieldsList = new ArrayList<FieldInfo>();
			getInjectionMap().put(parentClass, fieldsList);
		}
		fieldsList.add(fieldInfo);

		// Requires the field for validation if it's not a collection.
		if (fieldInfo.getCollectionType().equals(CollectionType.SIMPLE)) {
			addRequiredField(fieldInfo);
		}
	}

	public void addFieldToInject(VariableElement fieldToInject,
			CollectionType collectionType) {
		FieldInfo info = new FieldInfo(fieldToInject, collectionType);
		addFieldToInject(info);
	}

	public HashMap<ClassInfo, List<FieldInfo>> getInjectionMap() {
		return injectionMap;
	}

	public List<FieldInfo> getRequiredFields() {
		return requiredFields;
	}

	public void addRequiredField(FieldInfo field) {
		this.requiredFields.add(field);
	}

	public HashMap<String, RevolverRegistryEntry> getManagedElements() {
		return managedElements;
	}

	public Collection<RevolverRegistryEntry> getManagedElementsList() {
		return managedElements.values();
	}

	public List<FieldInfo> getCollectionsToInject() {
		return collectionsToInject;
	}

	public void addCollectionToInject(VariableElement variable,
			CollectionType type) {
		FieldInfo fieldInfo = new FieldInfo(variable, type);
		this.collectionsToInject.add(fieldInfo);
	}

	/**
	 * Adds a generator method under Revolver context. The method is registered
	 * by adding the return type to the managed elements list. The method
	 * arguments are added to the required field list in order to validate them
	 * later.
	 * 
	 * @param element
	 *            the method to add.
	 * @param returnType
	 *            the element returned by the method.
	 */
	public void addManagedMethod(ExecutableElement element, Element returnType) {
		MethodInfo methodInfo = new MethodInfo(element, returnType);
		this.managedElements.put(methodInfo.getterMethodName(), methodInfo);
		this.managedMethods.add(methodInfo);

		// Registers the method arguments as required for validation.
		for (FieldInfo f : methodInfo.getParametersFieldInfo()) {
			addRequiredField(f);
		}

	}

	public List<MethodInfo> getManagedMethods() {
		return this.managedMethods;
	}
}
