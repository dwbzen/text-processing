package org.dwbzen.text.pos;

import java.util.List;

public interface IPatternParser {

	/**
	 * 
	 * @param sentence a String of concatenated pattern words
	 */
	public boolean parse(String sentence);
	
	public List<String> getWords();
	
	public List<PatternWord> getPatternWords();
	
	boolean isValid();
	
	String getError();
	
}
