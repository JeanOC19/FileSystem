package diskUtilities;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Object to keep track of directory path inside the DiskUnit.
 * @author jeano
 *
 */
public class DirectoryManager {
	
	private ArrayList<Pair<String, INode>> path;
	private int size;
	
	/**
	 * Creates a new directory path with root as the current folder.
	 * @param node I-Node corresponding to the root folder.
	 */
	public DirectoryManager(INode node) {
		path = new ArrayList<Pair<String, INode>>();
		this.addDirToPath("root", node);
		size = 1;
	}
	
	/**
	 * Adds a folder to the end of the path.
	 * @param name name of the folder to be added.
	 * @param node I-Node of the folder to be added.
	 */
	public void addDirToPath(String name, INode node) 
			throws InvalidParameterException {
		if(name == null || node == null)
			throw new InvalidParameterException("Name or node can't be null.");
		
		Pair<String, INode> pair = new Pair<String, INode>(name, node);
		path.add(pair);
		size++;
		
	}
	
	/**
	 * @return name of the current folder in the path.
	 */
	public String getCurrentDirectoryName() {
		return path.get(size-1).getName();
	}
	
	/**
	 * @return I-Node of the current folder in the path.
	 */
	public INode getCurrentDirectoryNode() {
		return path.get(size-1).getNode();
	}
	
	/**
	 * Removes the last folder added to the path.
	 * @throws IllegalStateException whenever root folder wants to be removed.
	 */
	public void removeDirFromPath() {
		if(size == 1)
			throw new IllegalStateException("Cannot remove root folder.");
		path.remove(size -1);
		size--;
	}
	
	/**
	 * @return string representing the current path of the directory.
	 */
	public String getDirPath() {
		String result = "/";
		for(int i = 0; i < size; i++) {
			result += path.get(i).getName() + "/";
		}
		
		return result;
	}
	
	/**
	 * @return number of folders in the filepath.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Object to hold a pair of objects together.
	 * @author jeano
	 *
	 * @param <T1> first object type to be stored.
	 * @param <T2> second object type to be stored.
	 */
	private class Pair<T1,T2> {

		private T1 a;
		private T2 b;
		
		/**
		 * Creates a pair object with the specified objects.
		 * @param a first object of the corresponding pair.
		 * @param b second object of the corresponding pair.
		 */
		public Pair(T1 a, T2 b) {
			this.a = a;
			this.b = b;
		}
		
		/**
		 * @return the first object in the pair.
		 */
		public T1 getName() {
			return a;
		}

		/**
		 * @return the second object in the pair.
		 */
		public T2 getNode() {
			return b;
		}

	}

}
