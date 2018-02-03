package operandHandlers;

import systemGeneralClasses.OperandAnalyzer;


public class OperandValidatorUtils {
	
	public static boolean isValidName(String operand) { 
		if (operand.length() == 0) 
			return false; 
		
		// operand is not empty string 
		boolean isName = (Character.isLetter(operand.charAt(0)));
		int cp=1; 
		while (cp < operand.length() && isName) { 
			char c = operand.charAt(cp); 
			if (!(Character.isDigit(c) || Character.isLetter(c)) || c == '.')
				isName = false; 
			cp++; 
		}		
		return isName;

	}

	public static boolean isValidInt(String operand) { 
		try { 
			Integer.parseInt(operand); 
			return true; 
		} 
		catch(Exception e) { 
			return false; 
		}		
	}
	
	public static boolean isValidDir(String operand) {
		return (isValidName(operand) ||  
				(operand.charAt(0) == '.' && operand.charAt(1) == '.'));
	}
	
	public static OperandAnalyzer getAnalyzerFor(String op) {
		if (op.equals("int") || op.equals("bsize") || op.equals("nblocks"))
			return IntOperandAnalyzer.getInstance(); 
		else if (op.equals("name") || op.equals("file") )
			return NameOperandAnalyzer.getInstance(); 
		else if (op.equals("dir"))
			return DirOperandAnalyzer.getInstance();
		return null;   // if nothing matches
	}



}
