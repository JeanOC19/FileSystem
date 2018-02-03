package exceptions;

/**
 * Thrown when a block is given but has a null reference or invalid values.
 */

public class InvalidBlockException extends RuntimeException {
	
	private static final long serialVersionUID = 1286497411777506847L;

	/**
	 * Constructs an InvalidBlockException with no detail message.
	 */
	public InvalidBlockException() {
		super();
	}

	/**
	 * Constructs an InvalidBlockException with the message specified.
	 * @param message message to be displayed
	 */
	public InvalidBlockException(String message) {
		super(message);
	}
	
}
