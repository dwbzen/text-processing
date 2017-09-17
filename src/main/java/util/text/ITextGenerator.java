package util.text;

import java.util.List;

public interface ITextGenerator {

	public void generate(int numberToGenerate, String pattern);
	
	public void generate(int numberToGenerate);
	
	public List<String> getGeneratedText();
	
	public void clearGeneratedText();
}
