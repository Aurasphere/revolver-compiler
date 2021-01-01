package co.aurasphere.revolver;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import co.aurasphere.revolver.model.ClassRegistryEntry;
import co.aurasphere.revolver.model.CollectionType;
import co.aurasphere.revolver.model.FieldRegistryEntry;
import co.aurasphere.revolver.model.MethodRegistryEntry;
import co.aurasphere.revolver.model.BaseRevolverRegistryEntry;

public class CodeGenerator {

	static final String INJECT_METHOD_NAME = "inject";
	static final String LOCAL_INSTANCE_NAME = "instance";
	static final String GENERATED_CLASS_PACKAGE = "co.aurasphere.revolver";
	static final String INJECTOR_CLASS_NAME = "Revolver";
	static final String PROVIDER_CLASS_NAME = "RevolverComponentProvider";
	static final String RETURN_OBJECT_NAME = "returnObject";
	static final String LOCAL_PROVIDER_NAME = "componentProvider";
	static final ClassName INJECTOR_TYPE = ClassName.get(GENERATED_CLASS_PACKAGE, INJECTOR_CLASS_NAME);
	static final ClassName PROVIDER_TYPE = ClassName.get(GENERATED_CLASS_PACKAGE, PROVIDER_CLASS_NAME);

	private TypeSpec.Builder injectorClassBuilder;
	private TypeSpec.Builder componentProviderClassBuilder;

	public CodeGenerator() {
		this.injectorClassBuilder = TypeSpec.classBuilder(INJECTOR_CLASS_NAME).addModifiers(Modifier.PUBLIC);
		// Adds a generic method with empty body used as interface.
		this.injectorClassBuilder
				.addMethod(getInjectMethodBaseStub().addParameter(Object.class, LOCAL_INSTANCE_NAME).build());

		this.componentProviderClassBuilder = TypeSpec.classBuilder(PROVIDER_CLASS_NAME).addModifiers(PUBLIC);
	}

	public void generateComponentProvider(Filer filer, Types typeUtils) {
		// Creates the managed class getters.
		for (ClassRegistryEntry c : RevolverCompilationEnvironment.INSTANCE.getRegistry().getManagedClasses()) {
			MethodSpec.Builder builder = getMethodBuilder(c);

			// TODO : nella validazione si dovrebbe controllare che i nomi sono
			// sempre univoci., in realtà ci vuole un meccanismo di sovrascrittura
			// controllata
			String singletonName = c.getName() + "_instance";
			TypeName singletonClass = ClassName.get(c.getTypeMirror());

			builder.beginControlFlow("if($L == null)", singletonName);

			// Adds the arguments for the class constructor.
			int argNum = 0;
			for (FieldRegistryEntry i : c.getConstructorParameters()) {

				// Generates the getters for the constructors.
				builder.addStatement("$T arg$L = $L()", i.getType(), argNum++, i.getterMethodName());
			}

			// Creates the singleton and injects it.
			builder.addStatement("$L = new $T($L)", singletonName, singletonClass, getArgString(argNum))
					.addStatement("$T.$L($L)", INJECTOR_TYPE, INJECT_METHOD_NAME, singletonName).endControlFlow()
					.addStatement("return $L", singletonName);

			// Adds the method and the instance field to the class.
			componentProviderClassBuilder.addMethod(builder.build());
			componentProviderClassBuilder.addField(getSingletonInstanceField(singletonClass, singletonName));
		}

		// Creates the collections getters.
		for (FieldRegistryEntry f : RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getDeduplicatedCollectionsToInject()) {
			addCollectionProviderMethod(f, typeUtils);
		}

		// Creates the generator methods getters.
		for (MethodRegistryEntry m : RevolverCompilationEnvironment.INSTANCE.getRegistry().getManagedMethods()) {
			addGeneratorProviderMethod(m);
		}

		writeClass(componentProviderClassBuilder, filer);
	}

