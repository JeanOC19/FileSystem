package diskUtilities;

import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import exceptions.FullFileException;
import exceptions.NonEmptyFolderException;

/**
 * Object to represent a directory or folder inside a DiskUnit object which 
 * acts as a virtual disk.
 * @author jeano
 *
 */
public class Directory {
	
	private int rootBlock;
	private INode dirNode;
	private String name;
	private int numOfBlocks;
	private int bSize;
	private int filesPerBlock;
	private DiskUnit disk;
	
	/**
	 * Constructor to create an empty or uninitialized directory object.
	 * @param disk diskUnit where directory resides.
	 */
	public Directory(DiskUnit disk) {
		this.disk = disk;
		rootBlock = 0;
		bSize = disk.getBlockSize();
		filesPerBlock = bSize/24;
		numOfBlocks = 0;
	}
	
	/**
	 * Constructor to a directory object for an existing directory
	 * @param disk diskUnit where directory resides.
	 * @param dirNode I-Node of the directory that wants to be accessed.
	 */
	public Directory(DiskUnit disk, INode dirNode) {
		this.disk = disk;
		this.dirNode = dirNode;
		rootBlock = dirNode.getBlockIndex();
		bSize = disk.getBlockSize();
		filesPerBlock = bSize/24;
		numOfBlocks = dirNode.getSize();
	}
	
	/**
	 * Creates a new directory in the current diskUnit.
	 * @param name name of the directory that will be created.
	 * @throws InvalidParameterException whenever name is null.
	 */
	public void createDir(String name) throws InvalidParameterException {
		if(name == null)
			throw new InvalidParameterException("Directory name is null.");
		
		dirNode = disk.getFirstFreeINode();
		rootBlock = disk.getFreeBN();
		dirNode.setBlockIndex(rootBlock);
		dirNode.setType((byte) 1);
		dirNode.setSize(1);
		this.name = name;
		disk.saveINode(dirNode);
		numOfBlocks = 1;
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		for(int i = 5; i > 0; i--) {
			Utils.copyIntToBlock(root, bSize - (i * 4), 0);
		}
		disk.write(rootBlock, root);
	}

	/**
	 * Deletes the current directory from the diskUnit
	 * @throws NonEmptyFolderException when the current directory
	 * 	is not empty.
	 */
	public void deleteDir() throws NonEmptyFolderException {
		if(numOfBlocks > 0 && this.getFiles().size() > 0)
			throw new NonEmptyFolderException("Directory still has files in it.");
		
		disk.setFirstFreeINode(dirNode);
		name = null;
		dirNode = null;
		rootBlock = 0;
		disk = null;
	}
	
	/**
	 * Adds a file's information to the current directory.
	 * @param name name of the desired file to be added.
	 * @param nodeIndex index of the file's node.
	 * @throws InvalidParameterException whenever
	 */
	public void addFile(String name, int nodeIndex) throws InvalidParameterException {
		if(name == null)
			throw new InvalidParameterException("Name is null.");
		if(name.length() > 20)
			throw new InvalidParameterException("Name must be 20 characters or less.");
		//Add code to check if file can't be larger.
		
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, root);
		
		if(numOfBlocks == 0)
			this.addBlock();
		
		int blockIndex = this.getBlock(numOfBlocks - 1);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(blockIndex, vdb);
		int indexInBlock = this.getAvailableIndex(vdb);
		
