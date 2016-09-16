package co.aurasphere.revolver.codegen;

import java.util.List;

import javax.lang.model.element.Modifier;

import co.aurasphere.revolver.registry.ClassInfo;
import co.aurasphere.revolver.registry.FieldInfo;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class RevolverInjectorCodegen extends BaseCodegen {

	private static RevolverInjectorCodegen instance;

	private RevolverInjectorCodegen() {
		this.classBuilder = TypeSpec.classBuilder(
				CodegenConstants.INJECTOR_CLASS_NAME).addModifiers(
				Modifier.PUBLIC);
		
		// Adds a generic method with empty body used as interface.
		this.classBuilder.addMethod(getInjectMethodBaseStub().addParameter(
				Object.class, CodegenConstants.LOCAL_INSTANCE_NAME).build());

	}

	public static RevolverInjectorCodegen getInstance() {
		if (instance == null) {
			instance = new RevolverInjectorCodegen();
		}
		return instance;
	}

	/**
	 * Generates a method that populates the current instance of a class
	 * injecting the requested fields.
	 * 
	 * @param klass
	 *            the class to provide.
	 */
	public void addInjectMethod(ClassInfo klass, List<FieldInfo> fieldsToInject) {

		MethodSpec.Builder builder = getInjectMethodStub(klass);
		TypeName fieldClass;
		int argCounter = 0;

		// For each field to inject, creates a setter.
		for (FieldInfo i : fieldsToInject) {

			// If the field is public, sets is, otherwise calls the setter.
			// TODO: e se è una collection di array o un array di collection?

			// Gets the argument.
			fieldClass = ClassName.get(i.getType().asType());
			builder.addStatement("$L arg$L = $L.$L()",
					fieldClass, argCounter,
					CodegenConstants.PROVIDER_CLASS_NAME, i.getterMethodName());
			// TODO: PROVIDER_CLASS should be a class not a String.

			// Sets the argument.
			if (i.isPublicAttr()) {
				builder.addStatement("$L.$L = arg$L",
						CodegenConstants.LOCAL_INSTANCE_NAME, i.getType(),
						argCounter);
			} else {
				// TODO: e se il setter ha due o piu argomenti?
				builder.addStatement("$L.$L(arg$L)",
						CodegenConstants.LOCAL_INSTANCE_NAME, i.setterName(),
						argCounter);
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
		classBuilder.addMethod(builder.build());

	}

	/**
	 * Gets a base method builder for an injector.
	 * 
	 * @return
	 */
	private MethodSpec.Builder getInjectMethodBaseStub() {
		return MethodSpec.methodBuilder(CodegenConstants.INJECT_METHOD_NAME)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(TypeName.VOID);
	}

	private MethodSpec.Builder getInjectMethodStub(ClassInfo klass) {
		TypeName classType = ClassName.get(klass.getType());
		return getInjectMethodBaseStub().addParameter(classType,
				CodegenConstants.LOCAL_INSTANCE_NAME);
	}

}
