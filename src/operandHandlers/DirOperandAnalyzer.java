package operandHandlers;

import java.util.ArrayList;

import systemGeneralClasses.OperandAnalyzer;
import systemGeneralClasses.StringCharactersExtractor;

/**
 * Class to validate an operand that corresponds to a folder name of the "cd" command.
 * @author jeano
 *
 */
public class DirOperandAnalyzer implements OperandAnalyzer{
	
	private static final DirOperandAnalyzer DIROPANALIZER = new DirOperandAnalyzer(); 
	
	private StringCharactersExtractor sce; 
	private boolean isValidOperand; 
	private String operand; 
	
	/**
	 * Private constructor.
	 */
	private DirOperandAnalyzer() {
		
	}
	
	/**
	 * Method that returns the class object.
	 * @return DirOperandAnalyzer object to be used.
	 */
	public static DirOperandAnalyzer getInstance() { 
		return DIROPANALIZER; 
	}
	
	/**
	 * Retrieves the operand from the input string.
	 */
	public ArrayList<String> disectOperandFromInput(String is, int cp) {
		sce = new StringCharactersExtractor(is, cp); 
		isValidOperand = true; 

		if (!sce.hasMoreContent())
			isValidOperand = false; 
		else {
			operand = sce.extractStringUpToWhiteSpaceChar(); 
		}
		
		if (isValidOperand) 
			isValidOperand = OperandValidatorUtils.isValidDir(operand); 
		
		if (isValidOperand) { 
			ArrayList<String> opName = new ArrayList<String>(); 
			opName.add(operand); 
			return opName; 
		}
		else
			return null; 
	}

	/**
	 * Returns the index of the character that is being currently analyzed.
	 */
	@Override
	public int currentIndexInInput() {
		return sce.currentIndexValue();
	}

}