		if(blockIndex == rootBlock && indexInBlock + 24 > bSize - 20) {
			vdb = new VirtualDiskBlock(bSize);
			blockIndex = this.addBlock();
			Utils.copyStringToBlock(vdb, 20, name);
			Utils.copyIntToBlock(vdb, 20, nodeIndex);
		} else if(indexInBlock < 0) {
			vdb = new VirtualDiskBlock(bSize);
			blockIndex = this.addBlock();
			Utils.copyStringToBlock(vdb, 20, name);
			Utils.copyIntToBlock(vdb, 20, nodeIndex);
		} else {
			Utils.copyStringToBlock(vdb, indexInBlock, 20, name);
			Utils.copyIntToBlock(vdb, indexInBlock + 20, nodeIndex);
		}
		disk.write(blockIndex, vdb);	
		
	}

	/**
	 * Removes a file's information from the current directory.
	 * @param name name of the file that will be removed.
	 * @throws FileNotFoundException when file is not in the current directory.
	 * @throws InvalidParameterException when given name is null.
	 */
	public void removeFile(String name) 
			throws FileNotFoundException, InvalidParameterException {
		if(name == null)
			throw new InvalidParameterException("Name is null.");
		
		int fileIndex = this.findFileBlock(name);
		if(fileIndex == -1)
			throw new FileNotFoundException("No such file in this directory.");
		
		int lastBlock = this.getBlock(numOfBlocks - 1);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(fileIndex, vdb);
		int indexInBlock1 = 0;
	
		for(int i = 0; i < filesPerBlock; i++) {
			if(Utils.getStringFromBlock(vdb, i * 24, name.length()).contains(name)) {
				Utils.clearBlockSpace(vdb, i*24, 20);
				Utils.copyIntToBlock(vdb, i*24 + 20, 0);
				indexInBlock1 = i * 24;
			}
		}
		
		if(fileIndex == lastBlock) {
			if(Utils.isBlockEmpty(vdb))
				this.deleteBlock(fileIndex);
			else
				disk.write(fileIndex, vdb);
		} else {
			VirtualDiskBlock vdb2 = new VirtualDiskBlock(bSize);
			disk.read(lastBlock, vdb2);
			int indexInBlock2 = this.getAvailableIndex(vdb2) - 24;
			if(indexInBlock2 < 0)
				indexInBlock2 = (filesPerBlock - 1) * 24;

			Utils.copyStringToBlock(vdb, indexInBlock1, 20, 
					Utils.getStringFromBlock(vdb2, indexInBlock2, 20));
			Utils.copyIntToBlock(vdb, indexInBlock1 + 20,
					Utils.getIntFromBlock(vdb2, indexInBlock2 + 20));
			Utils.clearBlockSpace(vdb2, indexInBlock2, 20);
			Utils.copyIntToBlock(vdb2, indexInBlock2 + 20, 0);
			disk.write(fileIndex, vdb);
			
			if(Utils.isBlockEmpty(vdb2))
				this.deleteBlock(lastBlock);
			else
				disk.write(lastBlock, vdb2);
		}
			
	}
	
	/**
	 * Finds the I-Node corresponding to the desired file in the directory.
	 * @param name name of file whose I-Node wants to be accessed.
	 * @return I-Node object of the file or null if no such file exists.
	 */
	public INode getFileNode(String name) {
		int fileIndex = this.findFileBlock(name);
		if(fileIndex < 0)
			return null;
		
		if(fileIndex != -1) {
			VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
			disk.read(fileIndex, vdb);
			
			for(int i = 0; i < filesPerBlock * 24; i += 24) {
				if(Utils.getStringFromBlock(vdb, i, name.length()).contains(name)) {
					int node = Utils.getIntFromBlock(vdb, i + 20);
					return disk.getINode(node);
				}	
			}
		}
		return null;
	}
	
	/**
	 * Determines if the given file in the directory is a directory or 
	 * 	data file.
	 * @param name name of the file
	 * @return true if file is a directory, false if otherwise.
	 * @throws FileNotFoundException if no such file exists in current 
	 * 	directory.
	 */
	public boolean isFileDirectory(String name) throws FileNotFoundException {
		INode node = this.getFileNode(name);
		if(node == null)
			throw new FileNotFoundException("No such file in directory.");
		
		return node.getType() == (byte) 1;
	}
	
	/**
	 * Retrieves all the files in the current directory.
	 * @return An ArrayList with all the names of the files or a corresponding
	 * 	message if the directory is empty.
	 * @throws InvalidParameterException
	 */
	public ArrayList<String> getFiles() {
		ArrayList<String> list = new ArrayList<String>();
		
		if(numOfBlocks == 0)
			list.add("Directory is empty.");
		
		for(int i = 0; i < numOfBlocks; i++) {
			int index = this.getBlock(i);
			VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
			disk.read(index, vdb);
			for(int j = 0; j < filesPerBlock; j++) {
				if(Character.isLetter(Utils.getCharFromBlock(vdb, j * 24)))
					list.add(Utils.getStringFromBlock(vdb, j * 24, 20));
			}
		}
		
		return list;
	}

	/**
	 * Deletes a block of data from the current directory.
	 * @param blockIndex disk index of the block that wants to be deleted.
	 * @throws InvalidParameterException whenever the root block wants to
	 * 	be deleted while there are other data blocks corresponding to the 
	 * 	directory.
	 */
	private void deleteBlock(int blockIndex) throws InvalidParameterException{
		if(blockIndex == rootBlock && numOfBlocks != 1)
			throw new InvalidParameterException("Cannot delete root block");
		
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		disk.write(blockIndex, new VirtualDiskBlock(bSize));
		disk.read(rootBlock, root);
		
		if(numOfBlocks >= 1 && numOfBlocks <= 4) {
			Utils.copyIntToBlock(root, bSize - 20 + (4 * (numOfBlocks-2)), 0);
			disk.registerFB(blockIndex);
			disk.write(rootBlock, root);
		} else if(numOfBlocks > 4 && numOfBlocks <= 4 + bSize/4) {
			OneLevelBlock olb = new OneLevelBlock(disk);
			olb.getOneLevelBlock(Utils.getIntFromBlock(root, bSize - 8));
			if(numOfBlocks == 5) {
				olb.delete();
				Utils.copyIntToBlock(root, bSize - 8, 0);
				disk.write(rootBlock, root);
			} else 
				olb.deleteBlock(olb.getSize()-1);
		} else {
			TwoLevelBlock tlb = new TwoLevelBlock(disk);
			tlb.getTwoLevelBlock(Utils.getIntFromBlock(root, bSize - 4));
			if(numOfBlocks == 5 + bSize/4) {
				tlb.delete();
				Utils.copyIntToBlock(root, bSize - 4, 0);
				disk.write(rootBlock, root);
			} else
				tlb.deleteDiskBlock(tlb.getSize() - 1);	
		}
		
		numOfBlocks--;
		dirNode.setSize(numOfBlocks);
		if(numOfBlocks == 0)
			dirNode.setBlockIndex(0);
		disk.saveINode(dirNode);
			
	}

	/**
	 * Adds a block of data to the directory file.
	 * @return index of the block that was added to the file.
	 * @throws FullFileException whenever current directory has maximum 
	 * 	number of data blocks.
	 */
	private int addBlock() throws FullFileException {
		if(numOfBlocks >= 4 + ((bSize/4) + 1) * (bSize/4))
			throw new FullFileException("File has maximum number of blocks");
		int filesInRoot = (bSize - 20)/24;
		int newBlock;
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, root);

		if(numOfBlocks == 0)
			rootBlock = disk.getFreeBN();
		if(numOfBlocks == 1 && filesInRoot > 0) {
			newBlock = disk.getFreeBN();
			Utils.copyIntToBlock(root, bSize - 20, newBlock);
			disk.write(rootBlock, root);
		} else if(numOfBlocks >= 1 && numOfBlocks <= 4) {
			if(numOfBlocks == 4) {
				OneLevelBlock olb = new OneLevelBlock(disk);
				olb.createOneLevelBlock(1);
				newBlock = olb.getDiskBlock(0);
				Utils.copyIntToBlock(root, bSize - 8, olb.getRootBlock());
			} else {
				newBlock = disk.getFreeBN();
				Utils.copyIntToBlock(root, bSize - 20 + (numOfBlocks - 1) * 4, newBlock);
			}
			disk.write(rootBlock, root);
		} else if(numOfBlocks > 4 && numOfBlocks <= 4 + bSize/4) {
			if(numOfBlocks == 4 + bSize/4) {
				TwoLevelBlock tlb = new TwoLevelBlock(disk);
				tlb.createTwoLevelBlock(1);
				newBlock = tlb.getOneLevelBlock(0).getDiskBlock(0);
				Utils.copyIntToBlock(root, bSize - 4, tlb.getRootBlock());
				disk.write(rootBlock, root);
			} else {
				OneLevelBlock olb = new OneLevelBlock(disk);
				olb.getOneLevelBlock(Utils.getIntFromBlock(root, bSize - 8));
				VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
				olb.addBlock(vdb);
				newBlock = olb.getDiskBlock(olb.getSize() - 1);
			}
			
		} else {
			TwoLevelBlock tlb = new TwoLevelBlock(disk);
			tlb.getTwoLevelBlock(Utils.getIntFromBlock(root, bSize - 4));
			VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
			tlb.addDiskBlock(vdb);
			OneLevelBlock olb = tlb.getOneLevelBlock(tlb.getNumOfOneBlocks() - 1);
			newBlock = olb.getDiskBlock(olb.getSize() - 1);
		}
		numOfBlocks++;
		dirNode.setSize(numOfBlocks);
		disk.saveINode(dirNode);
		return newBlock;
	}

	/**
	 * Finds the first index where a file's information can be written
	 * 	to inside a data block, whenever available.
	 * @param vdb disk block that wants to be written to
	 * @return -1 if block is full or index where block can
	 * 	be written to.
	 */
	private int getAvailableIndex(VirtualDiskBlock vdb) 
			throws InvalidParameterException {
		if(vdb == null)
			throw new InvalidParameterException("Data block is null.");
		
		int pos = 0;
		while(pos < bSize - 24 && Utils.getIntFromBlock(vdb, pos + 20) != 0) {
			pos += 24;
		}
		if(pos > bSize - 24 || Utils.getIntFromBlock(vdb, pos + 20) != 0)
			return -1;
		else
			return pos;
	}

	/**
	 * Finds the data block where a given file's information is stored in.
	 * @param name name of the file
	 * @return index of the data block or -1 if file was not found in
	 * 	current directory.
	 */
	private int findFileBlock(String name) {
		if(numOfBlocks == 0)
			return -1;
		
		for(int i = 0; i < numOfBlocks; i++) {
			VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
			int blockIndex = this.getBlock(i);
			disk.read(blockIndex, vdb);
			for(int j = 0; j < filesPerBlock; j++) {
				if(Utils.getStringFromBlock(vdb, j * 24, name.length()).contains(name))
					return blockIndex;
			}
			
		}
		return -1;
	}
	
	/**
	 * Finds the desired block number in the list of data blocks of
	 * 	the directory.
	 * @param index index of the block to be retrieved.
	 * @return index of the data block in the diskUnit.
	 * @throws IndexOutOfBoundsException whenever index is less than 0
	 * 	or greater than current size.
	 */
	private int getBlock(int index) throws IndexOutOfBoundsException {
		if(index >= numOfBlocks || index < 0)
			throw new IndexOutOfBoundsException("Invalid block index = " + index);
		
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, root);
		
		if(index == 0)
			return rootBlock;
		else if(index > 0 && index < 4)
			return Utils.getIntFromBlock(root, (bSize - 20) + (index - 1) * 4);
		else if(index >= 4 && index <= 3 + bSize/4) {
			OneLevelBlock olb = new OneLevelBlock(disk);
			olb.getOneLevelBlock(Utils.getIntFromBlock(root, bSize - 8));
			return olb.getDiskBlock(index - 4);
		} else {
			TwoLevelBlock tlb = new TwoLevelBlock(disk);
			tlb.getTwoLevelBlock(Utils.getIntFromBlock(root, bSize - 4));
			int tlbIndex = (index - 4 - bSize/4)/(bSize/4);
			OneLevelBlock olb = tlb.getOneLevelBlock(tlbIndex);		
			return olb.getDiskBlock((index - 4 - bSize/4)%(bSize/4));
		}
	}
	
	/**
	 * @return the directory file's root block index
	 */
	public int getRootBlock() {
		return rootBlock;
	}

	/**
	 * @return the name of the current directory
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the I-Node of the current directory.
	 */
	public INode getDirNode() {
		return dirNode;
	}
	
}
