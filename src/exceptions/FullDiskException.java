package exceptions;

/**
 * Thrown when a file wants to be added and there is not enough space 
 * available in disk.
 * @author jeano
 *
 */
public class FullDiskException extends RuntimeException {
	
	private static final long serialVersionUID = 4693346754730484078L;
	
	/**
	 * Constructs a FullDiskException with the specified message.
	 * @param string message to be displayed.
	 */
	public FullDiskException(String string) {
		super(string);
	}

}
