package diskUtilities;

import java.security.InvalidParameterException;

public class TwoLevelBlock {

	private int rootBlock;
	private int numOfOneBlocks;
	private int size;
	private int bSize;
	private DiskUnit disk;

	public TwoLevelBlock(DiskUnit dUnit) {
		disk = dUnit;
		bSize = dUnit.getBlockSize();
		rootBlock = 0;
		numOfOneBlocks = 0;
	}

	/**
	 * 
	 * @param index number of disk blocks needed
	 */
	public void createTwoLevelBlock(int index) {

		rootBlock = disk.getFreeBN();
		size = index;
		numOfOneBlocks = index / ((bSize/4));
		if(index % (bSize/4) != 0)
			numOfOneBlocks++;
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		OneLevelBlock oneBlock = new OneLevelBlock(disk);
		
		//Create all full one-level blocks and save index to two-level block
		for(int i = 0; i < bSize/4; i++) {
			if(i < numOfOneBlocks - 1) {
				oneBlock.createOneLevelBlock(bSize/4);
				Utils.copyIntToBlock(vdb, i * 4, oneBlock.getRootBlock());
			} else
				Utils.copyIntToBlock(vdb, i * 4, 0);
		}

		//Create a partially filled one-level block.
		if(index % (bSize/4) != 0) {
			oneBlock.createOneLevelBlock(index % (bSize/4));
			Utils.copyIntToBlock(vdb, (numOfOneBlocks - 1) * 4, oneBlock.getRootBlock());
		}
	
		//Save two-level block to disk
		disk.write(rootBlock, vdb);
	}

	public void getTwoLevelBlock(int rootIndex) {
		rootBlock = rootIndex;
		numOfOneBlocks = 0;
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(rootIndex, vdb);
		int i = 0;
		while(i < bSize && Utils.getIntFromBlock(vdb, i) != 0) {
			numOfOneBlocks++;
			i+= 4;
		}
		
		OneLevelBlock olb = new OneLevelBlock(disk);
		olb.getOneLevelBlock(Utils.getIntFromBlock(vdb, (numOfOneBlocks - 1) * 4));
		size = (numOfOneBlocks) * (bSize/4) + olb.getSize();

	}

	public void writeString(String string) {
		if(string.length() > numOfOneBlocks * (bSize/4) * bSize)
			throw new InvalidParameterException("String is longer than available size.");

		OneLevelBlock oneBlock = new OneLevelBlock(disk);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, vdb);
		int substringSize = (bSize/4) * bSize;

		for(int i = 0; i < numOfOneBlocks - 1; i++) {
			oneBlock.getOneLevelBlock(Utils.getIntFromBlock(vdb, i * 4));
			oneBlock.writeString(string.substring(i * substringSize, (i + 1) * substringSize));
		}

