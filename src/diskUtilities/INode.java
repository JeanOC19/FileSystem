package diskUtilities;

/**
 * Object to represent an I-Node from a DiskUnit
*/
public class INode {

	private int nodeIndex;
	private int blockIndex;
	private int size;
	private byte type;
	
	/**
	 * Initializes the I-Node with the given values.
	 * @param index index of the I-Node
	 * @param i index of the first block in the file.
	 * @param sz size of the I-Node's file.
	 * @param t byte representing the type of file.
	*/
	public INode(int index, int i, int sz, byte t) {
		nodeIndex = index;
		blockIndex = i;
		size = sz;
		type = t;
	}

	/**
	 * @return nonnegative integer representing the index of the
	 * 	first data block of the file.
	*/
	public int getBlockIndex() {
		return blockIndex;
	}

	/**
	 * Sets the index of the first block of the node's file.
	 * @param i index of the first block in the file.
	*/
	public void setBlockIndex(int i) {
		blockIndex = i;
	}

	/**
	 * @return size of the I-Node's file.
	*/
	public int getSize() {
		return size;
	}

	/**
	 * @param sz sets the size of the I-Node's file.
	*/
	public void setSize(int sz) {
		size = sz;
	}

	/**
	 * @return byte representing the type of the file.
	*/
	public byte getType() {
		return type;
	}

	/**
	 * @param t byte representing the type of the file.
	*/
	public void setType(byte t) {
		type = t;
	}

	/**
	 * @return nonnegative integer representing the index 
	 * 	of the I-Node.
	*/
	public int getNodeIndex() {
		return nodeIndex;
	}

	/**
	 * @param nodeIndex integer representing the index of
	 * 	the node.
	*/
	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}
	
	
}
