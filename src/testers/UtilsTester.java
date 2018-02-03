package testers;

import java.io.File;
import java.io.IOException;

import diskUtilities.Utils;
import diskUtilities.VirtualDiskBlock;

public class UtilsTester {
	
	/**
	 * Tests the methods in Util class by sending different possible parameter values. 
	 * @param args
	*/
	public static void main(String[] args) {
		
		System.out.println("Testing Util class");
		
		testPowerOf2();
		String data = "";
		try {
			data = Utils.getStringFromFile("inputfile4");
			System.out.println(data);
		} catch (IOException e) {
			System.out.println("Unable to read file.");
		}
		
		File dir = new File("DiskUnits");
		boolean dirMaker = dir.mkdir();
		System.out.println(dirMaker);
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(64);
		System.out.println(Utils.isBlockEmpty(vdb));
		Utils.copyIntToBlock(vdb, 20, 3);
		System.out.println(Utils.isBlockEmpty(vdb));
		Utils.copyIntToBlock(vdb, 20, 0);
		System.out.println(Utils.isBlockEmpty(vdb));
		Utils.copyStringToBlock(vdb, 10, "        ");
		System.out.println(Utils.isBlockEmpty(vdb));

	}
	
	/**
	 * Sends a list of possible values to powerOf2 method and displays the method
	 * result for each value.
	*/
	private static void testPowerOf2() {
		for(int i = 0; i <= 32; i++) {
			System.out.println( i + " is a power of 2? " + Utils.powerOf2(i));
		}
	}
	
	
}
