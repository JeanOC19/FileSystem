package exceptions;

/**
 * Thrown whenever a file has the maximum number of data blocks 
 * and extra blocks want to be added.
 * @author jeano
 *
 */
public class FullFileException extends RuntimeException {

	/**
	 * Constructs a FullFileException with the specified message.
	 * @param string message to be displayed.
	 */
	public FullFileException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
