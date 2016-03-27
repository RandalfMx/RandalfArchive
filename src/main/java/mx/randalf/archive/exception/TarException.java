/**
 * 
 */
package mx.randalf.archive.exception;

/**
 * @author massi
 *
 */
public class TarException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1735556465296531650L;

	/**
	 * @param message
	 */
	public TarException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TarException(String message, Throwable cause) {
		super(message, cause);
	}

}
