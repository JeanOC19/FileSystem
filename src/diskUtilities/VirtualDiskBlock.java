package diskUtilities;

import java.security.InvalidParameterException;

/**
 * Object to represent a block of bytes from a DiskUnit.
 */
public class VirtualDiskBlock {
	private final int DEFAULT_BLOCK_SIZE = 256;
	private byte[] diskBlock;
	private int capacity;
	
	/**
	 * Creates a block (byte array) with default size of 256 bytes.
	*/
	public VirtualDiskBlock() {
		diskBlock = new byte[DEFAULT_BLOCK_SIZE];
		capacity = DEFAULT_BLOCK_SIZE;
	}
	
	/**
	 * Creates a block (byte array) with the specified size.
	 * @param blockCapacity number of bytes in the block. 
	 * @throws InvalidParameterException whenever the specified block size
	 * is less than 8 bytes or not a power of 2.
	*/
	public VirtualDiskBlock(int blockCapacity) throws InvalidParameterException{
		if(blockCapacity < 8 || !Utils.powerOf2(blockCapacity))
			throw new InvalidParameterException("Incorrect block size = " + blockCapacity);
		diskBlock = new byte[blockCapacity];
		capacity = blockCapacity;
	}
	
	/**
	 * @return a nonnegative integer representing the number of
	 * bytes in the block.
	*/
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Writes the given value in the specified index of the block.
	 * @param index the position to be written in the block.
	 * @param nuevo the byte that will be written into the block.
	 * @throws IndexOutOfBoundsException whenever index is not in the range
	 * 		0 <= index < block size.
	*/
	public void setElement(int index, byte nuevo) {
		if(index < 0 || index >= capacity)
			throw new IndexOutOfBoundsException("set: Invalid block index = " + index);
		diskBlock[index] = nuevo;
	}

	/**
	 * Reads and returns the given value in the specified index of the block.
	 * @param index the position to be read from the block.
	 * @return byte residing at index of the block.
	 * @throws IndexOutOfBoundsException whenever index is not in the range
	 * 		0 <= index < block size.
	*/
	public byte getElement(int index) {
		if(index < 0 || index >= capacity)
			throw new IndexOutOfBoundsException("get: Invalid block index = " + index);
		return diskBlock[index];
	}

}