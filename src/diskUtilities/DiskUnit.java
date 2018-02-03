package diskUtilities;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;
import exceptions.ExistingDiskException;
import exceptions.FullDiskException;
import exceptions.InvalidBlockException;
import exceptions.InvalidBlockNumberException;
import exceptions.InvalidIndexException;
import exceptions.NonExistingDiskException;

/**
*  Object to represent a virtual disk that stores and reads data managed as
*  blocks. Includes methods to read, write and modify blocks. Disks are 
*  Random Access Files.
*/
public class DiskUnit {
	
	private static final int DEFAULT_CAPACITY = 1024;  	// default number of blocks 	
	private static final int DEFAULT_BLOCK_SIZE = 256; 	// default number of bytes per block
	private static final String FILEPATH = "DiskUnits/";
	private int capacity;     					// number of blocks of current disk instance
	private int blockSize; 						// size of each block of current disk instance
	private int numOfINodes;
	private int firstFLB;
	private int firstFLBPos;
	private int firstFIN;
	private String diskName;
	
	
	// the file representing the simulated  disk, where all the disk blocks are stored
	private RandomAccessFile disk;

	 /**
	  * Saves the RAF file that will be used to represent the disk.
	  * @param name is the name of the disk
	  **/
	private DiskUnit(String name) {
		try {
			disk = new RandomAccessFile(FILEPATH + name, "rw");
			diskName = name;
		}
		catch (IOException e) {
			System.err.println ("Unable to start the disk");
			System.exit(1);
		}
	}
	
	/**
	 * Turns on an existing disk unit whose name is given. If successful, it makes
	 * the particular disk unit available for operations suitable for a disk unit.
	 * @param name is the name of the disk unit to activate
	 * @return the corresponding DiskUnit object
	 * @throws NonExistingDiskException whenever no ¨disk¨ with the 
	 * 		specified name is found.
	 **/
	public static DiskUnit mount(String name) throws NonExistingDiskException {
		File file = new File(FILEPATH + name);
		   if (!file.exists())
		       throw new NonExistingDiskException("No disk has name : " + name);
		  
		   DiskUnit dUnit = new DiskUnit(name);
		   try {
		  	   dUnit.disk.seek(0);
		       dUnit.capacity = dUnit.disk.readInt();
		       dUnit.blockSize = dUnit.disk.readInt();
		       dUnit.firstFLB = dUnit.disk.readInt();
		       dUnit.firstFLBPos = dUnit.disk.readInt();
		       dUnit.firstFIN = dUnit.disk.readInt();
		       dUnit.numOfINodes = dUnit.disk.readInt();
		   } catch (IOException e) {
		  	 e.printStackTrace();
		   }
		   	
		   return dUnit;     	
	}
	
	/**
	 * Creates a new disk unit with the given name. The disk is formatted
	 * as having default capacity (number of blocks), each of default
	 * size (number of bytes). Those values are: DEFAULT_CAPACITY and
	 * DEFAULT_BLOCK_SIZE. The created disk is left as in off mode.
	 * @param name the name of the file that is to represent the disk.
	 * @throws ExistingDiskException whenever the name attempted is
	 * 		already in use.
	*/
	public static void createDiskUnit(String name)
			throws ExistingDiskException {
	   createDiskUnit(name, DEFAULT_CAPACITY, DEFAULT_BLOCK_SIZE);
	}
	
