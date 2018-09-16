package org.dwbzen.text.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilities that process/reformat the word lists in reference/12Dicts and reference/pos
 * @author don_bacon
 *
 */
public class WordListUtils {
	protected static final Logger log = LogManager.getLogger(WordListUtils.class);
	private String inputFileName;
	private String outputFileName = null;
	private File inputFile = null;
	private File outputFile = null;
	private Map<String, String> variantMap = new TreeMap<String, String>();
	
	public WordListUtils(String input, String output) {
		inputFileName = input;
		outputFileName = output;
		inputFile = new File(input);
		if(outputFileName != null) {
			outputFile = new File(output);
		}
	}

	/**
	 * This reads through the "2+2lemma.txt" word list and creates
	 * a word map with the variant as the key and the primary word the value.<br>
	 * Variant includes inflections, variant spellings, and words formed with certain suffixes.
	 * </p>For example:</p><pre>
	 * funny
     *     funnier, funnies, funniest, funnily, funniness
     * </pre>
     * results in a 5-element map.<p>
     * Output include 2 files - a text file in the format key,value<br>
     * and a JSON file that can be deserialized to a TBD class. for example: <pre>
     * "vocabulary": [
     *   {
     *     "funny": {
     *   	  "numberOfVariants": 5,
     *   	  "variants": ["funnier", "funnies", "funniest", "funnily", "funnines" ]
     *     },
     *   }
     * ],
     * "variants": [
     *   { "funnier": "funny" },
     *   { "funnies": "funny" },
     *   ...
     * ]
     * </pre>
     * References (deisgnated by word -> word) in the source file variants list are ignored.<br>
     * A reference in the key lines, archer -> [arch] or are -> be for example,
     * are simply treated as a bidirectional variant.<br> 
     * So "arch" and "archer" are variants of each other.
	 * 
	 */
	public void createWordVariantMap() throws IOException {
		
		try {
			FileReader reader = new FileReader(inputFile);
			BufferedWriter writer = null;
			if(outputFile != null) {
				writer = new BufferedWriter(new FileWriter(outputFile));
			}
			BufferedReader inputFileReader = new BufferedReader(reader);
			String line;
			String key = null;
			while((line = inputFileReader.readLine()) != null) {
				if(line.length() > 0) { 
					boolean isVariantsLine = line.startsWith("    ") || line.startsWith("\t");
					if(isVariantsLine) {	// use the previous key
						String[] variants = line.split(",");
						for(String s : variants) {
							if(!s.contains("->")) {
								addToMap(s.trim(), key, writer);
							}
						}
					}
					else {	// it's a key line. Could contain a reference abstracted -> [abstract] for example
						String[] refs = line.split("->");
						String tempKey = refs[0].trim();
						key = tempKey.endsWith("+") ? tempKey.substring(0, tempKey.indexOf('+')) : tempKey;
						if(refs.length > 1) {
							String value = refs[1].substring(2, refs[1].indexOf(']'));
							addToMap(key, value, writer);
							addToMap(value, key, writer);
						}
					}
				}
			}
			inputFileReader.close();
		}
		catch(Exception e) {
			throw new IOException(inputFileName + " does not exist or is unreadable");
		}
	}
	
	private void addToMap(String key, String value, BufferedWriter writer) throws IOException {
		variantMap.put(key, value);
		String text = key + "," + value;
		if(writer != null) {
			writer.write(text);
			writer.newLine();
		}
		log.debug(text);
	}
	
	public String getInputFileName() {
		return inputFileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public File getInputFile() {
		return inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public Map<String, String> getVariantMap() {
		return variantMap;
	}

	/**
	 * Syntax: WordListUtils <inputFfileName> [-file <name> ]
	 * If no output file is given, results written to stdout.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String...args) throws IOException {
		String inputFileName = null;
		String outputFileName = null;	// name only - no extension
		boolean displayMap = false;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				outputFileName = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-display")) {
				displayMap = true;
			}
			else {
				inputFileName = args[i];
			}
		}
		WordListUtils wordListUtils = new WordListUtils(inputFileName, outputFileName);
		wordListUtils.createWordVariantMap();
		Map<String, String> variantMap = wordListUtils.getVariantMap();
		if(displayMap) {
			System.out.println(variantMap);
		}
		
	}
}
