package org.dwbzen.text.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Utility methods used by Builder, Runner and Dictionary classes
 * 
 * @author don_bacon
 *
 */
public class PosUtil {
	
	public final static String[] CONFIG_FILES = {"/config.properties"};
	static Properties configProperties = null;
	static Configuration configuration = null;
	static String resource = null;
	static String data = null;
	static {
		configuration = Configuration.getInstance(CONFIG_FILES);
		configProperties = configuration.getProperties();
		resource = configProperties.getProperty("RESOURCE", "build/resources/main/reference");
		data = configProperties.getProperty("DATA", "C:\\data");
	}
	
	public PosUtil() {
		
	}
	
	public static List<String> parseInstance(String patternInstance) {
		return parseInstance(patternInstance, false);
	}
	
	/**
	 * Converts a part of speech instance string to a List<String> where each element is an individual POS<br>
	 * For example, "N`op`A`sp` would be parsed to a 4-element List<String> ["N", "`op`, "A", "`sp`]<br>
	 * 
	 * @param patternInstance String
	 * @param suppressDelims boolean if true, the multi-char pos delimiter (`) and tag delimiter # are  not retained.
	 * @return List<String>
	 */
	public static List<String> parseInstance(String patternInstance, boolean suppressDelims) {
		List<String> tokens = new ArrayList<String>();
		int index = 0;
		int index2 = 0;
		while(index < patternInstance.length()) {
			char c = patternInstance.charAt(index);
			if(c == '`') {
				if(suppressDelims) {
					index2 = patternInstance.indexOf('`', index+1);
					tokens.add(
							index2 >= patternInstance.length() ?
								patternInstance.substring(index + 1) :
								patternInstance.substring(index + 1, index2));
					index = index2 + 1;
				}
				else {
					index2 = patternInstance.indexOf('`', index+1) + 1;
					tokens.add(
						index2 >= patternInstance.length() ?
							patternInstance.substring(index) :
							patternInstance.substring(index, index2));
					index = index2;
				}
			}
			else if(c == '#') {		// start of a tag. Format is #dd (d = 0, .. 9)
				index2 = index + 3;
				tokens.add(patternInstance.substring(index + (suppressDelims ? 1 :0), index2));
				index = index2;
			}
			else {
				if(c != '|') { // legacy dictionary reference we can ignore
					tokens.add(String.valueOf(c));
				}
				index++;
			}
		}
		return tokens;
	}
	
	public StringBuilder readPosFile(String filename) {
		StringBuilder sb = new StringBuilder();
		FileReader reader = null;
		if(filename.contains(":")) {	// absolute path, as in "C:/data/text/"
			try {
				reader = new FileReader(filename);		
				if(reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				System.err.println("Could not read " + filename);
				e.printStackTrace();
			}
		}
		else {	// relative to POS_DIR=/reference/pos/<aposfile>
			InputStream is = this.getClass().getResourceAsStream(filename);
			if(is != null) {
				try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {
					stream.forEach(s -> sb.append(s));
				}
			}
		}
		return sb;
	}
	public static StringBuilder readFile(String filename) {
		PosUtil util = new PosUtil();
		return util.readPosFile(filename);
	}
	
	public static Configuration getConfiguration() {
		return configuration;
	}
	
	
	/**
	 * Returns true if ANY of the compareString characters are in sourceString, false otherwise.</p>
	 * Comparisons are case-sensitive.
	 * For example containsAny("iNv", "LN") == true</p>
	 * containsAny("iNv", "V") == false.
	 * @param sourceString
	 * @param compareString
	 * @return
	 */
	public static boolean containsAny(String sourceString, String compareString) {
		boolean contains = false;
		contains |= compareString.chars().anyMatch(c -> sourceString.indexOf(c) >= 0);
		return contains;
	}
	
	public static String getInputFilename(String inputFile) {
		String inputFilename = inputFile.contains("$RESOURCE") ? inputFile.replace("$RESOURCE", resource) : inputFile;
		inputFilename = inputFile.contains("$DATA") ? inputFile.replace("$DATA", data) : inputFilename;
		return inputFilename;
	}

}
