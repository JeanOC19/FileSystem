package exceptions;

/**
 * Thrown when a disk is trying to be accessed but does not exist.
 */

public class NonExistingDiskException extends RuntimeException {

	private static final long serialVersionUID = 1826996270164576528L;

	/**
	 * Constructs a NonExistingDiskException with no message.
	 */
	public NonExistingDiskException() {
		super();
	}

	/**
	 * Constructs a NonExistingDiskException with message specified.
	 * @param message message to be displayed
	 */
	public NonExistingDiskException(String message) {
		super(message);
	}	

}
