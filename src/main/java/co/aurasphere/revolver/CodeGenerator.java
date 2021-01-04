package co.aurasphere.revolver;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang.StringUtils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import co.aurasphere.revolver.model.BaseRevolverRegistryEntry;
import co.aurasphere.revolver.model.ClassRegistryEntry;
import co.aurasphere.revolver.model.CollectionType;
import co.aurasphere.revolver.model.FieldRegistryEntry;
import co.aurasphere.revolver.model.MethodRegistryEntry;

public class CodeGenerator {

	private static final String INJECT_METHOD_NAME = "inject";
	private static final String GENERATED_CLASS_PACKAGE = "co.aurasphere.revolver";
	private static final String INJECTOR_CLASS_NAME = "Revolver";
	private static final String PROVIDER_CLASS_NAME = "RevolverComponentProvider";

	public void generateInjector(Filer filer) {
		String localInstanceName = "instance";
		TypeSpec.Builder injectorClassBuilder = TypeSpec.classBuilder(INJECTOR_CLASS_NAME)
				.addModifiers(Modifier.PUBLIC);
		// Adds a generic method with empty body used as interface.
		injectorClassBuilder
				.addMethod(MethodSpec.methodBuilder(INJECT_METHOD_NAME).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						.returns(TypeName.VOID).addParameter(Object.class, localInstanceName).build());

		ClassName providerClassName = ClassName.get(GENERATED_CLASS_PACKAGE, PROVIDER_CLASS_NAME);
		HashMap<ClassRegistryEntry, List<FieldRegistryEntry>> injectionMap = RevolverCompilationEnvironment.INSTANCE
				.getRegistry().getInjectionMap();
		Set<ClassRegistryEntry> classesToInject = injectionMap.keySet();

		// Generates one method for each class that needs injection
		for (ClassRegistryEntry c : classesToInject) {
			TypeName classType = ClassName.get(c.getType());
			List<FieldRegistryEntry> fieldsToInject = injectionMap.get(c);

			// Builds the method header.
			MethodSpec.Builder builder = MethodSpec.methodBuilder(INJECT_METHOD_NAME)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.VOID)
					.addParameter(classType, localInstanceName);

			// For each field to inject, creates a setter.
			for (int argCounter = 0; argCounter < fieldsToInject.size(); argCounter++) {
				FieldRegistryEntry currentArg = fieldsToInject.get(argCounter);

				// Gets the argument.
				TypeName fieldClass = ClassName.get(currentArg.getTypeMirror());
				builder.addStatement("$T arg$L = $T.$L()", fieldClass, argCounter, providerClassName,
						currentArg.getterMethodName());

				// If the field is public, sets is, otherwise calls the setter.
				if (currentArg.isPublic()) {
					builder.addStatement("$L.$L = arg$L", localInstanceName, currentArg.getType(), argCounter);
				} else {
					builder.addStatement("$L.$L(arg$L)", localInstanceName, currentArg.setterName(), argCounter);
				}
			}

			// Adds the method to the class.
			injectorClassBuilder.addMethod(builder.build());
		}
		writeClass(injectorClassBuilder, filer);
	}

	public void generateComponentProvider(Filer filer, Types typeUtils) {
		String returnObjectName = "returnObject";
		Set<String> generatedSingletonNames = new HashSet<>();
		ClassName injectorType = ClassName.get(GENERATED_CLASS_PACKAGE, INJECTOR_CLASS_NAME);
		TypeSpec.Builder componentProviderClassBuilder = TypeSpec.classBuilder(PROVIDER_CLASS_NAME)
				.addModifiers(PUBLIC);

		// Creates the managed class getters.
		for (ClassRegistryEntry c : RevolverCompilationEnvironment.INSTANCE.getRegistry().getManagedClasses()) {
			// Builds the method signature.
			TypeName providedClass = ClassName.get(c.getTypeMirror());
			MethodSpec.Builder builder = MethodSpec.methodBuilder(c.getterMethodName()).returns(providedClass)
					.addModifiers(Modifier.STATIC);

			// Validates the singleton instance name
			String singletonName = checkDuplicatedSingletonName(c, generatedSingletonNames);
			TypeName singletonClass = ClassName.get(c.getTypeMirror());

			builder.beginControlFlow("if($L == null)", singletonName);

			// Adds the arguments for the class constructor.
			AtomicInteger argNum = new AtomicInteger();
			c.getConstructorParameters().forEach(i -> builder.addStatement("$T arg$L = $L()", i.getType(),
					argNum.getAndIncrement(), i.getterMethodName()));
			String argString = IntStream.range(0, argNum.get()).mapToObj(i -> "arg" + i + ", ")
					.collect(Collectors.joining());
			argString = StringUtils.removeEnd(argString, ", ");

			// Creates the singleton and injects it.
			builder.addStatement("$L = new $T($L)", singletonName, singletonClass, argString)
					.addStatement("$T.$L($L)", injectorType, INJECT_METHOD_NAME, singletonName).endControlFlow()
					.addStatement("return $L", singletonName);

			// Adds the method and the instance field to the class.
			componentProviderClassBuilder.addMethod(builder.build());
			componentProviderClassBuilder
					.addField(FieldSpec.builder(singletonClass, singletonName, STATIC, PRIVATE).build());
		}

		// Creates the collections getters.
		for (FieldRegistryEntry f : RevolverCompilationEnvironment.INSTANCE.getRegistry()
				.getDeduplicatedCollectionsToInject()) {
			TypeMirror collectionElementType = f.getCollectionElementTypeMirror();

			// Gets all the injectable matches (subtypes or implementations) for a
			// collection.
			List<BaseRevolverRegistryEntry<?>> validEntries = RevolverCompilationEnvironment.INSTANCE.getRegistry()
					.getManagedElementsList().parallelStream()
					.filter(entry -> typeUtils.isAssignable(entry.getTypeMirror(), collectionElementType))
					.collect(Collectors.toList());

			// Builds the method header-
			MethodSpec.Builder builder = MethodSpec.methodBuilder(f.getterMethodName())
					.returns(ClassName.get(f.getTypeMirror())).addModifiers(Modifier.STATIC);

			// Generates an assignment for each candidate component.
			AtomicInteger argCounter = new AtomicInteger();
			validEntries.forEach(entry -> builder.addStatement("$L arg$L = $L()", collectionElementType,
					argCounter.getAndIncrement(), entry.getterMethodName()));

			// Adds the entries found to the collection.
			CollectionType collectionType = f.getCollectionType();
			if (collectionType.equals(CollectionType.ARRAY)) {
				String argString = IntStream.range(0, argCounter.get()).mapToObj(i -> "arg" + i + ", ")
						.collect(Collectors.joining());
				argString = StringUtils.removeEnd(argString, ", ");
				builder.addStatement("return new $L{$L}", f.getTypeMirror(), argString);
			} else {
				// Handles parameterized and unparameterized collections.
				ClassName collectionClass = collectionType.getCollectionClass();
				if (collectionElementType != null) {
					TypeName parameterType = ClassName.get(collectionElementType);
					TypeName parameterizedCollection = ParameterizedTypeName.get(collectionClass, parameterType);
					builder.addStatement("$T $L = new $T()", parameterizedCollection, returnObjectName, parameterizedCollection);
				} else {
					builder.addStatement("$T $L = new $T()", collectionClass, returnObjectName, collectionClass);
				}

				IntStream.range(0, argCounter.get())
						.forEach(i -> builder.addStatement("$L.add(arg$L)", returnObjectName, i));
				builder.addStatement("return $L", returnObjectName);
			}
			componentProviderClassBuilder.addMethod(builder.build());
		}

		// Creates the generator methods getters.
		for (MethodRegistryEntry m : RevolverCompilationEnvironment.INSTANCE.getRegistry().getManagedMethods()) {
			TypeName providedClass = ClassName.get(m.getReturnTypeMirror());
			// Builds the method signature.
			MethodSpec.Builder builder = MethodSpec.methodBuilder(m.getterMethodName()).returns(providedClass)
					.addModifiers(Modifier.STATIC);

			// Creates a static field for keeping the instance and generates it.
			String singletonInstanceName = checkDuplicatedSingletonName(m, generatedSingletonNames);
			FieldSpec singletonInstanceField = FieldSpec.builder(providedClass, singletonInstanceName, STATIC, PRIVATE)
					.build();
			componentProviderClassBuilder.addField(singletonInstanceField);
			builder.beginControlFlow("if ($L == null )", singletonInstanceName);

			// Injects the arguments.
			AtomicInteger argCounter = new AtomicInteger();
			m.getParametersFieldInfo().forEach(f -> builder.addStatement("$T arg$L = $L()", f.getType(), argCounter.getAndIncrement(), f.getterMethodName()));
		
			String argString = IntStream.range(0, argCounter.get()).mapToObj(i -> "arg" + i + ", ")
					.collect(Collectors.joining());
			argString = StringUtils.removeEnd(argString, ", ");
			builder.addStatement("$L = $L().$L($L)", singletonInstanceName, m.getParentClassInfo().getterMethodName(),
					m.getMethodName(), argString).endControlFlow().addStatement("return $L", singletonInstanceName);
			componentProviderClassBuilder.addMethod(builder.build());
		}

		writeClass(componentProviderClassBuilder, filer);
	}

	private String checkDuplicatedSingletonName(BaseRevolverRegistryEntry<?> entry, Set<String> generatedSingletonNames) {
		String singletonName = entry.getName() + "_instance";
		if (generatedSingletonNames.contains(singletonName)) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"Duplicate component found. Use @Named if you need multiple objects of the same type",
					entry.getElement());
		}
		generatedSingletonNames.add(singletonName);
		return singletonName;
	}

	private void writeClass(TypeSpec.Builder classBuilder, Filer filer) {
		JavaFile file = JavaFile.builder(GENERATED_CLASS_PACKAGE, classBuilder.build()).build();
		try {
			file.writeTo(filer);
		} catch (IOException e) {
			RevolverCompilationEnvironment.INSTANCE.printMessage(Kind.ERROR,
					"Exception during Revolver class generation: " + e.getMessage(), null);
		}
	}

}