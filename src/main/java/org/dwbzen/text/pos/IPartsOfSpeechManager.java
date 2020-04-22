package org.dwbzen.text.pos;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dwbzen.text.util.Configuration;

public interface IPartsOfSpeechManager {
	static final String CONFIG_FILENAME = "/config.properties";
	static final String POS_DIR = "/reference/pos/";
	
	IPartsOfSpeechManager getInstance();
	IPartsOfSpeechManager getInstance(List<String> posFiles);
	IPartsOfSpeechManager getInstance(List<String> posFiles, String name);
	
	void configure();
	Configuration getConfiguration();
	Properties getConfigProperties();
	
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
	
	default String getPosFileExtension() {
		return "." + getPosFileType();
	}
}
