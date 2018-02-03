package diskUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Scanner;
import exceptions.NonExistingDiskException;

/**
 * A list that keeps track of available DiskUnits and saves them
 * 	into a file for future use.
 */
public class DiskManager {
	
	private ArrayList<ManagerNode> diskList;
	private int numDisks;
	private String formatStr;
	private final String PATH = "DiskUnits\\DiskNames.txt";
	
	
	/**
	 * Initializes the file that contains the information
	 * 	of the disks and copies them to a list.
	 */
	public DiskManager() {
		
		File file = new File(PATH);
		formatStr = "%-15s %-10s %-10s";
		diskList = new ArrayList<ManagerNode>();
		
	    if (file.exists()) {
	    	Scanner read;
	    	try {
				read = new Scanner (file);
				read.useDelimiter(" , ");
				read.nextLine();

				while (read.hasNext() && read.hasNextLine())
				{
					String name = read.next().trim();
					String y = read.next().trim();
					String x = read.next().trim();
					diskList.add(new ManagerNode(name, Integer.parseInt(y), Integer.parseInt(x)));
					read.nextLine();
					numDisks++;
				}
				read.close();
			} catch (FileNotFoundException e) {
				System.out.println("Unable to open DiskNames.txt");
			}
	    } 
	}

	/**
	 * Adds a disk to the list of available disks.
	 * @param name name of the disk to be added.
	 * @param cap capacity of the given disk.
	 * @param bs size of the disk blocks.
	 */
	public void addDisk(String name, int cap, int bs) {
		numDisks++;
		diskList.add(new ManagerNode(name, cap, bs));
	}
	
	/**
	 * Retrieves the disk in the given index.
	 * @param index index to search for disk.
	 * @return an object containing the disk information
	 * @throws InvalidParameterException when the index of the list
	 * 	is out of bounds.
	 */
	public ManagerNode getDisk(int index) throws InvalidParameterException {
		if (index < 0 || index > numDisks)
			throw new InvalidParameterException("Invalid index = " + index);
		return diskList.get(index);
	}
	
	/**
	 * Removes a specified disk from the list of disks.
	 * @param name name of the disk that will be removed.
	 * @throws NonExistingDiskException whenever the list is empty
	 * 	or the disk is not found.
	 */
	public void removeDisk(String name) 
			throws NonExistingDiskException {
		if(numDisks == 0) 
			throw new NonExistingDiskException("There are no available disks.");
		
		int i = 0;
		ManagerNode disk = diskList.get(i);
		while(!disk.getName().contains(name) && i < numDisks)
			disk = diskList.get(++i);
		
		if(i >= numDisks)
			throw new NonExistingDiskException("Disk was not found");
		else {
			diskList.remove(i);
			numDisks--;
		}
	}
	
	/**
	 * Simulates closing the file.
	 */
	public void close() {
		saveList();
		diskList = null;
		numDisks = 0;
	}
	
	/**
	 * Saves the contents of the list into the file.
	 */
	private void saveList() {
		
		String header = String.format(formatStr, "Disk", "BlockSize", "Capacity");
		
		try {
			PrintWriter out = new PrintWriter(PATH, "UTF-8");
			out.println(header);
			for(ManagerNode node : diskList){
				out.println(String.format(formatStr, node.getName() + " , "
						, node.getBlocksize() + " , ", node.getCapacity() + " , "));
			}
			out.close();
		} catch (IOException x) {
			System.err.println(x);
		}
		
	}
	
	/**
	 * @return a nonnegative integer representing the amount of
	 * 	disks that are available.
	 */
	public int getNumberOfDisks() {
		return numDisks;
	}
	
	/**
	 * An object that stores the information of a disk including its 
	 * 	name, capacity and block size.
	 */
	public class ManagerNode {
		private String name;
		private int capacity;
		private int blocksize;
		
		/**
		 * Initializes the node with specified parameters.
		 * @param name name of the disk to be stored.
		 * @param capacity capacity of the disk.
		 * @param blockSize block size of the disk.
		 */
		private ManagerNode(String name, int capacity, int blockSize) {
			this.name = name;
			this.capacity = capacity;
			this.blocksize = blockSize;
		}

		/**
		 * @return name of the disk.
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name name to be given to disk.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return a nonnegative integer representing the capacity of
		 * 	the disk.
		 */
		public int getCapacity() {
			return capacity;
		}

		/**
		 * @param capacity capacity of the saved disk.
		 */
		public void setCapacity(int capacity) {
			this.capacity = capacity;
		}

		/**
		 * @return a nonnegative integer representing the block size of
		 * 	the disk.
		 */
		public int getBlocksize() {
			return blocksize;
		}

		/**
		 * @param blocksize block size of the saved disk.
		 */
		public void setBlocksize(int blocksize) {
			this.blocksize = blocksize;
		}
	}
}