package org.dwbzen.text.util;

import java.util.Properties;

/**
 * Utility methods used by Builder, Runner classes
 * 
 * @author don_bacon
 *
 */
public final class Util {
	
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