		oneBlock.getOneLevelBlock(Utils.getIntFromBlock(vdb, (numOfOneBlocks - 1) * 4));
		oneBlock.writeString(string.substring((substringSize * (numOfOneBlocks - 1))));

	}

	public String readString() throws InvalidParameterException {
		if(rootBlock == 0)
			throw new InvalidParameterException("A block must be read or created first.");

		String result = "";
		for(int i = 0; i < numOfOneBlocks; i++) {
			OneLevelBlock oneBlock = this.getOneLevelBlock(i);
			result += oneBlock.readString();
		}
		return result;
	}

	public void delete() {

		for(int i = numOfOneBlocks - 1; i >= 0; i--) {
			OneLevelBlock oneBlock = this.getOneLevelBlock(i);
			oneBlock.delete();
		}

		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.write(rootBlock, vdb);
		disk.registerFB(rootBlock);

	}

	public void addDiskBlock(VirtualDiskBlock vdb) {
		if(numOfOneBlocks >= bSize/4)
			throw new InvalidParameterException("Two level block is full.");

		OneLevelBlock oneBlock = this.getOneLevelBlock(numOfOneBlocks - 1);
		
		if(oneBlock.getSize() >= bSize/4) {
			oneBlock.createOneLevelBlock(1);
			oneBlock.writeBlock(0, vdb);
			this.addOneLevelBlock(oneBlock);
		} else
			oneBlock.addBlock(vdb);
		size++;

	}
	
	public VirtualDiskBlock readDiskBlock(int index) {
		if(index >= size || size < 0)
				throw new InvalidParameterException("Invalid index =" + index);
		
		int tlbIndex = index / (bSize/4);
		
		OneLevelBlock olb = this.getOneLevelBlock(tlbIndex);
		return olb.readBlock(index % (bSize/4));
	}
	
	public void deleteDiskBlock(int index) {
		if(index >= size || index < 0)
			throw new InvalidParameterException("Invalid index =" + index);

		int tlbIndex = index / (bSize/4);

		OneLevelBlock olb = this.getOneLevelBlock(tlbIndex);
		olb.deleteBlock((index % (bSize/4)));
		
		if(olb.getSize() == 0)
			this.deleteOneLevelBlock(tlbIndex);
		else {
			OneLevelBlock lastOLB = this.getOneLevelBlock(numOfOneBlocks - 1);
			int lastBlock = lastOLB.getDiskBlock(lastOLB.getSize() - 1);
			VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
			disk.read(lastBlock, vdb);
			olb.addBlock(vdb);
			lastOLB.deleteBlock(lastOLB.getSize() - 1);
			
			if(lastOLB.getSize() == 0) 
				this.deleteOneLevelBlock(numOfOneBlocks - 1); 
		}
		size--;		
	}

	public void writeDiskBlock(int index, VirtualDiskBlock block) {
		if(index >= size || index < 0)
			throw new InvalidParameterException("Invalid index =" + index);

		int tlbIndex = index / (bSize/4);
		System.out.println("One-block number is " + tlbIndex);
		System.out.println("Index in block is " + index % (bSize/4));
		OneLevelBlock olb = this.getOneLevelBlock(tlbIndex);
		olb.writeBlock(index % (bSize/4), block);
		size++;
	}
	
	private void addOneLevelBlock(OneLevelBlock olb) {
		if(numOfOneBlocks >= bSize/4)
			throw new InvalidParameterException("Two-level Block is full.");

		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, root);
		Utils.copyIntToBlock(root, numOfOneBlocks * 4, olb.getRootBlock());
		disk.write(rootBlock, root);
		numOfOneBlocks++;
	}
	
	private void deleteOneLevelBlock(int index) {
		if(index >= bSize/4)
			throw new InvalidParameterException("Two-level Block is full.");
		
		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, root);
		int blockIndex = Utils.getIntFromBlock(root, index * 4);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		OneLevelBlock olb = new OneLevelBlock(disk);
		olb.getOneLevelBlock(blockIndex);
		olb.delete();
		Utils.copyIntToBlock(root, index * 4, 
				Utils.getIntFromBlock(vdb, (numOfOneBlocks - 1) * 4));
		Utils.copyIntToBlock(root, (numOfOneBlocks - 1) * 4, 0);
		disk.write(rootBlock, root);
		numOfOneBlocks--;
	}

	public OneLevelBlock getOneLevelBlock(int index) {

		if(index >= numOfOneBlocks)
			throw new IndexOutOfBoundsException("Invalid two-level block index = " + index);

		OneLevelBlock oneBlock = new OneLevelBlock(disk);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, vdb); 			//Read the two-level block from disk
		//Read index of one-level block and return its object.
		oneBlock.getOneLevelBlock(Utils.getIntFromBlock(vdb, index * 4));
		return oneBlock;
	}

	public int getRootBlock() {
		return rootBlock;
	}

	public int getNumOfOneBlocks() {
		return numOfOneBlocks;
	}
	
	public int getSize() {
		return size;
	}
	
}