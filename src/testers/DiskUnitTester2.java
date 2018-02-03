package testers;

import java.security.InvalidParameterException;

import diskUtilities.DiskUnit;
import diskUtilities.VirtualDiskBlock;
import exceptions.ExistingDiskException;

public class DiskUnitTester2 {

	 /**
	  * Tests low level format operation of DiskUnit and different exceptions
	  * that can be thrown by DiskUnit or VirtualDiskBlock
	  * @param args
	  **/
	public static void main(String[] args) {
		String disk = "disk6";
		try {
			DiskUnit.createDiskUnit(disk, 32, 16);
		} catch (InvalidParameterException | ExistingDiskException e) {
			e.toString();
		}
		DiskUnit d = DiskUnit.mount("disk6");
		//Choose 3 random blocks to write to
		int count = 0;
		while(count < 3) {
			writeHelloWorld(d, (int) ( 1 + Math.random() * (d.getCapacity() - 2)));
			count++;
		}
		//Show disk contents before and after formatting.
		showDiskContent(d);
		testDiskFormatting(d);
		d.shutdown();
		
		testExceptions(disk);
	}

	 /**
	  * Tests different exception by creating disks that already exist, mounting
	  * non existing disks and by trying to write to block 0.
	  * @param d name of the disk whose block 0 will be written.
	  **/
	private static void testExceptions(String d) {
		System.out.println("\nTesting various possible exceptions:");
		DiskUnit disk;
		VirtualDiskBlock vdb = new VirtualDiskBlock();
		try {
			disk = DiskUnit.mount("disk90");
		} catch (RuntimeException e) {
			e.getMessage();
			System.out.println(e);
		}
		
		try {
			DiskUnit.createDiskUnit("disk6");
		} catch(ExistingDiskException | InvalidParameterException e) {
			e.getMessage();
			System.out.println(e);
		}
		
		try {
			DiskUnit.createDiskUnit("disk90", 11, 11);
		} catch(ExistingDiskException | InvalidParameterException e) {
			e.getMessage();
			System.out.println(e);
		}
		
		try {
			disk = DiskUnit.mount(d);
			disk.write(0, vdb);
		} catch(RuntimeException e) {
			e.getMessage();
			System.out.println(e);
		}
	}

	 /**
	  * Tests formatting by showing contents of disk after executing the operation.
	  * @param d disk to be formatted.
	  **/
	private static void testDiskFormatting(DiskUnit d) {
		System.out.println("\nTesting disk formatting:");
		d.lowLevelFormat();
		showDiskContent(d);
	}

	 /**
	  * Writes an example text to the disk at the specified block.
	  * @param d disk to be written to.
	  * @param blockNum block to be written into disk.
	  **/
	private static void writeHelloWorld(DiskUnit d, int blockNum) {
		String s = "Hello!!";
		VirtualDiskBlock db = new VirtualDiskBlock(d.getBlockSize());
		for(int i = 0; i < d.getBlockSize(); i++ ) {
			if(s.length() < i + 1)
				db.setElement(i, (byte) 0);
			else
				db.setElement(i, (byte) s.charAt(i));
		}
		d.write(blockNum, db);
		
	}
	
	 /**
	  * Shows current contents of the disk by accessing each block. Method from DiskUnitTester1.
	  * @param d disk which contents will be displayed
	  **/
	private static void showDiskContent(DiskUnit d) { 
		
		System.out.println("Capacity of disk is: " + d.getCapacity()); 
		System.out.println("Size of blocks in the disk is: " + d.getBlockSize()); 
		
		VirtualDiskBlock block = new VirtualDiskBlock(d.getBlockSize()); 
		for (int b = 0; b < d.getCapacity(); b++) { 
			d.read(b, block); 
			showVirtualDiskBlock(b, block); 
		}
		
	}
	
	 /**
	  * Shows current contents of the specified block. Method from DiskUnitTester1.
	  * @param b number of block to be displayed from disk.
	  * @param block block containing data to be displayed.
	  **/
	private static void showVirtualDiskBlock(int b, VirtualDiskBlock block) {
	    System.out.print(" Block "+ b + "\t"); 
	    for (int i=0; i<block.getCapacity(); i++) {
	    	char c = (char) block.getElement(i); 
	    	if (Character.isLetterOrDigit(c))
	    		System.out.print(c); 
	    	else
	    		System.out.print('-'); 
	    }
	    System.out.println(); 
	}
}
