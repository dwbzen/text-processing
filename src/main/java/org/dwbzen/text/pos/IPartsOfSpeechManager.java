package org.dwbzen.text.pos;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dwbzen.text.util.Configuration;

public interface IPartsOfSpeechManager {
	static final String CONFIG_FILENAME = "/config.properties";
	static final String POS_DIR = "/reference/pos/";
	
	void configure();
	Configuration getConfiguration();
	Properties getConfigProperties();
	
	int loadWords(String posFileName);
	int analyzeAndSaveWord(String line);
	
	/**
	 * A Map<String, List<String>> of words keyed by part of speech
	 */
	Map<String, List<String>> getWordMap();
	
	/**
	 * A MapMap<String, String> of parts of speech keyed by words
	 */
	Map<String, String> getPosMap();
	
	default String getConfigurationFilename() {
		return CONFIG_FILENAME;
	}
	
	String getPosFileType();	// txt or json
	
	/**
	 * Creates a count of words by part of speech
	 * suppresses zero count parts of speech
	 * @return String the stats formatted
	 */
	 String getStats();
	 
	/**
	 * Gets a sorted map of all the Words having the parts of speech specified.
	 * @param poss String[]
	 * @return Map<String, List<String>> of part of speech, List of words
	 */
	 Map<String, List<String>> getWordsForPos(String[] poss);
	
	default String getPosFileExtension() {
		return "." + getPosFileType();
	}
}
