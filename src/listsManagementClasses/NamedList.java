package listsManagementClasses;

import java.util.ArrayList;

public class NamedList extends ArrayList<Integer> {

	private static final long serialVersionUID = 896807030271445097L;
	
	private String name; 
	public NamedList(String name) { 
		this.name = name; 
	}
	
	public String getName() {
		return name;
	}
	
}
