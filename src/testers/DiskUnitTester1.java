package testers;

import diskUtilities.DiskUnit;
import diskUtilities.Utils;
import diskUtilities.VirtualDiskBlock;

public class DiskUnitTester1 {

	/**
	 * Tests reading operation contents of DiskUnit.
	 * @param args
	 */
	public static void main(String[] args) {
		DiskUnit d = DiskUnit.mount("lola"); // edit the name of the disk to mount
		
	    showDiskContent(d); 
		
		showFileInDiskContent(d);   
		d.shutdown(); 
	}

	/**
	 * Method to display the data in the disk in its logical order. It does not 
	 * include empty blocks or blocks that do not follow the data's sequence.
	 * @param d disk to be read.
	 */
	private static void showFileInDiskContent(DiskUnit d) { 
		VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize()); 
		
		System.out.println("\nContent of the file begining at block 1"); 
		int bn = 1; 
		while (bn != 0) { 
			d.read(bn, vdb); 
			showVirtualDiskBlock(bn, vdb);
			bn = getNextBNFromBlock(vdb);			
		}
		
	}

	 /**
	  * Displays the current contents of the disk by accessing each block
	  * residing inside the disk.
	  * @param d disk which contents will be displayed.
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
	  * Shows current contents of the specified block inside the disk.
	  * @param b number of block to be read.
	  * @param block block containing data to be displayed.
	  **/
	private static void showVirtualDiskBlock(int b, VirtualDiskBlock block) {
	    System.out.print(" Block "+ b + "\t"); 
	    for (int i=0; i<block.getCapacity(); i++) {
	    	char c = (char) block.getElement(i); 
	    	if (Character.isLetterOrDigit(c) || Character.isWhitespace(c))
	    		System.out.print(c); 
	    	else if(i <= block.getCapacity() - 4 && Utils.getIntFromBlock(block, i) != 0) {
	    		int value = Utils.getIntFromBlock(block, i);
	    		System.out.print(value);   	
	    		block.setElement(i+3,(byte)0);
	    		block.setElement(i+2, (byte)0);
	    		block.setElement(i+1,(byte)0);
	    		if(value >= 100) {
	    			i+=2;
	    		} else if(value >= 10)
	    			i+=1;
	    	} else
	    		System.out.print('-'); 
	    }
	    System.out.println(); 
	}


	/** 
	 * Takes a block (vdb) and saves the address of the block (value) that contains 
	 * the remainder of the data in the last 4 bytes of the specified block.
	 * @param vdb block to be written to.
	 * @param value address of next block.
	 */
	public static void copyNextBNToBlock(VirtualDiskBlock vdb, int value) { 
		int lastPos = vdb.getCapacity()-1;

		for (int index = 0; index < 4; index++) { 
			vdb.setElement(lastPos - index, (byte) (value & 0x000000ff)); 	
			value = value >> 8; 
		}

	}
	
	/** 
	 * Takes a block (vdb) and gets the number of the block that
	 * contains the next chunk of data.
	 * @param vdb block whose next wants to be found.
	 * @return block that comes after vdb.
	 */
	private static int getNextBNFromBlock(VirtualDiskBlock vdb) { 
		int bsize = vdb.getCapacity(); 
		int value = 0; 
		int lSB; 
		for (int index = 3; index >= 0; index--) { 
			value = value << 8; 
			lSB = 0x000000ff & vdb.getElement(bsize-1-index);
			value = value | lSB; 
		}
		return value; 

	}

}

