package co.aurasphere.revolver.codegen;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

import co.aurasphere.revolver.environment.ErrorMessageBundle;
import co.aurasphere.revolver.environment.RevolverBaseBean;
import co.aurasphere.revolver.registry.ClassInfo;
import co.aurasphere.revolver.registry.FieldInfo;
import co.aurasphere.revolver.registry.MethodInfo;

import com.squareup.javapoet.JavaFile;

public class CodegenController extends RevolverBaseBean{
	
	public static void generateComponentProvider(Filer filer){
		RevolverComponentProviderCodegen generator = RevolverComponentProviderCodegen.getInstance();
		
		// Creates the managed class getters.
		for(ClassInfo c : registry.getManagedClasses()){
			if (c.isSingleton()){
				generator.addSingleton(c);
			} else {
				generator.addClassProviderMethod(c);
			}
		}
		
		// Creates the constants getters.
		for(FieldInfo f : registry.getManagedConstants()){
			generator.addConstantProviderMethod(f);
		}
		
		// Creates the collections getters.
		for(FieldInfo f : registry.getCollectionsToInject()){
			generator.addCollectionProviderMethod(f);
		}
		
		// Creates the generator methods getters.
		for(MethodInfo m : registry.getManagedMethods()){
			generator.addGeneratorProviderMethod(m);
		}
		
		writeClass(generator, filer);
	}
	
	public static void generateInjector(Filer filer){
		RevolverInjectorCodegen generator = RevolverInjectorCodegen.getInstance();
		
		HashMap<ClassInfo, List<FieldInfo>> injectionMap = registry.getInjectionMap();
		Set<ClassInfo> classesToInject = injectionMap.keySet();
		for(ClassInfo c : classesToInject){
			generator.addInjectMethod(c, injectionMap.get(c));
		}
		writeClass(generator, filer);
	}
	
	private static void writeClass(BaseCodegen generator, Filer filer){
		JavaFile file = generator.build();
		try {
			file.writeTo(filer);
		} catch (IOException e) {
			messenger().exception(ErrorMessageBundle.WRITING_CLASS_EX, e);
		}
	}

}
