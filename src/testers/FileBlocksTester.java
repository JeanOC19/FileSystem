package testers;

import diskUtilities.DataFile;
import diskUtilities.DiskUnit;
import diskUtilities.INode;
import diskUtilities.OneLevelBlock;
import diskUtilities.TwoLevelBlock;
import diskUtilities.Utils;
import diskUtilities.VirtualDiskBlock;
import exceptions.ExistingDiskException;

public class FileBlocksTester {
	
	public static void main(String[] args) {
		
		String name = "test";
		try {
			DiskUnit.createDiskUnit(name, 64, 64);
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
		
		DataFile file = new DataFile(d);
		String str = "This is a test string to see if the data can be written and read.";
		System.out.println("String length is: " + str.length());
		
		file.createFile(str);
		System.out.println(file.readFile());
//		file.deleteFile();
		
		str = "You can modify the testers to make them more user-friendly. The " +
				"current versions of tester 1 and of tester 2 require some minor editing in" +
				" order to use it on a particular disk wanted. Just edit (inside the main method) " +
				"the line that \".mounts.\" the particular disk that you want to use. Remember that " +
				"tester 0 only creates the six disks mentioned above. Again, you can modify to " +
				"create others or to make those testers easier to use. You should run tester 1 " +
				"on a particular disk unit before running the tester 2 on that same unit. " +
				"As part of this exercise, you must figure out what each tester does and how it " +
				"works. Why is the output as it is on each case? Basically, we are creating a file. ";
		file.appendFile(str);
		System.out.println(file.readFile());
		
		file.deleteFile();
		
//		file.test(str);
		
//		TwoLevelBlock tlb = new TwoLevelBlock(d);
//		tlb.createTwoLevelBlock(1);
//		Utils.copyStringToBlock(vdb, 20, "First time ");
//		tlb.writeDiskBlock(0, vdb);
//		for(int j = 0; j < 16 ; j++) {
//			Utils.copyStringToBlock(vdb, 20, "Testing the name " + j);
//			Utils.copyIntToBlock(vdb, 20, i);
//			tlb.addDiskBlock(vdb);
//		}
//		tlb.deleteDiskBlock(5);
//		Utils.copyStringToBlock(vdb, 20, "Wrting over data...");
//		tlb.writeDiskBlock(4, vdb);
//		tlb.writeDiskBlock(9, vdb);
//		vdb = tlb.readDiskBlock(4);
//		System.out.println(Utils.getStringFromBlock(vdb, 20));
		
	
	}
}