	public void generateInjector(Filer filer) {
		HashMap<ClassRegistryEntry, List<FieldRegistryEntry>> injectionMap = RevolverCompilationEnvironment.INSTANCE
				.getRegistry().getInjectionMap();
		Set<ClassRegistryEntry> classesToInject = injectionMap.keySet();
		for (ClassRegistryEntry c : classesToInject) {
			addInjectMethod(c, injectionMap.get(c));
		}
		writeClass(injectorClassBuilder, filer);
	}

	private void writeClass(TypeSpec.Builder classBuilder, Filer filer) {
		JavaFile file = JavaFile.builder(GENERATED_CLASS_PACKAGE, classBuilder.build()).build();
		try {
			file.writeTo(filer);
		} catch (IOException e) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"Exception during Revolver class generation: " + e.getMessage());
		}
	}

	// ----- COMPONENT PROVIDER

	private static MethodSpec.Builder getMethodBuilder(BaseRevolverRegistryEntry element) {
		TypeName providedClass = ClassName.get(element.getTypeMirror());
		// Builds the method signature.
		return MethodSpec.methodBuilder(element.getterMethodName()).returns(providedClass)
				.addModifiers(Modifier.STATIC);
	}

	private static FieldSpec getSingletonInstanceField(TypeName singletonClass, String singletonName) {
		return FieldSpec.builder(singletonClass, singletonName, STATIC, PRIVATE).build();
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

	public void addCollectionProviderMethod(FieldRegistryEntry field, Types typeUtils) {
		Collection<BaseRevolverRegistryEntry> managedElements = RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getManagedElementsList();
		TypeMirror currentElementType = null;
		TypeMirror fieldType = field.getTypeMirror();
		TypeMirror collectionElementType = field.getCollectionElementTypeMirror();

		List<BaseRevolverRegistryEntry> validEntries = new ArrayList<BaseRevolverRegistryEntry>();
		for (BaseRevolverRegistryEntry entry : managedElements) {
			currentElementType = entry.getTypeMirror();

			// This happens if the type is a primitive, which is not supported. In this case
			// you can use the Object counterpart (e.g. int -> Integer)
			if (currentElementType == null) {
				RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
						"Primitive types are not supported. Use the object counterpart (e.g. int -> Integer). Offending field ["
								+ entry.getName() + "].",
						entry.getType());

				// Here we just return since there's an error already.
				return;
			}

			// If a a managed element is a subtype or an implementation of a
			// required type, I add it to the generator class.
			if (typeUtils.isAssignable(currentElementType, collectionElementType)) {
				validEntries.add(entry);
			}
		}

		MethodSpec.Builder builder = getMethodBuilder(field);
		// TODO: should this class be a type?
		int counter = 0;
		for (BaseRevolverRegistryEntry entry : validEntries) {
			builder.addStatement("$L arg$L = $L()", collectionElementType, counter++, entry.getterMethodName());
		}

		CollectionType collectionType = field.getCollectionType();
		// Adds the entries found to the collection.
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
		case SINGLE:
			break;
		}

		if (collectionType.equals(CollectionType.ARRAY)) {
			// for (int i = 0; i < counter; i++) {
			// builder.addStatement("returnObject[$1L] = args$1L", i);
			// }
			builder.addStatement("return new $L{$L}", fieldType, getArgString(counter));
		} else {
			// If the collection has a generic, the class type will contain
			// that.
			if (collectionElementType != null) {
				TypeName generic = ClassName.get(collectionElementType);

				TypeName genericCollection = ParameterizedTypeName.get(collectionClass, generic);
				builder.addStatement("$T $L = new $T()", genericCollection, RETURN_OBJECT_NAME, genericCollection);
			} else {
				builder.addStatement("$T $L = new $T()", collectionClass, RETURN_OBJECT_NAME, collectionClass);
			}

			for (int i = 0; i < counter; i++) {
				builder.addStatement("$L.add(arg$L)", RETURN_OBJECT_NAME, i);
			}
			builder.addStatement("return $L", RETURN_OBJECT_NAME);
		}
		componentProviderClassBuilder.addMethod(builder.build());
	}

	/**
	 * Adds a method that provides a component using a generator method.
	 * 
	 * @param m the generator method to use.
	 */
	public void addGeneratorProviderMethod(MethodRegistryEntry m) {
		TypeName providedClass = ClassName.get(m.getReturnTypeMirror());
		// Builds the method signature.
		MethodSpec.Builder builder = MethodSpec.methodBuilder(m.getterMethodName()).returns(providedClass)
				.addModifiers(Modifier.STATIC);

		// Creates a static field for keeping the instance and generates it.
		String singletonInstanceName = m.getName() + "_instance";
		// TODO: names must be unique.
		FieldSpec singletonInstanceField = getSingletonInstanceField(providedClass, singletonInstanceName);
		componentProviderClassBuilder.addField(singletonInstanceField);
		builder.beginControlFlow("if ($L == null )", singletonInstanceName);

		// Injects the arguments.
		int argCounter = 0;
		for (FieldRegistryEntry f : m.getParametersFieldInfo()) {
			builder.addStatement("$T arg$L = $L()", f.getType(), argCounter++, f.getterMethodName());
		}
		builder.addStatement("$L = $L().$L($L)", singletonInstanceName, m.getParentClassInfo().getterMethodName(), m.getMethodName(), getArgString(argCounter))
				.endControlFlow().addStatement("return $L", singletonInstanceName);
		this.componentProviderClassBuilder.addMethod(builder.build());
	}

	// INJECTOR -----------

	/**
	 * Generates a method that populates the current instance of a class injecting
	 * the requested fields.
	 * 
	 * @param klass the class to provide.
	 */
	public void addInjectMethod(ClassRegistryEntry klass, List<FieldRegistryEntry> fieldsToInject) {

		MethodSpec.Builder builder = getInjectMethodStub(klass);
		TypeName fieldClass;
		int argCounter = 0;

		// For each field to inject, creates a setter.
		for (FieldRegistryEntry i : fieldsToInject) {

			// If the field is public, sets is, otherwise calls the setter.
			// TODO: e se è una collection di array o un array di collection?

			// Gets the argument.
			fieldClass = ClassName.get(i.getTypeMirror());
			builder.addStatement("$T arg$L = $T.$L()", fieldClass, argCounter, PROVIDER_TYPE, i.getterMethodName());
			// TODO: PROVIDER_CLASS should be a class not a String.

			// Sets the argument.
			if (i.isPublic()) {
				builder.addStatement("$L.$L = arg$L", LOCAL_INSTANCE_NAME, i.getType(), argCounter);
			} else {
				// TODO: e se il setter ha due o piu argomenti?
				builder.addStatement("$L.$L(arg$L)", LOCAL_INSTANCE_NAME, i.setterName(), argCounter);
			}

//			// Recursively injects arguments.
//			if (i.getCollectionType().equals(CollectionType.SIMPLE)) {
//				builder.addStatement("inject(arg$L)", argCounter);
//			} else {
//				TypeMirror collectionElementTypeMirror = i
//						.getCollectionElementTypeMirror();
//				TypeName elementType = ClassName
//						.get(collectionElementTypeMirror);
//				builder.beginControlFlow("for($T element : arg$L)",
//						elementType, argCounter)
//						.addStatement("inject(element)").endControlFlow();
//			}

			// Increments the argument counter.
			argCounter++;
		}

		// Adds the method to the class.
		injectorClassBuilder.addMethod(builder.build());

	}

	/**
	 * Gets a base method builder for an injector.
	 * 
	 * @return
	 */
	private MethodSpec.Builder getInjectMethodBaseStub() {
		return MethodSpec.methodBuilder(INJECT_METHOD_NAME).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(TypeName.VOID);
	}

	private MethodSpec.Builder getInjectMethodStub(ClassRegistryEntry klass) {
		TypeName classType = ClassName.get(klass.getType());
		return getInjectMethodBaseStub().addParameter(classType, LOCAL_INSTANCE_NAME);
	}

}