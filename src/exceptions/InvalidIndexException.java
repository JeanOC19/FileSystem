package exceptions;

/**
 * Thrown when an invalid index is given to access a list element.
 */

public class InvalidIndexException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs an InvalidIndexException with the specified 
	 * message.
	 * @param string message to be displayed.
	 */
	public InvalidIndexException(String string) {
		super(string);
	}
}
