package diskUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;

/**
 * Some utility methods to copy/get int values, byte values and char values
 * to/from VirtualDiskBlock or array of bytes.
 * 
 * @author pedroirivera-vega
 */
public class Utils {
	public static final int INTSIZE = 4; 

	/**
	 * Determines if given value is a power of 2.
	 * @param value the number that will be examined.
	 * @return true if value is power of 2, false otherwise.
	 */
	public static boolean powerOf2(int value) {
		//Check for 2^0 special case
		if(value == 1)
			return true;
		/* Divides value by 2 repeatedly. The while loop 
		 * stops when value is less than 2 or an odd number.
		 * If that is the case, the number is not a power of 2.
		 * */
		while(value > 2 && (value % 2 == 0)) {
			value = value / 2;
		}

		return value == 2;
	}

	/**
	 * Copy an integer value into four consecutive bytes in a block.
	 * @param vdb The block where the integer value is copied.
	 * @param index The index of the first byte in the block that
	 * shall be written. The number is stored in four bytes whose 
	 * indexes are: index, index+1, index+2, and index+3, starting
	 * from the less significant byte in the number up to the most
	 * significant. 
	 * @param value The integer value to be written in block. 
	 */
	public static void copyIntToBlock(VirtualDiskBlock vdb, int index, int value) { 
		for (int i = INTSIZE-1; i >= 0; i--) { 
			vdb.setElement(index+i, (byte) (value & 0x000000ff)); 	
			value = value >> 8; 
		}
	}

	/**
	 * Extracts an integer value from four consecutive bytes in a block. 
	 * @param vdb The block. 
	 * @param index The index in block of the less significant byte of the  
	 * number to extract. 
	 * @return The value extracted from bytes index+3, index+2, index+1, and index. 
	 * From most significant to less significant bytes of the number's four bytes. 
	 */
	public static int getIntFromBlock(VirtualDiskBlock vdb, int index) {  
		int value = 0; 
		int lSB; 
		for (int i=0; i < INTSIZE; i++) { 
			value = value << 8; 
			lSB = 0x000000ff & vdb.getElement(index + i);
			value = value | lSB; 
		}
		return value; 
	}

	/**
	 * Copy an integer value into four consecutive bytes in an array of bytes.
	 * @param b The array where the integer value is copied.
	 * @param index The index of the first byte in the b that
	 * shall be written. The number is stored in four bytes whose 
	 * indexes are: index, index+1, index+2, and index+3, starting
	 * from the less significant byte in the number up to the most
	 * significant. 
	 * @param value The integer value to be written in the array. 
	 */
	public static void copyIntToBytesArray(byte[] b, int index, int value) { 
		for (int i = INTSIZE-1; i >= 0; i--) { 
			b[index+i] = (byte) (value & 0x000000ff); 	
			value = value >> 8; 
		}
	}

	/**
	 * Extracts an integer value from four consecutive bytes in a byte[] array. 
	 * @param b The array. 
	 * @param index The index in block of the less significant byte of the  
	 * number to extract. 
	 * @return The value extracted from bytes index+3, index+2, index+1, and index. 
	 * From most significant to less significant bytes of the number's four bytes. 
	 */
	public static int getIntFromBytesArray(byte[] b, int index) {  
		int value = 0; 
		int lSB; 
		for (int i=0; i < INTSIZE; i++) { 
			value = value << 8; 
			lSB = 0x000000ff & b[index + i];
			value = value | lSB; 
		}
		return value; 
	}

	/**
	 * Writes a char to a VirtualDiskBLock.
	 * @param vdb block to be written to.
	 * @param index index where char will be written to.
	*/
	public static void copyCharToBlock(VirtualDiskBlock vdb, int index, char c) { 
		vdb.setElement(index, (byte) c); 
	}	
	
	/**
	 * Reads a char from a VirtualDiskBLock.
	 * @param vdb block to be read from.
	 * @param index index of block that will be read.
	 * @return character read from the block.
	*/
	public static char getCharFromBlock(VirtualDiskBlock vdb, int index) { 
		return (char) vdb.getElement(index); 
	}

	/**
	 * Writes a char to a bytes array.
	 * @param b byte array where char will be written to.
	 * @param index index of the array to write to.
	*/
	public static void copyCharToBytesArray(byte[] b, int index, char c) { 
		b[index] = (byte) c; 
	}	
	
