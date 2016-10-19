package co.aurasphere.revolver.environment;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

public class Messenger {

	private ProcessingEnvironment processingEnv;

	Messenger(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}
	
	private String replaceArguments(String message, Object[] args){
		String resultMessage = message;
		for (Object arg : args) {
			resultMessage = resultMessage.replaceFirst("\\{\\}", arg.toString());
		}
		return resultMessage;
	}

	public void error(ErrorMessageBundle messageBundle, Object... args) {
		String message = replaceArguments(messageBundle.getValue(), args);
		processingEnv.getMessager().printMessage(Kind.ERROR, "Revolver compilation error: " + message);
	}

	/**
	 * Logs an exception interrupting the compilation.
	 * 
	 * @param message
	 *            the error message.
	 * @param exception
	 *            the exception to log.
	 */
	public void exception(ErrorMessageBundle message, Exception exception) {
		processingEnv.getMessager().printMessage(Kind.ERROR,
				message.getValue() + " Nested exception is: " + exception.getMessage());
	}

	public void note(String message) {
		processingEnv.getMessager().printMessage(Kind.NOTE, message);
	}

	public void warning(ErrorMessageBundle messageBundle, Object... args) {
		String message = replaceArguments(messageBundle.getValue(), args);
		processingEnv.getMessager().printMessage(Kind.WARNING, "Revolver compilation warning: " + message);
	}

}
