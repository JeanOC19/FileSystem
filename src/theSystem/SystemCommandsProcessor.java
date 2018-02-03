package theSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import diskUtilities.DataFile;
import diskUtilities.Directory;
import diskUtilities.DirectoryManager;
import diskUtilities.DiskManager;
import diskUtilities.DiskManager.ManagerNode;
import diskUtilities.DiskUnit;
import diskUtilities.INode;
import diskUtilities.Utils;
import systemGeneralClasses.Command;
import systemGeneralClasses.CommandActionHandler;
import systemGeneralClasses.CommandProcessor;
import systemGeneralClasses.FixedLengthCommand;
import systemGeneralClasses.SystemCommand;
import stack.IntStack;


/**
 * Processes user input commands and calls the corresponding 
 * 	classes that manage and change disk contents.
 * @author Pedro I. Rivera-Vega
 *
 */
public class SystemCommandsProcessor extends CommandProcessor { 
	
	private static final String FILEPATH = "DiskUnits/";
	private ArrayList<String> resultsList; 
	private DirectoryManager dirManager;
	private Directory currDirectory;
	private DiskUnit dUnit;
	
	SystemCommand attemptedSC; 

	boolean stopExecution; 

	/**
	 *  Initializes the list of possible commands for each of the
	 *  states the system can be in. 
	 */
	public SystemCommandsProcessor() {
		
		// stack of states
		currentState = new IntStack(); 
		
		// The system may need to manage different states. For the moment, we
		// just assume one state: the general state. The top of the stack
		// "currentState" will always be the current state the system is at...
		currentState.push(GENERALSTATE); 

		// Maximum number of states for the moment is assumed to be 1
		// this may change depending on the types of commands the system
		// accepts in other instances...... 
		createCommandList(1);    // only 1 state -- GENERALSTATE

		add(GENERALSTATE, SystemCommand.getFLSC("showdisks", new ShowDisksProcessor())); 		
		add(GENERALSTATE, SystemCommand.getFLSC("createdisk name nblocks bsize", new CreateDiskProcessor())); 
		add(GENERALSTATE, SystemCommand.getFLSC("deletedisk name", new DeleteDiskProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("mount name ", new MountProcessor())); 
		add(GENERALSTATE, SystemCommand.getFLSC("unmount", new UnmountProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("loadfile file name", new LoadFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cp file name", new CopyFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("ls", new ListFilesProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cat name", new DisplayContentProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cd dir", new ChangeDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("mkdir name", new CreateDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("rmdir name", new RemoveDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("drmdir name", new RecRemoveDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("rm name", new RemoveFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("find name", new FindFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("append file name", new AppendFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("help", new HelpProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("exit", new ShutDownProcessor())); 
				
		//Set to execute
		stopExecution = false; 
		
		//Create directory
		File dir = new File("DiskUnits");
		dir.mkdir();

	}
		
	public ArrayList<String> getResultsList() { 
		return resultsList; 
	}
	
	/**
	 * Command for shutting down the command system.
	 */
	private class ShutDownProcessor implements CommandActionHandler { 
		/**
		 * Shuts down the command system and unmounts any mounted disk.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) { 

			resultsList = new ArrayList<String>(); 
			resultsList.add("System is shutting down...");
			if(dUnit != null) {
				dUnit.shutdown();
				dirManager = null;
				currDirectory = null;
				dUnit = null;
			}
			stopExecution = true;
			return resultsList; 
		}
	}

	/**
	 * Command for displaying the list of available disks.
	 */
	private class ShowDisksProcessor implements CommandActionHandler {
		/**
		 * Calls DiskManager and shows the list's contents.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			
			String name = null;
			if(dUnit != null) {
				name = dUnit.getDiskName();
			}
				
			DiskManager dManager = new DiskManager();
			
			if(dManager.getNumberOfDisks() == 0) {
				resultsList.add("No disks are available");
				return resultsList;
			} else {
				for(int i = 0; i < dManager.getNumberOfDisks(); i++) {
					ManagerNode dInfo = dManager.getDisk(i);
					if(name != null && dInfo.getName().contains(name)) {
						resultsList.add("	" + dInfo.getName() + "   " + dInfo.getCapacity() 
							+ "   " + dInfo.getBlocksize() + "   mounted");
					} else {
						resultsList.add("	" + dInfo.getName() + "   " + dInfo.getCapacity() 
						+ "   " + dInfo.getBlocksize() + "   unmounted");
					}
				}
			}
			dManager.close();
			
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to create a new disk.
	 */
	private class CreateDiskProcessor implements CommandActionHandler {
		/**
		 * Creates a new DiskUnit object and adds the disk to the
		 * list of available disks.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 

			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			if(name.length() > 12) {
				resultsList.add("Disk name is too long");
				return resultsList;
			}
			int nblocks = Integer.parseInt(fc.getOperand(2));
			int bsize = Integer.parseInt(fc.getOperand(3));
			
			try {
				DiskManager dManager = new DiskManager();
				if(dManager.getNumberOfDisks() >= 20) {
					resultsList.add("No more disks can be created");
					dManager.close();
					return resultsList;
				}
				DiskUnit.createDiskUnit(name, nblocks, bsize);
				resultsList.add(FILEPATH + name);
				resultsList.add("New disk succesfully created: " + name);
				dManager.addDisk(name, nblocks, bsize);
				dManager.close();
			} catch (Exception e) {
				resultsList.add(e.getMessage());
			}
			
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to delete an existing disk.
	 */
	private class DeleteDiskProcessor implements CommandActionHandler {
		/**
		 * Looks for disk, deletes the DiskUnit object and removes
		 * it from the list of disks.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			
			if(dUnit != null && dUnit.getDiskName() != name) {
				resultsList.add(name + " must be unmounted first.");
				return resultsList;
			}
			
			try {
				DiskUnit.deleteDisk(name);
				DiskManager dManager = new DiskManager();
				dManager.removeDisk(name);
				dManager.close();
			} catch (Exception e) {
				resultsList.add(e.getMessage());
				return resultsList;
			}
			resultsList.add(name + " was deleted succesfully.");

			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to mount an existing disk and make it usable.
	 */
	private class MountProcessor implements CommandActionHandler {
		/**
		 * Mounts the existing disk if no disk is currently
		 * mounted.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			
			if(dUnit != null) {
				resultsList.add("A disk is already mounted");
				return resultsList;
			}
			try {
				dUnit = DiskUnit.mount(name);	
				resultsList.add(name + " was mounted succesfully");
			} catch (Exception e) {
				resultsList.add("Unable to mount " + name );
				resultsList.add(e.getMessage());
				return resultsList;
			}
			
			//Creates a directory manager and sets up the root directory
			dirManager = new DirectoryManager(dUnit.getINode(0));
			currDirectory = new Directory(dUnit, dUnit.getINode(0));

			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to unmount currently mounted disk.
	 */
	private class UnmountProcessor implements CommandActionHandler {
		/**
		 * Unmounts disk if a disk is mounted.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}

			try {
				dUnit.shutdown();
				resultsList.add("Succesfully unmounted disk.");
			} catch (Exception e) {
				resultsList.add("Unable to unmount disk.");
			}
			dUnit = null;
			dirManager = null;
			currDirectory = null;
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to load an external file into the mounted disk.
	 */
	private class LoadFileProcessor implements CommandActionHandler {
		/**
		 * Looks for the external file and writes it to the disk
		 *  with the specified filename as long as there is 
		 *  available space.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(2);
			String filename = fc.getOperand(1);
			
			if(name.length() > 20) {
				resultsList.add("Name is too long.");
				return resultsList;
			}
			
			//Read file data then check if there is enough space to store file.
			try {
				String data = Utils.getStringFromFile(filename);
				DataFile file = new DataFile(dUnit);
				
				if(currDirectory.getFileNode(name) != null) {
					file = new DataFile(dUnit, currDirectory.getFileNode(name));
					file.overwriteFile(data);
				} else {
					if(!dUnit.checkIfEnoughSpace(data.length()/(dUnit.getBlockSize()-4) + 1)) {
						resultsList.add("Not enough space in disk");
						return resultsList;
					}
					
					file.createFile(data);
					currDirectory.addFile(name, file.getFileNode().getNodeIndex());
				}
			} catch (IOException e) {
				resultsList.add(e.getMessage());
				return resultsList;
			}
			
			resultsList.add("Copied " + filename + " to " + dUnit.getDiskName());
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to make a copy of an internal file in the mounted disk.
	 */
	private class CopyFileProcessor implements CommandActionHandler {
		/**
		 * Looks for the specified file name and makes a copy with
		 * 	the new name given inside the disk.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
		
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(2);

			if(name.length() > 20) {
				resultsList.add("Name is too long.");
				return resultsList;
			}
			try {
				INode targetINode = currDirectory.getFileNode(fc.getOperand(1));
				DataFile oldFile = new DataFile(dUnit, targetINode);
				DataFile newFile = new DataFile(dUnit);
				newFile.createFile(oldFile.readFile());
				currDirectory.addFile(name, newFile.getFileNode().getNodeIndex());
			} catch (Exception e) {
				resultsList.add(e.getMessage());
				return resultsList;
			}
			
			resultsList.add(fc.getOperand(1) + " was copied");
				
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to show the files in the current working directory 
	 * 	of the disk.
	 */
	private class ListFilesProcessor implements CommandActionHandler {
		/**
		 * Reads the directory file and displays the file names,
		 * 	sizes and file types.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			return currDirectory.getFiles(); 
		} 
		
	}
	
	/**
	 * Command to display the contents of a specified file
	 * 	in the disk.
	 */
	private class DisplayContentProcessor implements CommandActionHandler {
		/**
		 * Looks for file in the working directory and displays
		 * 	its contents.
		 * @param c input to be read.
		 * @return message to display to user.
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			
			try {
				INode fileNode = currDirectory.getFileNode(name);
				DataFile file = new DataFile(dUnit, fileNode);
				String contents = file.readFile();
				int length = contents.length()/48;
				if(contents.length() % 48 != 0)
					length++;
				
				for(int i = 0; i < length; i++) {
					if(i == length - 1) 
						resultsList.add(contents.substring(i * 48));
					else
						resultsList.add(contents.substring(i * 48, (i + 1) * 48));
				}
			} catch (Exception e) {
				resultsList.add(e.getMessage());
			}
			
			return resultsList; 
		} 
		
	}

	/**
	 * Command to change the current working directory in the disk.
	 * @author jeano
	 *
	 */
	private class ChangeDirectoryProcessor implements CommandActionHandler {

		/**
		 * Looks for the directory in the current folder and
		 * 	sets it as the current directory.
		 * @param c command with the directory name.
		 * @return if command was executed properly or not
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			INode dirNode = currDirectory.getFileNode(name);
			
			if(name.equals("..") && dirManager.getCurrentDirectoryName() != "root") {
				dirManager.removeDirFromPath();
				currDirectory = new Directory(dUnit, dirManager.getCurrentDirectoryNode());
			} else if(dirNode == null || dirNode.getType() == (byte) 0) {
				resultsList.add("No such folder in current directory.");
				return resultsList;
			} else {
				currDirectory = new Directory(dUnit, dirNode);
				dirManager.addDirToPath(name, dirNode);
			}
			resultsList.add(dirManager.getDirPath());
			
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to create a new folder in the current directory.
	 * @author jeano
	 *
	 */
	private class CreateDirectoryProcessor implements CommandActionHandler {

		/**
		 * Executes the command by creating the folder if no 
		 * 	folder exists with that name.
		 * @param c command with name of folder to create
		 * @return if command was executed properly or not
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			
			if(name.length() > 20) {
				resultsList.add("Name is too long");
				return resultsList;
			}
			
			INode dirNode = currDirectory.getFileNode(name);
			if(dirNode != null) {
				resultsList.add("File with that name already exists in folder");
				return resultsList;
			}
			
			Directory newDir = new Directory(dUnit);
			newDir.createDir(name);
			currDirectory.addFile(name, newDir.getDirNode().getNodeIndex());
			
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to remove a folder from the current directory.
	 * @author jeano
	 *
	 */
	private class RemoveDirectoryProcessor implements CommandActionHandler {

		/**
		 * Executes the command by deleting the folder if it's found in 
		 * 	the current directory.
		 * @param c command with name of folder to delete
		 * @return if command was executed properly or not
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			
			INode node = currDirectory.getFileNode(name);
			if(node == null) {
				resultsList.add("No such directory in current directory");
				return resultsList;
			} else if(node.getType() == (byte) 0) {
				resultsList.add("Name corresponds to a data file.");
				return resultsList;
			} else if(node.getSize() != 0) {
				resultsList.add("Directory is not empty.");
				return resultsList;
			}
			
			try {
				Directory dir = new Directory(dUnit, node);
				dir.deleteDir();
				currDirectory.removeFile(name);
			} catch (FileNotFoundException e) {
				resultsList.add(e.getMessage());
			}
				
			
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to remove a desired folder recursively and all its 
	 * 	contents in case its not empty.
	 * @author jeano
	 *
	 */
	private class RecRemoveDirectoryProcessor implements CommandActionHandler {

		/**
		 * Executes the command by deleting the folder if its found.
		 * @param c command with name of folder to delete
		 * @return if command was executed properly or not
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			INode node  = currDirectory.getFileNode(name);
			
			try {
				if(node == null || !currDirectory.isFileDirectory(name)) {
					resultsList.add("No such folder in current directory.");
					return resultsList;
				}
				
				this.deleteFolder(new Directory(dUnit, node));
				currDirectory.removeFile(name);
				
			} catch (FileNotFoundException e) {
				resultsList.add(e.getMessage());
			}
			return resultsList; 
		} 
		
		/**
		 * Recursively deletes the desired folder and all its contents. 
		 * @param dir directory to delete.
		 */
		private void deleteFolder(Directory dir) {
			
			ArrayList<String> filesList = dir.getFiles();
			try {
				for(int i = 0; i < filesList.size(); i++) {
					INode node = dir.getFileNode(filesList.get(i));
					if(dir.isFileDirectory(filesList.get(i))) {
						this.deleteFolder(new Directory(dUnit, node));
						dir.removeFile(filesList.get(i));
					} else {
						DataFile file = new DataFile(dUnit, node);
						file.deleteFile();
						dir.removeFile(filesList.get(i));
					}
				}
				dir.deleteDir();
			} catch (FileNotFoundException e) {
				resultsList.add(e.getMessage());
			}

		}
		
	}
	
	/**
	 * Command to delete a specified data file from the disk.
	 * @author jeano
	 *
	 */
	private class RemoveFileProcessor implements CommandActionHandler {

		/**
		 * Executes the command by deleting the data file if its
		 * 	found in the current directory.
		 * @param c command with name of file to delete.
		 * @return if command was executed properly or not
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			
			try {
				INode fileNode = currDirectory.getFileNode(name);
				
				if(fileNode == null || fileNode.getType() == (byte) 1)
					resultsList.add("No such data file in directory.");
				else {
					DataFile file = new DataFile(dUnit, fileNode);
					file.deleteFile();
					currDirectory.removeFile(name);
				}
				
			} catch (IOException e) {
				resultsList.add(e.getMessage());
				return resultsList;
			}
			
			return resultsList; 
		} 
		
	}
	
	/**
	 * Command to find all instances of a data file in the disk.
	 * @author jeano
	 *
	 */
	private class FindFileProcessor implements CommandActionHandler {
		private String name;
		private DirectoryManager searchPath;
		
		/**
		 * Executes the command by finding all data files with the
		 * specified name, starting by the root folder.
		 * @param c command with name of file.
		 * @return if command was executed properly or not
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
			name = fc.getOperand(1);
			searchPath = new DirectoryManager(dUnit.getINode(0));
			Directory dir = new Directory(dUnit, dUnit.getINode(0));
			
			this.searchFolder(dir);	
			
			return resultsList; 
		} 
		
		/**
		 * Searches recursively for the file until all folders have been 
		 * 	visited.
		 * @param dir directory where file will be searched for.
		 */
		private void searchFolder(Directory dir) {
			ArrayList<String> files = dir.getFiles();
			
			for(int i = 0; i < files.size(); i++) {
				String file = files.get(i).trim();
				if(file.contains(name)) {
					resultsList.add(searchPath.getDirPath() + name);
				}
				try {
					if(dir.isFileDirectory(file)) {
						Directory dir2 = new Directory(dUnit, dir.getFileNode(file));
						searchPath.addDirToPath(file, dir.getFileNode(file));
						searchFolder(dir2);
					}
				} catch (FileNotFoundException e) {
					return;
				}
					
			}
			
			if(searchPath.getSize() > 1)
				searchPath.removeDirFromPath();
		}
		
	}
	
	/**
	 * Command to add specified data to the end of an existing file.
	 * @author jeano
	 *
	 */
	private class AppendFileProcessor implements CommandActionHandler {

		/**
		 * Executes the command by finding the existing file, reading
		 * 	the external file and appending the data.
		 * @param c command with name of external file and file to append to.
		 * @return if command was executed properly or not
		 */
		@Override
		public ArrayList<String> execute(Command c) {
			
			resultsList = new ArrayList<String>(); 
			if(dUnit == null) {
				resultsList.add("No disk is currently mounted.");
				return resultsList;
			}
			
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(2);
			String filename = fc.getOperand(1);
			
			try {
				String data = Utils.getStringFromFile(filename);
				INode fileNode = currDirectory.getFileNode(name);
				
				if(fileNode == null)
					new LoadFileProcessor().execute(c);
				else if(fileNode.getType() == (byte) 1)
					resultsList.add("File is a directory");
				else {
					DataFile file = new DataFile(dUnit, fileNode);
					file.appendFile(data);
				}
				
			} catch (IOException e) {
				resultsList.add(e.getMessage());
			}
			
			
			return resultsList; 
		} 
		
	}
	
	/**
	 * @return
	 */
	public boolean inShutdownMode() {
		return stopExecution;
	}
	
}		