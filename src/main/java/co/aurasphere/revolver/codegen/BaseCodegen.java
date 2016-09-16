package co.aurasphere.revolver.codegen;

import co.aurasphere.revolver.environment.RevolverBaseBean;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class BaseCodegen extends RevolverBaseBean{

	protected TypeSpec.Builder classBuilder;

	public JavaFile build() {
		return JavaFile.builder(CodegenConstants.GENERATED_CLASS_PACKAGE,
				classBuilder.build()).build();
	}

}
