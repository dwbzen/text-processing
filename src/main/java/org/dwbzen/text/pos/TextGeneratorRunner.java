package org.dwbzen.text.pos;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dwbzen.text.util.PosUtil;

public class TextGeneratorRunner {

	/**
	 * Generates text from pattern(s) using configured PartsOfSpeech files<br>
	 * If specified, "-pos file1,[file2...] overrides config setting POS_FILE<br>
	 * To also include POS_FILE, add to the list<br>
	 * by default POS files loaded from POS_DIR folder: /reference/pos/<br>
	 * This can be overridden by specifying a complete file path, for example "C:/data/text/filename.pos"<br>
	 * Default IPartsOfSpeechManager is DictionaryManager which uses JSON parts of speech files.<br>
	 * Use -legacy command line option to use legacy text POS files and PartsOfSpeechManager.<br>
	 * POS files are specified without the filename extension (.json, .txt).<br>
	 * -template <filename> : if only the name is provided, "bin/main/reference/pos/" is added to the path.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TextGenerator generator = null;
		int numberToGenerate = 20;
		String defaultPattern = "ANp";
		List<String> patternList = new ArrayList<String>();
		List<String>postProcessing = new ArrayList<String>();
		String patternFileName = null;
		String delim = ",";	// for CSV output - field separator
		List<String> posFiles = Collections.emptyList();
		boolean useLegacy = false;
		IPartsOfSpeechManager partsOfSpeechManager = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-n")) {
				numberToGenerate = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-pattern")) {
				patternList.add(args[++i]);				
			}
			else if(args[i].equalsIgnoreCase("-template")) {	
				// template library file, for example: -template  "bandNamePatterns.txt"
				patternFileName = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-format")) {
				// format processing
				postProcessing.add(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-delim")) {
				delim = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-pos")) {	// alternative list of POS_FILEs
				posFiles = Arrays.asList(args[++i].split(","));
			}
			else if(args[i].equalsIgnoreCase("-legacy")) {
				useLegacy = true;
			}
			else {
				patternList.add(args[i]);
			}
		}

		if(useLegacy) {
			partsOfSpeechManager = PartsOfSpeechManager.newInstance(posFiles);
		}
		else {
			partsOfSpeechManager = DictionaryManager.instance(posFiles, "name");
		}
		generator = TextGenerator.newInstance(partsOfSpeechManager);
		if(patternList.size() == 0 && patternFileName == null) {
			patternList.add(defaultPattern);
		}
		if(patternFileName != null) {
			File f = new File(patternFileName.contains("/") ? patternFileName : "bin/main/reference/pos/" + patternFileName);
			if(f.canRead()) {
				int ret = generator.setInputPatterns(f);
				if(ret != 0) {
					return;
				}
			}
			else {
				throw new RuntimeException("Cannot read template file:" + patternFileName);
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
