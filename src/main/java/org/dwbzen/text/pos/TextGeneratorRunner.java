package org.dwbzen.text.pos;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextGeneratorRunner {

	/**
	 * 
	 * This class generates text from pattern(s) using configured PartsOfSpeech files.
	 * 
	 * POS files loaded from POS_DIR folder by default: <i>/reference/pos/</i>. <br>
	 * This can be overridden by specifying a complete file path, for example "C:/data/text/filename.pos".<br>
	 * Default <tt>IPartsOfSpeechManager</tt> is <tt>DictionaryManager</tt> which uses JSON parts of speech files.<p>
	 * Use the <tt>-legacy</tt> flag to use the original (legacy) parts of speech files. This feature will be deprecated in a future release.
	 * <h2>Command Line Arguments</h2>
	 * <dl>
	 * <dt>-n <i>number</i></dt>
	 * <dd>number of instances to generate</dd>
	 * <dt>-pattern <i>pattern text</i></dt>
	 * <dd>generate using the inline part-of-speech pattern</dt>
	 * <dt>-template <i>filename</i></dt>
	 * <dd>use pattern(s) in specified file. if only the name is provided, "bin/main/reference/pos/" is added to the path. See examples in <tt>reference/pos</tt> folder</dd>
	 * <dt>-format <i>format code</i></dt>
	 * <dd>Output formatting. See <tt>TextGenerator</tt> for details</dd>
	 * <dt>-pos <i>file1[,file2,...filen]</i></dt>
	 * <dd>Parts-of-speech file(s). This overrides the the <tt>POS_FILE</tt> setting in config.properties.</dd>
	 * </dl>
	 * Use -legacy command line option to use legacy text POS files and PartsOfSpeechManager.<br>
	 * POS files are specified without the filename extension (.json, .txt). 
	 * 
	 * @author Don Bacon
	 * @param args
	 * @throws Exception
	 * @see org.dwbzen.text.pos.TextGenerator
	 */
	public static void main(String[] args) throws Exception {
		TextGenerator generator = null;
		int numberToGenerate = 20;
		String defaultPattern = "ANp";
		List<String> patternList = new ArrayList<String>();
		String[] postProcessing = null;
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
				postProcessing = args[++i].split(",");
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
			partsOfSpeechManager = LegacyPartsOfSpeechManager.newInstance(posFiles);
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

		if(postProcessing != null && postProcessing.length > 0) {
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
