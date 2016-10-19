package co.aurasphere.revolver.codegen;

import static co.aurasphere.revolver.codegen.CodegenConstants.PROVIDER_CLASS_NAME;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import co.aurasphere.revolver.registry.ClassInfo;
import co.aurasphere.revolver.registry.CollectionType;
import co.aurasphere.revolver.registry.FieldInfo;
import co.aurasphere.revolver.registry.MethodInfo;
import co.aurasphere.revolver.registry.RevolverRegistryEntry;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class RevolverComponentProviderCodegen extends BaseCodegen {

	private static RevolverComponentProviderCodegen instance;

	private RevolverComponentProviderCodegen() {
		this.classBuilder = TypeSpec.classBuilder(PROVIDER_CLASS_NAME)
				.addModifiers(PUBLIC);
	}

	public static RevolverComponentProviderCodegen getInstance() {
		if (instance == null) {
			instance = new RevolverComponentProviderCodegen();
		}
		return instance;
	}

	/**
	 * Generates a method that returns the current instance of a class and the
	 * relative field.
	 * 
	 * @param klass
	 *            the class to provide.
	 */
	public void addSingleton(ClassInfo klass) {

		MethodSpec.Builder builder = getMethodBuilder(klass);

		// TODO : nella validazione si dovrebbe controllare che i nomi sono
		// sempre univoci.
		String singletonName = klass.getName() + "_instance";
		TypeName singletonClass = ClassName.get(klass.getTypeMirror());

		builder.beginControlFlow("if($L == null)", singletonName);

		int argNum = addConstructorArguments(klass, builder);

		// Creates the singleton and injects it.
		builder.addStatement("$L = new $T($L)", singletonName, singletonClass,
				getArgString(argNum))
				.addStatement("$T.$L($L)", CodegenConstants.INJECTOR_TYPE,
						CodegenConstants.INJECT_METHOD_NAME, singletonName)
				.endControlFlow().addStatement("return $L", singletonName);

		// Adds the method and the instance field to the class.
		classBuilder.addMethod(builder.build());
		classBuilder.addField(getSingletonInstanceField(singletonClass,
				singletonName));

	}

	/**
	 * Generates a method that returns a new instance of a class.
	 * 
	 * @param klass
	 *            the class to provide.
	 */
	public void addClassProviderMethod(ClassInfo klass) {

		MethodSpec.Builder builder = getMethodBuilder(klass);

		int argNum = addConstructorArguments(klass, builder);

		// Creates the object, injects it and adds the return statement.
		builder.addStatement("$T $L = new $T($L)", klass.getType(),
				CodegenConstants.RETURN_OBJECT_NAME, klass.getType(),
				getArgString(argNum))
				.addStatement("$T.$L($L)", CodegenConstants.INJECTOR_TYPE,
						CodegenConstants.INJECT_METHOD_NAME,
						CodegenConstants.RETURN_OBJECT_NAME)
				.addStatement("return $L", CodegenConstants.RETURN_OBJECT_NAME);

		// Adds this method to the class.
		classBuilder.addMethod(builder.build());

	}

	private static MethodSpec.Builder getMethodBuilder(
			RevolverRegistryEntry element) {
		TypeName providedClass = ClassName.get(element.getTypeMirror());
		// Builds the method signature.
		return MethodSpec.methodBuilder(element.getterMethodName())
				.returns(providedClass).addModifiers(Modifier.STATIC);
	}

	private static int addConstructorArguments(ClassInfo klass,
			MethodSpec.Builder builder) {
		// Adds the arguments for the class constructor.
		int argNum = 0;
		for (FieldInfo i : klass.getConstructorParameters()) {

			// Generates the getters for the construnctors.
			builder.addStatement("$T arg$L = $L()", i.getType(), argNum++,
					i.getterMethodName());
		}
		return argNum;
	}

	private static FieldSpec getSingletonInstanceField(TypeName singletonClass,
			String singletonName) {
		return FieldSpec
				.builder(singletonClass, singletonName, STATIC, PRIVATE)
				.build();
	}

	private static String getArgString(int argNum) {
		if (argNum == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < argNum; i++) {
			builder.append("arg").append(i).append(", ");
		}

		// Removes last comma.
		int len = builder.length();
		builder.delete(len - 2, len);
		return builder.toString();
	}

	public void addConstantProviderMethod(FieldInfo field) {

		MethodSpec.Builder builder = getMethodBuilder(field);

		// Adds the return statement with the constant.
		Object value = field.getType().getConstantValue();
		if (field.isString()) {
			builder.addStatement("return $S", value);
		} else if (field.isPrimitive()) {
			builder.addStatement("return $L", value);
		}

		// Adds this method to the class.
		classBuilder.addMethod(builder.build());
	}

	public void addCollectionProviderMethod(FieldInfo field) {
		Collection<RevolverRegistryEntry> managedElements = registry
				.getManagedElementsList();
		TypeMirror currentElementType = null;
		TypeMirror fieldType = field.getTypeMirror();
		TypeMirror collectionElementType = field
				.getCollectionElementTypeMirror();

		Types typeUtils = environment.getTypeUtils();

		List<RevolverRegistryEntry> validEntries = new ArrayList<RevolverRegistryEntry>();
		for (RevolverRegistryEntry entry : managedElements) {
			currentElementType = entry.getTypeMirror();
			// If a a managed element is a subtype or an implementation of a
			// required type, I add it to the generator class.
			if (typeUtils.isAssignable(currentElementType,
					collectionElementType)) {
				validEntries.add(entry);
			}
		}

		MethodSpec.Builder builder = getMethodBuilder(field);

		// TODO: should this class be a type?
		int counter = 0;
		for (RevolverRegistryEntry entry : validEntries) {
			builder.addStatement("$L arg$L = $L()", collectionElementType,
					counter++, entry.getterMethodName());
		}

		// Adds the entries found to the collection.
		CollectionType collectionType = field.getCollectionType();
		ClassName collectionClass = null;
		switch (collectionType) {
		case ARRAY:
			// Removes array square brackets.
			// TypeName arrayType = ArrayTypeName.get(fieldType);
			// returnType = returnType.replace("[]", "");
			// builder.addStatement("$L[] returnObject = new $L[$L]",
			// returnType,
			// returnType, counter);
			break;
		case LIST:
			collectionClass = ClassName.get(ArrayList.class);
			break;
		case SET:
			collectionClass = ClassName.get(HashSet.class);
			break;
		case QUEUE:
			collectionClass = ClassName.get(LinkedList.class);
			break;
		case SIMPLE:
			// Never happens at the moment.
			// TODO: future may inject if finds exactly one valid
			// implementation but has to be validated first.
			break;
		}

		if (collectionType.equals(CollectionType.ARRAY)) {
			// for (int i = 0; i < counter; i++) {
			// builder.addStatement("returnObject[$1L] = args$1L", i);
			// }
			builder.addStatement("return new $L{$L}", fieldType,
					getArgString(counter));
		} else {
			// If the collection has a generic, the class type will contain
			// that.
			if (collectionElementType != null) {
				TypeName generic = ClassName.get(collectionElementType);

				TypeName genericCollection = ParameterizedTypeName.get(
						collectionClass, generic);
				builder.addStatement("$T $L = new $T()", genericCollection,
						CodegenConstants.RETURN_OBJECT_NAME, genericCollection);
			} else {
				builder.addStatement("$T $L = new $T()", collectionClass,
						CodegenConstants.RETURN_OBJECT_NAME, collectionClass);
			}

			for (int i = 0; i < counter; i++) {
				builder.addStatement("$L.add(arg$L)",
						CodegenConstants.RETURN_OBJECT_NAME, i);
			}
			builder.addStatement("return $L",
					CodegenConstants.RETURN_OBJECT_NAME);
		}
		classBuilder.addMethod(builder.build());
	}

	/**
	 * Adds a method that provides a component using a generator method.
	 * 
	 * @param m
	 *            the generator method to use.
	 */
	public void addGeneratorProviderMethod(MethodInfo m) {
		// TODO Auto-generated method stub

		TypeName providedClass = ClassName.get(m.getReturnTypeMirror());
		// Builds the method signature.
		MethodSpec.Builder builder = MethodSpec
				.methodBuilder(m.getterMethodName()).returns(providedClass)
				.addModifiers(Modifier.STATIC);

		// If the generator method is a singleton, creates a static field for
		// keeping the instance and generates it.
		String singletonInstanceName = m.getName() + "_instance";
		if (m.isSingleton()) {
			// TODO: names must be unique.
			FieldSpec singletonInstanceField = getSingletonInstanceField(
					providedClass, singletonInstanceName);
			classBuilder.addField(singletonInstanceField);
			builder.beginControlFlow("if ($L == null )", singletonInstanceName);
		}

		// Injects the arguments.
		int argCounter = 0;
		for (FieldInfo f : m.getParametersFieldInfo()) {
			builder.addStatement("$T arg$L = $L()", f.getType(), argCounter++,
					f.getterMethodName());
		}

		if (m.isSingleton()) {
			builder.addStatement("$L = $T.$L($L)", singletonInstanceName,
					m.getParentClass(), m.getMethodName(),
					getArgString(argCounter)).endControlFlow()
					.addStatement("return $L", singletonInstanceName);

		} else {
			// Returns the result of the generator method.
			// TODO: at the moment generator methods must be static. for validation methods must be public
			builder.addStatement("return $T.$L($L)", m.getParentClass(),
					m.getMethodName(), getArgString(argCounter));
		}
		this.classBuilder.addMethod(builder.build());

	}
}
