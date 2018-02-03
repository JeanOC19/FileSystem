package testers;

import java.io.FileNotFoundException;
import java.io.IOException;

import diskUtilities.DataFile;
import diskUtilities.Directory;
import diskUtilities.DiskUnit;
import diskUtilities.INode;
import diskUtilities.OneLevelBlock;
import diskUtilities.TwoLevelBlock;
import diskUtilities.Utils;
import diskUtilities.VirtualDiskBlock;
import exceptions.ExistingDiskException;

public class DirectoryTester {
	
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
		
		String data = "";
		try {
			data = Utils.getStringFromFile("pelagato");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Directory dir = new Directory(d);
		dir.createDir("folder");
		
		for(i = 0; i < 15; i++) {
			dir.addFile("testfile" + i, 10);
		}
		
		try {
			for(i = 14; i >= 0; i--) {
				dir.removeFile("testfile" + i);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dir.deleteDir();
		
		
		
		
		
	
	}
}
