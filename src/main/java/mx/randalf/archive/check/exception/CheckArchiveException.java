/**
 * 
 */
package mx.randalf.archive.check.exception;

/**
 * @author massi
 *
 */
public class CheckArchiveException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8984077610515465L;

	/**
	 * @param message
	 */
	public CheckArchiveException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CheckArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

}
