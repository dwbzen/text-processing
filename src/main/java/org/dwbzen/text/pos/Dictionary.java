package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.IJson;
import org.dwbzen.text.util.PosUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A collection of WordPos. 
 * 
 * @author don_bacon
 *
 */
public class Dictionary implements IJson {

	@JsonProperty("name")			private String name = null;
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
	
	public static Dictionary instance(List<String> posFiles, String name) {
		Dictionary dictionary = null;
		String combinedName = null;
		for(String posFile : posFiles) {
			if(dictionary == null) {
				dictionary = instance(posFile);
				combinedName = dictionary.getName();
			}
			else {
				dictionary.addDictionary(instance(posFile));
			}
		}
		dictionary.setName(name != null ? name : combinedName);
		return dictionary;
	}
	
	/**
	 * Creates a Dictionary instance from a JSON part of speech file
	 * @param filename
	 * @return
	 */
	public static Dictionary instance(String posFile) {
		Dictionary dictionary = null;
		StringBuilder sb = PosUtil.readFile(posFile);
		if(sb != null && sb.length() > 0) {
			try {
				dictionary = new ObjectMapper().readValue(sb.toString(), Dictionary.class);
			} catch (JsonMappingException e ) {
				System.err.println("Could not deserialize " + posFile + " because " + e.getMessage());
			} catch (JsonProcessingException e) {
				System.err.println("Could not deserialize " + posFile + " because " + e.getMessage());
			}
		}
		return dictionary;
	}
	
	public static void main(String[] args) {
		String name = null;
		List<String> files = new ArrayList<>();
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-name")) {
				name = args[++i];
			}
			else { 
				files.add(args[i]);
			}
		}
		Dictionary dictionary = Dictionary.instance(files, name);
		System.out.println(dictionary.toJson(true));
	}
}
