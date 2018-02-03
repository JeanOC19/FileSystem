package exceptions;

/**
 * Thrown when a block with an invalid index is trying to be accessed. 
 */
public class InvalidBlockNumberException extends RuntimeException {

	private static final long serialVersionUID = 7285933491512462899L;

	/**
	 * Constructs an InvalidBlockNumberException with no detail message.
	 */
	public InvalidBlockNumberException() {
		super();
	}

	/**
	 * Constructs an InvalidBlockNumberException with message given.
	 * @param msg message to be displayed
	 */
	public InvalidBlockNumberException(String msg) {
		super(msg);
	}
	
}
