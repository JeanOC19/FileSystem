package exceptions;

/**
 * Thrown when an existing disk wants to be created.
 */

public class ExistingDiskException extends RuntimeException {

	private static final long serialVersionUID = 8573321031281248088L;
	
	/**
	 * Constructs an ExistingDiskException with no detail message.
	 */
	public ExistingDiskException() {
		super();
	}

	/**
	 * Constructs an ExistingDiskException with the message specified
	 * @param msg message to be shown
	 */
	public ExistingDiskException(String msg) {
		super(msg);
	}	
	
}