	/**
	 * Reads a char from a char array.
	 * @param b byte array where char will be read from.
	 * @param index index of the array to start reading data from.
	 * @return character that was read.
	*/
	public static char getCharFromBytesArray(byte[] b, int index) { 
		return (char) b[index]; 
	}	
	
	/**
	 * Reads a string from a VirtualDiskBLock.
	 * @param vdb block where data will be read from
	 * @param length length of the string to be written.
	 * @return string retrieved from the block
	*/
	public static String getStringFromBlock(VirtualDiskBlock vdb, int length) {
		String result = "";
		
		for(int i = 0; i < length; i++) {
			result += Utils.getCharFromBlock(vdb, i);
		}
		
		return result;
	}
	
	/**
	 * Reads a string from a VirtualDiskBLock.
	 * @param vdb block where data will be read from
	 * @param index index of first character to read from block.
	 * @param length length of the string to be written.
	 * @return string retrieved from the block
	*/
	public static String getStringFromBlock(VirtualDiskBlock vdb, int index, int length) {
		String result = "";
		
		for(int i = index; i < length + index; i++) {
			result += Utils.getCharFromBlock(vdb, i);
		}
		
		return result;
	}
	
	/**
	 * Writes a string to a VirtualDiskBlock.
	 * @param vdb block to be written to
	 * @param length length of the string to be written.
	 * @param str string to be written to block
	*/
	public static void copyStringToBlock(VirtualDiskBlock vdb, int length, String str) throws InvalidParameterException {
		if(str.length() > length)
			throw new InvalidParameterException("String is longer than usable capacity.");
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '\n')
				copyCharToBlock(vdb, i, ' ');
			copyCharToBlock(vdb, i, str.charAt(i));
			
		}
		
	}
	
	/**
	 * Writes a string to a VirtualDiskBlock.
	 * @param vdb block to be written to.
	 * @param index index in block of string's first character.
	 * @param length length of the string to be written.
	 * @param str string to be written to block
	*/
	public static void copyStringToBlock(VirtualDiskBlock vdb, int index, int length, String str) throws InvalidParameterException {
		if(str.length() > length)
			throw new InvalidParameterException("String is longer than usable capacity.");
		if(str.length() + index > vdb.getCapacity())
			throw new InvalidParameterException("String is too long for given index.");
		
		for(int i = index; i < index + str.length(); i++) {
			if((i - index) < str.length()) {
				if(str.charAt(i - index) == '\n');
				copyCharToBlock(vdb, i, ' ');
			copyCharToBlock(vdb, i, str.charAt(i - index));
			} else {
				copyCharToBlock(vdb, i, ' ');
			}
			
		}
		
	}
	
	/**
	 * Determines if a VirtualDiskBlock is empty.
	 * @param block block to be analyzed.
	 * @return true if block only has zeros or whitespace, false otherwise.
	 */
	public static boolean isBlockEmpty(VirtualDiskBlock block) {
		for(int i = 0; i < block.getCapacity(); i++) {
			if(block.getElement(i) != (byte) 0 && 
					!Character.isWhitespace((char)block.getElement(i)))
					return false;
		}
		return true;
	}
	
	/**
	 * Clears a VirtualDiskBlock by setting its contents to 0.
	 * @param block block to be cleared.
	 */
	public static void clearBlock(VirtualDiskBlock block) 
			throws InvalidParameterException {
		if(block == null)
			throw new InvalidParameterException("Block can't be null.");
		for(int i = 0; i < block.getCapacity(); i++) {
			block.setElement(i, (byte) 0 );
		}
	}
	
	/**
	 * Clears a specific section of a data block.
	 * @param block block that will be cleared.
	 * @param index index of first byte that wants to be cleared in the block.
	 * @param length number of bytes that want to be cleared.
	 */
	public static void clearBlockSpace( VirtualDiskBlock block, int index, int length) {
		for(int i = index; i < index + length; i++) {
			block.setElement(i, (byte) 0 );
		}
	}
	
	/**
	 * Looks for a file specified and reads its contents.
	 * @param name name of the file that will be read.
	 * @return string of the data read from the file.
	 * @throws IOException when file can't be read.
	 * @throws FileNotFoundException when the file can't be found in 
	 * 	the directory.
	*/
	public static String getStringFromFile(String name) 
			throws IOException , FileNotFoundException {
		File file=new File(name);
		if (!file.exists())
			throw new FileNotFoundException(name + " was not found in the directory.");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(name),"ISO-8859-1"));
		String result = "", line;
		while ((line = reader.readLine()) != null) {
			result += line;
		}
		reader.close();
		return result;
	}

}

