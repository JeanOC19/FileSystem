package testers;

import diskUtilities.DiskManager;

public class DiskManagerTester {
	
	public static void main(String[] args) {
		
		DiskManager dMan = new DiskManager();
//		dMan.addDisk("disk1", 32, 32);
//		dMan.addDisk("disk5", 32, 32);
//		dMan.addDisk("discoPrueba", 128, 64);
//		System.out.println(dMan.getDisk(1).getName());
//		System.out.println(dMan.getDisk(1).getBlocksize());
//		System.out.println(dMan.getDisk(1).getCapacity());
//		dMan.close();
		
		dMan = new DiskManager();
		System.out.println(dMan.getDisk(0).getName());
		System.out.println("Capacity of disk: " + dMan.getDisk(0).getBlocksize());
		System.out.println("BlockSize of disk: " + dMan.getDisk(0).getCapacity());
		System.out.println();
		
		System.out.println(dMan.getDisk(1).getName());
		System.out.println("Capacity of disk: " + dMan.getDisk(1).getBlocksize());
		System.out.println("BlockSize of disk: " + dMan.getDisk(1).getCapacity());
		System.out.println("Number of disks: " + dMan.getNumberOfDisks());
		try {
			dMan.removeDisk("lolapalooza");
		} catch (Exception e) {
			System.out.println("Disk was not found.");
		}
		dMan.close();
	}

}
