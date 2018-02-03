package diskUtilities;

import java.security.InvalidParameterException;

/**
 * Object to represent a data or text file inside a DiskUnit object.
 * @author jeano
 *
 */
public class DataFile {
	
	private DiskUnit disk;
	private int bSize;
	private int rootBlock;
	private INode fileNode;

	/**
	 * Creates an empty or uninitialized DataFile object.
	 * @param disk diskUnit where file will be in.
	 */
	public DataFile(DiskUnit disk) {
		this.disk = disk;
		rootBlock = 0;
		bSize = disk.getBlockSize();
		
	}
	
	/**
	 * Creates a DataFile object for an existing data file.
	 * @param disk diskUnit where file is in.
	 * @param node I-Node of the existing file.
	 */
	public DataFile(DiskUnit disk, INode node) {
		this.disk = disk;
		bSize = disk.getBlockSize();
		fileNode = node;
		rootBlock = node.getBlockIndex();
	}
	
	/**
	 * Creates a text or data file in the current disk.
	 * @param string data that will be written into the file.
	 * @throws InvalidParameterException whenever the string is null or
	 * 	when the string is too large to be stored in a single file.
	 */
	public void createFile(String string) throws InvalidParameterException {
		if(string == null)
			throw new InvalidParameterException("String is null.");
		if(string.length() > (bSize - 20) + (3 * bSize + 
				(bSize/4) * bSize) + ((bSize/4) * bSize)*(bSize/4))
			throw new InvalidParameterException("File is too large.");
		
		//Set up root block and I-Node for file
		fileNode = disk.getFirstFreeINode();
		rootBlock = disk.getFreeBN();
		fileNode.setBlockIndex(rootBlock);
		fileNode.setSize(string.length()/bSize + 1);
		fileNode.setType((byte) 0);
		disk.saveINode(fileNode);
		
		//Initialize indexes of root block as 0
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		for(int i = 20; i < bSize - 4; i += 4) {
			Utils.copyIntToBlock(root, i, 0);
		}
		int nextFB;
		
		if(string.length() <= bSize - 20) {
			Utils.copyStringToBlock(root, string.length(), string);
			disk.write(rootBlock, root);
		} 
		if(string.length() > bSize - 20) {
			Utils.copyStringToBlock(root, bSize - 20, string.substring(0, bSize - 20));
			int index = bSize - 20;
			int numBlocks = string.substring(bSize-20).length() / bSize;
			if(string.substring(bSize-20).length() % bSize != 0)
				numBlocks++;
			if(numBlocks > 3)
				numBlocks = 3;
			
			for(int i = 0; i < numBlocks; i++) {
				
				VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
				nextFB = disk.getFreeBN();
				Utils.copyIntToBlock(root, index + i * 4, nextFB);
				
				if(string.substring((i * bSize) + index).length() < bSize)
					Utils.copyStringToBlock(vdb, string.substring(i * bSize + index).length(),
							string.substring(i * bSize + index));
				else {
					Utils.copyStringToBlock(vdb, bSize, 
							string.substring(i * bSize + index, (i+1) * bSize + index));
				}
				disk.write(nextFB, vdb);
			}
		}
		if(string.length() > (bSize - 20) + 3 * bSize ) {
			int numBlocks = (string.length() - ((bSize - 20) + 3*bSize)) / bSize;
			if((string.length() - ((bSize - 20) + 3*bSize)) % bSize != 0)
				numBlocks++;
	
			String sub;
			if(numBlocks > bSize/4) {
				numBlocks = bSize/4;
				sub = string.substring(bSize - 20 + 3*bSize, bSize - 20 + 3*bSize + bSize * (bSize/4));
			} else {
				sub = string.substring(bSize - 20 + 3*bSize);
			}
			
			OneLevelBlock oneBlock = new OneLevelBlock(disk);
			oneBlock.createOneLevelBlock(numBlocks);
			oneBlock.writeString(sub);
			Utils.copyIntToBlock(root, bSize - 8, oneBlock.getRootBlock());
			
		}
		if(string.length() > (bSize - 20) + 3 * bSize + (bSize/4) * bSize) {
			
			String sub = string.substring((bSize - 20) + 3 * bSize + (bSize/4) * bSize);
			int numBlocks = sub.length()/bSize;
			if(sub.length() % bSize != 0)
				numBlocks++;
			TwoLevelBlock twoBlock = new TwoLevelBlock(disk);
			twoBlock.createTwoLevelBlock(numBlocks);
			twoBlock.writeString(sub);
			Utils.copyIntToBlock(root, bSize - 4, twoBlock.getRootBlock());
		} 
		
		disk.write(rootBlock, root);
	}
	
