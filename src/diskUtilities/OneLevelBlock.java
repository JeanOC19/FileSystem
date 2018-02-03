package diskUtilities;

import java.security.InvalidParameterException;

public class OneLevelBlock {

	private int rootBlock;
	private int size;
	private int bSize;
	private DiskUnit disk;

	public OneLevelBlock(DiskUnit dUnit) {
		disk = dUnit;
		bSize = disk.getBlockSize();
		rootBlock = 0;
		size = 0;
	}

	public void createOneLevelBlock(int blocks) {
		if(blocks > bSize/4)
			throw new InvalidParameterException("One-level block can't have " + blocks + " blocks.");

		rootBlock = disk.getFreeBN();
		size = blocks;
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);

		for(int i = 0; i < bSize/4; i++) {
			if(i < blocks)
				Utils.copyIntToBlock(vdb, i * 4, disk.getFreeBN());
			else
				Utils.copyIntToBlock(vdb, i * 4, 0);
		}
		disk.write(rootBlock, vdb);
	}
	
	public void getOneLevelBlock(int rootIndex) {
		rootBlock = rootIndex;
		this.size = 0;
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(rootIndex, vdb);
		int i = 0;
		while(i < bSize/4 && Utils.getIntFromBlock(vdb, i * 4) != 0) {
			this.size++;
			i++;
		}
	}

	public void writeString(String string) throws InvalidParameterException {
		if(string.length() > size * bSize)
			throw new InvalidParameterException("String is longer than available size.");

		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		for(int i = 0; i < size; i++) {
			vdb = new VirtualDiskBlock(bSize);
			if(string.length() > bSize) {
				Utils.copyStringToBlock(vdb, bSize, string.substring(0, bSize));
				string = string.substring(bSize);
			} else
				Utils.copyStringToBlock(vdb, string.length(), string);
			disk.write(this.getDiskBlock(i), vdb);

		}

	}

	public void writeBlock(int index, VirtualDiskBlock data) throws InvalidParameterException {
		if(data == null)
			throw new InvalidParameterException("Disk block is null");
		if(index >= size)
			throw new InvalidParameterException("Invalid index =" + index);

		int block = this.getDiskBlock(index);
		disk.write(block, data);

	}

	public void addBlock(VirtualDiskBlock data) throws InvalidParameterException {
		if(data == null)
			throw new InvalidParameterException("Disk block is null");
		if(size >=  bSize/4)
			throw new InvalidParameterException("One-Level block is full.");

		VirtualDiskBlock root = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, root);
		Utils.copyIntToBlock(root, size*4, disk.getFreeBN());
		disk.write(rootBlock, root);
		size++;
		this.writeBlock(size - 1, data);

	}
	
	public void deleteBlock(int index) throws InvalidParameterException {
		if(index >= size)
			throw new InvalidParameterException("Invalid index value = " + index);
		
		int blockIndex = this.getDiskBlock(index);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.write(blockIndex, vdb);
		disk.registerFB(blockIndex);
		
		blockIndex = this.getDiskBlock(size - 1);
		disk.read(rootBlock, vdb);
		Utils.copyIntToBlock(vdb, index * 4, blockIndex);
		Utils.copyIntToBlock(vdb, (size - 1) * 4, 0);
		disk.write(rootBlock, vdb);
		size--;
	}

	public VirtualDiskBlock readBlock(int index) throws InvalidParameterException {
		if(index >= size)
			throw new InvalidParameterException("Invalid index value = " + index);
		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(this.getDiskBlock(index), vdb);
		return vdb;
	}

	public String readString() throws InvalidParameterException {
		if(rootBlock == 0)
			throw new InvalidParameterException("A block must be read or created first.");

		String result = "";
		for(int i = 0; i < size; i++) {
			VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
			disk.read(this.getDiskBlock(i), vdb);
			result += Utils.getStringFromBlock(vdb, bSize);
		}
		return result;
	}

	public void delete() {
		if(rootBlock == 0)
			throw new InvalidParameterException("A block must be read or created first.");

		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize); //An empty VDB
		for(int i = size - 1; i >= 0; i--) {
			int index = this.getDiskBlock(i);
			disk.write(index, vdb);
			disk.registerFB(index);
		}
		disk.write(rootBlock, vdb);
		disk.registerFB(rootBlock);
	}

	public int getDiskBlock(int index) {
		if(index >= size || index < 0)
			throw new IndexOutOfBoundsException("Invalid one-level block index =" + index);

		VirtualDiskBlock vdb = new VirtualDiskBlock(bSize);
		disk.read(rootBlock, vdb);
		return Utils.getIntFromBlock(vdb, index * 4);
	}

	public int getRootBlock() {
		return rootBlock;
	}

	public int getSize() {
		return size;
	}

}