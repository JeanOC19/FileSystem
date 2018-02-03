package testers;

import diskUtilities.DiskUnit;
import diskUtilities.INode;
import diskUtilities.Utils;
import diskUtilities.VirtualDiskBlock;
import exceptions.ExistingDiskException;

public class FileSystemTester {
	
	public static void main(String[] args) {
		
		String name = "woop";
		try {
			DiskUnit.createDiskUnit(name, 32, 32);
		} catch (ExistingDiskException e) {
			System.out.println("Disk already exists.");
		}
		DiskUnit d = DiskUnit.mount(name);
		
	    showDiskContent(d); 
		 
		d.shutdown(); 
	}

	private static void showDiskContent(DiskUnit d) {
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
		d.read(0, vdb);
		
		int i = Utils.getIntFromBlock(vdb, 0);
		System.out.println("Capacity of disk is " + i);
		i = Utils.getIntFromBlock(vdb, 4);
		System.out.println("BlockSize of disk is " + i);
		i = Utils.getIntFromBlock(vdb, 8);
		System.out.println("Index of first free block is " + i);
		i = Utils.getIntFromBlock(vdb, 12);
		System.out.println("Index inside free block " + i);
		i = Utils.getIntFromBlock(vdb, 16);
		System.out.println("First free INode " + i);
		i = Utils.getIntFromBlock(vdb, 20);
		System.out.println("Number of INodes " + i);
		
		System.out.println("///////////////////////////////////////////");
		
		d.read(1, vdb);
		i = Utils.getIntFromBlock(vdb, 0);
		System.out.println("File location is: " + i);
		i = Utils.getIntFromBlock(vdb, 4);
		System.out.println("File size is: " + i);
		i = (int) vdb.getElement(8);
		if(i == 1)
			System.out.println("This is a directory.");
		else
			System.out.println("This is a file.");
		
		System.out.println("//////////////////////////////////////////");
		
		INode node = d.getINode(0);
		System.out.println("File location is: " + node.getBlockIndex());
		System.out.println("File size is: " + node.getSize());
		i = (int) node.getType();
		if(i == 1)
			System.out.println("This is a directory.");
		else
			System.out.println("This is a file.");
		
		System.out.println("//////////////////////////////////////////");
		
		for(int j = 0; j < d.getNumOfINodes(); j++) {
			node = d.getINode(j);
			System.out.println("INode index is " + node.getBlockIndex());
			System.out.println("INode size is " + node.getSize());
		}
		
		
		
	}

}