	/**
	 * Creates a new disk unit with the given name. The disk is formatted
	 * as with the specified capacity (number of blocks), each of specified
	 * size (number of bytes).  The created disk is left as in off mode.
	 * @param name the name of the file that is to represent the disk.
	 * @param capacity number of blocks in the new disk
	 * @param blockSize size per block in the new disk
	 * @throws ExistingDiskException whenever the name attempted is
	 * already in use.
	 * @throws InvalidParameterException whenever the values for capacity
	 *  or blockSize are not valid according to the specifications
	*/
	public static void createDiskUnit(String name, int capacity, int blockSize)
			throws ExistingDiskException, InvalidParameterException {

		File file=new File(FILEPATH + name);
	    if (file.exists())
	       throw new ExistingDiskException("Disk name is already used: " + name);
	   	
	    RandomAccessFile disk = null;
	    if (capacity < 0 || blockSize < 32 || !Utils.powerOf2(capacity) || !Utils.powerOf2(blockSize))
	       throw new InvalidParameterException("Invalid values: " + " capacity = " 
	    		   + capacity + " block size = " + blockSize);

	    try {
	        disk = new RandomAccessFile(FILEPATH + name, "rw");
	        setUpBlockZero(disk, capacity, blockSize);
	        setUpINodes(disk, capacity, blockSize);
	        reserveDiskSpace(disk, capacity, blockSize);
	    }
	    catch (IOException e) {
	        System.err.println ("Unable to start " + name);
	        System.exit(1);
	    }
	    
	    try {
	        disk.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    DiskUnit dUnit = mount(name);

	    int temp = dUnit.firstFLB;
	    dUnit.firstFLB = 0;
	    for(int i = dUnit.capacity - 1; i >= temp; i--) {
	    	dUnit.registerFB(i);
	    }
	    INode node = dUnit.getINode(0);
	    node.setBlockIndex(dUnit.getFreeBN());
	    node.setType((byte) 1); 
	    node.setSize(1);
	    dUnit.saveINode(node);
	    dUnit.shutdown();

	}

	/**
	 * Sets the size of the specified disk unit according to the capacity 
	 * (number of blocks) and the block size (number of bytes) and saves 
	 * these parameter values into block 0 of the disk.
	 * @param disk file that is used to represent the disk.
	 * @param capacity number of blocks in disk.
	 * @param blockSize size per block in the disk.
	*/
	private static void reserveDiskSpace(RandomAccessFile disk, int capacity, int blockSize) {
		try {
			disk.setLength(blockSize * capacity);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			disk.seek(0);
			disk.writeInt(capacity);  
			disk.writeInt(blockSize);
		} catch (IOException e) {
			e.printStackTrace();
		} 	
	}

	/**
	 * Copies the contents of the specified block into the
	 * specified block number (address) of the disk.
	 * @param blockNum the number of the block that will be written.
	 * @param b block which values will be copied into disk.
	 * @throws InvalidBlockNumberException whenever the block number is 
	 * 0 or less or larger than the capacity.
	 * @throws InvalidBlockException whenever given block is empty 
	 * or a different size.
	*/
	public void write(int blockNum, VirtualDiskBlock b) 
			throws InvalidBlockNumberException, InvalidBlockException {
		if(blockNum <= 0 || blockNum >= capacity)
			throw new InvalidBlockNumberException("Invalid block index = " + blockNum);
		if(b == null)
			throw new InvalidBlockException("Block is null.");
		if(b.getCapacity() != blockSize)
			throw new InvalidBlockException("Block is not the correct size");
		try {
			//Go to the block location in the RAF
			disk.seek(blockNum * blockSize);
			//Read each byte in the block and copy it.
			for(int i = 0; i < blockSize; i++) {
				disk.writeByte(b.getElement(i));
			}
		} catch (IOException e) {
			System.out.println("Unable to write to disk.");
		}
	}

	/**
	 * Reads and copies the contents of the specified disk block 
	 * (address) into the specified block.
	 * @param blockNum the number of the block that will be read.
	 * @param b block where values will be copied to.
	 * @throws InvalidBlockNumberException whenever the block number is 
	 * 0 or less or larger than the capacity.
	 * @throws InvalidBlockException whenever given block is a different
	 * size than blocks in disk.
	*/
	public void read(int blockNum, VirtualDiskBlock b) 
			throws InvalidBlockNumberException, InvalidBlockException {
		if(b == null)
			throw new InvalidBlockException("Block is null.");
		if(blockNum < 0 || blockNum >= capacity)
			throw new InvalidBlockNumberException("Invalid block index = " + blockNum);
		if(b.getCapacity() != blockSize)
			throw new InvalidBlockException("Block is not the correct size");
		try {
			//Go to the location of the block in the RAF
			disk.seek(blockNum * blockSize);
			//Read each byte from file and copy it to the block
			for(int i = 0; i < blockSize; i++) {
				b.setElement(i, disk.readByte());
			}
		} catch (IOException e) {
			System.out.println("Unable to read from disk");
		}
	}
	
	/**
	 * @return a nonnegative number representing the number of blocks (capacity) 
	 * in the disk.
	*/
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @return a nonnegative number representing the number of bytes (blockSize) 
	 * in each block.
	*/
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * @return a string representing the name of the disk.
	*/
	public String getDiskName() {
		return diskName;
	}
	
	/**
	 * @return a nonnegative number representing the number of I-Nodes in the 
	 * disk.
	*/
	public int getNumOfINodes() {
		return numOfINodes;
	}

	/**
	 * Retrieves a free I-Node from the list of free nodes.
	 * @return The first I-Node in the list that is available.
	 * @throws FullDiskException when there are no available I-Nodes
	*/
	public INode getFirstFreeINode() throws FullDiskException {
		if(firstFIN == 0)
			throw new FullDiskException("Disk is full, no more files can be added.");
		INode node = this.getINode(firstFIN);
		firstFIN = node.getBlockIndex();
		return node;
	}

	/**
	 * Sets a new I-Node as a free I-Node in the disk.
	 * @param node the I-Node to set as available.
	 * @throws InvalidIndexException whenever the index is of the 
	 * 	root I-node or larger than the number of I-Nodes.
	 * @throws InvalidParameterException whenever the node is null.
	*/
	public void setFirstFreeINode(INode node) 
			throws InvalidIndexException, InvalidParameterException {
		if(node == null)
			throw new InvalidParameterException("I-Node is null");
		if(node.getNodeIndex() < 1 || node.getNodeIndex() >= this.numOfINodes)
			throw new InvalidIndexException("Invalid I-Node received");
		node.setBlockIndex(firstFIN);
		firstFIN = node.getNodeIndex();
		node.setSize(0);
		node.setType((byte) 0);
		this.saveINode(node);
	}
	
	/**
	 * Formats the data in the disk by setting all blocks, except the first
	 * one, to zero.
	*/
	public void lowLevelFormat() {
		try {
			for(int j = blockSize; j < capacity * blockSize; j++) {
				disk.seek(j);
				disk.writeByte(0);
			}
		} catch (IOException e) {
			System.err.println ("Unable to format disk");
			System.exit(1);
		}
	}

	/** Simulates shutting-off the disk. Just closes the corresponding RAF. 
	 *  Called when no more operations will be made on the disk. 
	 **/
	public void shutdown() {
		try {
			disk.seek(8);
			disk.writeInt(firstFLB);
			disk.seek(12);
			disk.writeInt(firstFLBPos);
			disk.seek(16);
			disk.writeInt(firstFIN);
			disk.close();
			diskName = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Deletes the RAF of the selected disk.
	 * @param name name of the file (disk) that wants to be deleted.
	 **/
	public static void deleteDisk(String name) {
		File raf = new File(FILEPATH + name);
		raf.delete();
	}
	
	/**
	 * Sets up the initial values of block 0 in the disk.
	 * @param disk the RAF of the corresponding disk
	 * @param cap capacity of the disk
	 * @param bSize block size of the disk.
	*/
	private static void setUpBlockZero(RandomAccessFile disk, int cap, int bSize) {
		
		int firstFreeINode = 0;
		int numOfINodes = (int) (cap * bSize * 0.01);
		if(numOfINodes < 1)
			numOfINodes = 1;
		int firstFLB = numOfINodes/(bSize/9) + 1;
		if(numOfINodes%(bSize/9) != 0)
			firstFLB++;
		if(numOfINodes > 1)
			firstFreeINode = 1;
		
		try {
	        disk.seek(0);
	        disk.writeInt(cap);
	        disk.writeInt(bSize);
			disk.writeInt(firstFLB);
			disk.writeInt(0);
			disk.writeInt(firstFreeINode);
			disk.writeInt(numOfINodes);
			
			//Set up root directory I-Node
			disk.seek(bSize);
			disk.writeInt(0);
			disk.writeInt(0);
			disk.writeByte(1);
			disk.writeInt(2);
			disk.writeInt(0);
		} catch (IOException e) {
			System.out.println("Unable to write to block 0.");
		}
		
	}

	/**
	 * Sets up and initializes the I-Nodes of the corresponding disk.
	 * @param disk the RAF of the corresponding disk
	 * @param capacity capacity of the disk
	 * @param blockSize size of blocks of the disk.
	*/
	private static void setUpINodes(RandomAccessFile disk, int capacity, int blockSize) {
		int index = 2;
		int numINodes = (int) (capacity * blockSize * .01);
		int numBlocks = numINodes/(blockSize/9) + 1;
		if(numINodes%(blockSize/9) != 0)
			numBlocks++;
		int nodesPerBlock = blockSize/9;
		
		for(int i = 1; i <= numBlocks; i++) {
			for(int j = 0; j < nodesPerBlock; j++) {
				if((i != 1 || j != 0) && (index != 1)) {
					try {
						disk.seek((i * blockSize) + (j * 9));
						disk.writeInt(index);
						disk.seek((i * blockSize) + (j * 9) + 4);
						disk.writeInt(0);
						
						if(index == numINodes - 1)
							index = 0;
						else
							index++;
					} catch (IOException e) {
						System.out.println("Unable to set up I-Nodes.");
					}
				}
			}
		}
	}
	
	/**
	 * @param i index of the I-Node to be read
	 * @return The I-Node object that wants to be read.
	 * @throws InvalidIndexException whenever the index given is
	 * 	not that of a valid I-Node.
	*/
	public INode getINode(int i) throws InvalidIndexException {
		if( i < 0 || i >= numOfINodes) 
			throw new InvalidIndexException("Invalid I-Node index = " + i);
		int nodesPerBlock = blockSize/9;
		int blockIndex = (i % nodesPerBlock);
		int blockNum = i/nodesPerBlock + 1;

		INode node = null;
		try {
			disk.seek(blockNum * blockSize + blockIndex * 9);
			node = new INode(i, disk.readInt(), disk.readInt(),disk.readByte());
		} catch (IOException e) {
			System.out.println("Unable to read INode from disk.");
		}
		return node;
	}
	
	/**
	 * Saves an I-Node object to the disk being used.
	 * @param node I-Node to be saved to disk.
	 * @throws InvalidIndexException whenever the index is out of the 
	 * 	possible range of I-Nodes.
	 * @throws InvalidParameterException whenever I-node is null.
	*/
	public void saveINode(INode node) 
			throws InvalidIndexException, InvalidParameterException {
		if(node == null)
			throw new InvalidParameterException("I-Node is null.");
		if(node.getNodeIndex() < 0 || node.getNodeIndex() >= numOfINodes) 
			throw new InvalidIndexException("Invalid I-Node number = " + node.getNodeIndex());
		
		int nodesPerBlock = blockSize/9;
		int blockIndex = (node.getNodeIndex() % nodesPerBlock);
		int blockNum = node.getNodeIndex()/nodesPerBlock + 1;
		try {
			disk.seek(blockNum * blockSize + blockIndex * 9);
			disk.writeInt(node.getBlockIndex()); 
			disk.writeInt(node.getSize());
			disk.writeByte(node.getType());
		} catch (IOException e) {
			System.out.println("Unable to read INode from disk.");
		}
	}

	/**
	 * @return the index of the next available free block in the disk.
	 * @throws FullDiskException when there are no free blocks.
	*/
	public int getFreeBN() throws FullDiskException { 
		   int bn; 
		   if (firstFLB == 0) 
		      throw new FullDiskException("Disk is full.");
		   
		   VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		   this.read(firstFLB, vdb);
		   
		   if (firstFLBPos != 0) { 
			  bn = Utils.getIntFromBlock(vdb, firstFLBPos * 4);
		      firstFLBPos--; 
		   }   
		   else {                                  
		      bn = firstFLB; 
		      firstFLB = Utils.getIntFromBlock(vdb, 0);  
		      firstFLBPos = (blockSize/4) - 1;               
		   } 
		   return bn;     
		}
	
	/**
	 * Receives the index of a block that will be set as available.
	 * @throws InvalidIndexException whenever the index is not of a
	 * 	block in the disk.
	*/
	public void registerFB(int bn) throws InvalidIndexException { 
		if(bn <= 0 || bn >= capacity)
			throw new InvalidIndexException("registerFB: Index of block is invalid = " + bn);
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		if (firstFLB == 0)  { 
			firstFLB = bn; 
			Utils.copyIntToBlock(vdb, 0, 0);
			firstFLBPos = 0; 
			this.write(firstFLB, vdb);
		}  else if (firstFLBPos == (blockSize/4) - 1) {  
			Utils.copyIntToBlock(vdb, 0, firstFLB);
			firstFLBPos = 0;
			firstFLB = bn; 
			this.write(firstFLB, vdb);
		}  else { 
			this.read(firstFLB, vdb);
			firstFLBPos++; 
			Utils.copyIntToBlock(vdb, firstFLBPos * 4, bn);
			this.write(firstFLB, vdb);
		} 
	}     
	
	/**
	 * @param size the required number of blocks in the disk
	 * @return if there is enough space in disk or not.
	*/
	public boolean checkIfEnoughSpace(int size) {
		INode node = this.getINode(0);
		if(size > this.getCapacity() - node.getSize())
			return false;
		else
			return true;
	}
}
