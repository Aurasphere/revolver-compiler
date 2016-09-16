package co.aurasphere.revolver.environment;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

public class Messenger {

	private ProcessingEnvironment processingEnv;

	Messenger(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	public void error(ErrorMessageBundle messageBundle, Object... args) {
		String message = messageBundle.getValue();
		for (Object arg : args) {
			message = message.replaceFirst("\\{\\}", arg.toString());
		}
		processingEnv.getMessager().printMessage(Kind.ERROR, "Revolver compilation error: " + message);
	}

	/**
	 * Logs an exception interrupting the compilation.
	 * 
	 * @param message
	 *            the error messaage.
	 * @param exception
	 *            the exception to log.
	 */
	public void exception(ErrorMessageBundle message, Exception exception) {
		processingEnv.getMessager().printMessage(Kind.ERROR,
				message + "Nested exception is: " + exception.getMessage());
	}

}
