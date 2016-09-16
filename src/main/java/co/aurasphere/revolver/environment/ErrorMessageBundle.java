package co.aurasphere.revolver.environment;

public enum ErrorMessageBundle {
	
	INJECT_ON_CONSTRUCTOR_E("@Inject not supported on constructor methods. Annotate the class [{}] with @RevolverContext or call Revolver.inject(this) instead."),
	
	INVALID_FIELD_NOT_FINAL_E("Fields annotated with @RevolverContext must be final: [{}]"),
	INVALID_FIELD_FINAL_E("Fields annotated with @Inject can't be final: [{}]"),
	
	INVALID_CONSTANT_NOT_PRIMITIVE_E("Fields annotated with @Constant must be String or primitive types initialized explicitly with a value: [{}]"),
	
	INVALID_CLASS_ENUM_E("Can't use @RevolverContext on enum [{}]"),
	INVALID_CLASS_INTERFACE_E("Can't use @RevolverContext on interface [{}]"),
	INVALID_CLASS_ABSTRACT_E("Can't use @RevolverContext on abstract class [{}]" ),
	INVALID_CLASS_NOT_PUBLIC_E("@RevolverContext can be used only on public classes: [{}]"),
	
	NO_PUBLIC_CONSTRUCTOR_E("No public constructor found for class [{}]"),
	VARARGS_NOT_SUPPORTED("Varargs constructors not supported yet. Please specify a different constructor for class [{}]"),
	SETTER_AND_FIELD_NOT_PUBLIC_E("Setter [{}] or field [{}] in class [{}] must be public."),
	SETTER_NOT_FOUND_E("No setter found for field [{}]. Expected signature: [{}]"), 
	SETTER_ARGS_MISMATCH_E("Setter arguments doesn't match for field [{}]. Expected only 1 argument with type [{}]."), 
	REQUIRED_COMPONENT_NOT_FOUND_E("Required component [{}] for class [{}] not found under Revolver context. Are you missing an annotation?"), 
	REQUIRED_COMPONENT_TYPE_MISMATCH_E("Mismatching type for component [{}] in class [{}]: expected [{}] but got [{}] from class [{}]."),
	INVALID_RETURN_TYPE_VOID_E("Generator method [{}] in class [{}] can't return void!"), 
	
	WRITING_CLASS_EX("Exception during Revolver class generation.");

	private String value;

	public String getValue() {
		return this.value;
	}

	private ErrorMessageBundle(String value) {
		this.value = value;
	}

}
