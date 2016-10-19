package co.aurasphere.revolver.codegen;

import com.squareup.javapoet.ClassName;

public class CodegenConstants {
	
	static final String INJECT_METHOD_NAME = "inject";

	static final String LOCAL_INSTANCE_NAME = "instance";

	static final String GENERATED_CLASS_PACKAGE = "co.aurasphere.revolver";
	
	static final String INJECTOR_CLASS_NAME = "Revolver";
	
	static final String PROVIDER_CLASS_NAME = "RevolverComponentProvider";
	
	static final String RETURN_OBJECT_NAME = "returnObject";
	
	static final String LOCAL_PROVIDER_NAME = "componentProvider";
	
	static final ClassName INJECTOR_TYPE = ClassName.get(GENERATED_CLASS_PACKAGE, INJECTOR_CLASS_NAME);

	static final ClassName PROVIDER_TYPE = ClassName.get(GENERATED_CLASS_PACKAGE, PROVIDER_CLASS_NAME);
}