	/**
	 * Adds desired data to the end of the current file.
	 * @param string data to be added to the file.
	 * @throws InvalidParameterException whenever the string is null or when 
	 * method is called when no file has been initialized.
	 */
	public void appendFile(String string) throws InvalidParameterException {
		if(string == null)
			throw new InvalidParameterException("String is null.");
		if (rootBlock == 0)
			throw new InvalidParameterException("A file must be accessed first.");
		
		String newString = this.readFile();
		newString += string;
		if(newString.length() > (bSize - 20) + (3 * bSize + 
				(bSize/4) * bSize) + ((bSize/4) * bSize)*(bSize/4))
			throw new InvalidParameterException("File is too large");
		
		this.deleteFile();
		this.createFile(newString);
	}
	
	/**
	 * Replaces the contents of an existing file with the given data.
	 * @param string data to be written to file.
	 * @throws InvalidParameterException whenever the string is null or when 
	 * method is called when no file has been initialized. 
	 */
	public void overwriteFile(String string) 
			throws InvalidParameterException, IllegalStateException {
		if(string == null)
			throw new InvalidParameterException("String is null.");
		if (rootBlock == 0)
			throw new IllegalStateException("A file must be accessed first.");
		
		this.deleteFile();
		this.createFile(string);
	}
	
	/**
	 * Deletes the current file from disk.
	 * @throws IllegalStateException whenever the method is called 
	 * 	without initializing a file first.
	 */
	public void deleteFile() throws IllegalStateException {
		if (rootBlock == 0)
			throw new IllegalStateException("A file must be accessed first.");
		
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);	
		disk.read(rootBlock, root);
		int i = bSize - 4;
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		int blockIndex = Utils.getIntFromBlock(root, i);
		
		while(i >= bSize - 20) {
			
			blockIndex = Utils.getIntFromBlock(root, i);
			if(blockIndex != 0) {
				if(i < bSize - 8) {
					disk.write(blockIndex, vdb);
					disk.registerFB(blockIndex);
				} 
				else if(i == bSize - 8) {
					OneLevelBlock oneBlock = new OneLevelBlock(disk);
					oneBlock.getOneLevelBlock(blockIndex);
					oneBlock.delete();
				}
				else if(i == bSize - 4){
					TwoLevelBlock twoBlock = new TwoLevelBlock(disk);
					twoBlock.getTwoLevelBlock(blockIndex);
					twoBlock.delete();
				}
			}
			i -= 4;
		}
		disk.setFirstFreeINode(fileNode);
		disk.write(rootBlock, vdb);
		disk.registerFB(rootBlock);
		
	}
	
	/**
	 * Reads the contents of the current file.
	 * @return the string containing the file's data.
	 * @throws IllegalStateException whenever the method is called 
	 * 	without initializing a file first.
	 */
	public String readFile() throws IllegalStateException {
		if (rootBlock == 0)
			throw new IllegalStateException("A file must be accessed first.");
		
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);	
		disk.read(rootBlock, root);
		int i = bSize - 20;
		
		String result = Utils.getStringFromBlock(root, i);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		int blockIndex = Utils.getIntFromBlock(root, i);
		
		for(i = bSize - 20; i < bSize; i += 4) {
			
			blockIndex = Utils.getIntFromBlock(root, i);
			if(blockIndex != 0) {
				if(i < bSize - 8) {
					disk.read(blockIndex, vdb);
					result += Utils.getStringFromBlock(vdb, bSize);
				} 
				else if(i == bSize - 8) {
					OneLevelBlock oneBlock = new OneLevelBlock(disk);
					oneBlock.getOneLevelBlock(blockIndex);
					result += oneBlock.readString();
				}
				else if(i == bSize - 4){
					TwoLevelBlock twoBlock = new TwoLevelBlock(disk);
					twoBlock.getTwoLevelBlock(blockIndex);
					result += twoBlock.readString();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * @return index of the file's root block
	 */
	public int getRootBlock() {
		return rootBlock;
	}
	
	/**
	 * @return I-Node object corresponding to the file.
	 */
	public INode getFileNode() {
		return fileNode;
	}
}
