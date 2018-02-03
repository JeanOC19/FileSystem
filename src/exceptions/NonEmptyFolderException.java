package exceptions;
/**
 * Thrown when a nonempty folder wants to be deleted.
 * @author jeano
 *
 */
public class NonEmptyFolderException extends RuntimeException {

	private static final long serialVersionUID = -7157973649681408776L;
	
	/**
	 * Constructs an NonEmptyFolderException with no detail message.
	 */
	public NonEmptyFolderException() {
		super();
	}

	/**
	 * Constructs an NonEmptyFolderException with the message specified.
	 * @param message message to be displayed
	 */
	public NonEmptyFolderException(String message) {
		super(message);
	}

}
