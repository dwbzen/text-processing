package org.dwbzen.text.pos;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextGeneratorRunner {

	/**
	 * Generates text from pattern(s) using configured PartsOfSpeech files
	 * TODO: refactor the main into a new Runner class
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TextGenerator generator = null;
		int numberToGenerate = 20;
		String defaultPattern = "ANp";
		List<String> patternList = new ArrayList<String>();
		List<String>postProcessing = new ArrayList<String>();
		String patternLib = null;
		String delim = ",";	// for CSV output - field separator
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-n")) {
				numberToGenerate = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-pattern")) {
				patternList.add(args[++i]);				
			}
			else if(args[i].equalsIgnoreCase("-template")) {	// template library file
				patternLib = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-format")) {
				// format processing
				postProcessing.add(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-delim")) {
				delim = args[++i];
			}
			else {
				patternList.add(args[i]);
			}
		}

		generator = new TextGenerator();
		if(patternList.size() == 0 && patternLib == null) {
			patternList.add(defaultPattern);
		}
		if(patternLib != null) {
			File f = new File(patternLib);
			if(f.canRead()) {
				int ret = generator.setInputPatterns(f);
				if(ret != 0) {
					return;
				}
			}
			else {
				throw new RuntimeException("Cannot read template file:" + patternLib);
			}
		}
		else {
			generator.setPatternList(patternList);
		}

		if(postProcessing.size() > 0) {
			generator.setPostProcessing(postProcessing);
			generator.setDelimiter(delim);
		}
		else {
			generator.setPostProcessing("NC");	// no conversion
		}
		generator.apply(numberToGenerate);	// functional interface
		List<String> generatedText = generator.getGeneratedText();
		
		if(generatedText != null) {
			for(String text : generatedText) {
				if(text.length()>0) {
					System.out.println(text);
				}
			}
		}
	}

}
