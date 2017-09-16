package util.text;

import java.util.List;

public interface IPatternParser {

	/**
	 * 
	 * @param sentence a String of concatenated pattern words
	 */
	public void parse(String sentence);
	
	public List<String> getWords();
	
	public List<PatternWord> getPatternWords();
}
