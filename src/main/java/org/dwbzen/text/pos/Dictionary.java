package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.IJson;
import org.dwbzen.text.util.PosUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A collection of WordPos. 
 * 
 * @author don_bacon
 *
 */
public class Dictionary implements IJson {
    public static final String DEFAULT_NAME = "MyDictionary";
    public static final String POS_FILE_TYPE = "json";
    
	@JsonProperty("name")			private String name = "";
	@JsonProperty("sourceFiles")	private List<String> sourceFiles = new ArrayList<>();
	@JsonProperty("words")			private List<WordPos> words = new ArrayList<>();
	
	public Dictionary() { }
	
	public Dictionary(String dictionaryName) {
		name = dictionaryName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getSourceFiles() {
		return sourceFiles;
	}

	public void setSourceFiles(List<String> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}

	public void setWords(List<WordPos> words) {
		this.words = words;
	}

	public List<WordPos> getWords() {
		return words;
	}
	
	public void addWordPos(WordPos wp) {
		words.add(wp);
	}
	
	public int size() {
		return words.size();
	}
	
	/**
	 * Adds the contents of another Dictionary to this one. 
	 * @param aDictionary
	 */
	public void addDictionary(Dictionary aDictionary) {
		if(aDictionary != null && aDictionary.size() > 0) {
			sourceFiles.addAll(aDictionary.getSourceFiles());
			words.addAll(aDictionary.getWords());
		}
	}
	
	/**
	 * Creates a Dictionary instance from a JSON part of speech file.<br>
	 * posFile does not need an extension. If present it us used otherwise ".json" is added.
	 * @param posFile the JSON part of speech file base only (no extension).
	 * @param myName the Dictionary name. Can be blank but not null;
	 * @return Dictionary instance
	 * @throws RuntimeException if unable to deserialize the posFile
	 */
	public static Dictionary instance(List<String> posFiles, String myName) {
		Dictionary dictionary = null;
		for(String posFile : posFiles) {
			if(dictionary == null) {
				dictionary = instance(posFile);
			}
			else {
				dictionary.addDictionary(instance(posFile));
			}
		}
		dictionary.setName(myName != null ? myName : DEFAULT_NAME);
		return dictionary;
	}
	
	/**
	 * Creates a Dictionary instance from a JSON part of speech file.<br>
	 * posFile does not need an extension. If present it us used otherwise ".json" is added.
	 * @param posFile the JSON part of speech file base only (no extension).
	 * @return Dictionary instance
	 * @throws RuntimeException if unable to deserialize the posFile
	 */
	public static Dictionary instance(String posFile) {
		Dictionary dictionary = null;
		String posFileName = posFile.contains(".") ? posFile : posFile + "." + POS_FILE_TYPE;
		StringBuilder sb = PosUtil.readFile(posFileName);
		if(sb != null && sb.length() > 0) {
			try {
				dictionary = new ObjectMapper().readValue(sb.toString(), Dictionary.class);
			} catch (JsonProcessingException e ) {
				String message = "Could not deserialize " + posFile + " because " + e.getMessage();
				System.err.println(message);
				throw new RuntimeException(message, e);
			} 
		}
		return dictionary;
	}
	
}
