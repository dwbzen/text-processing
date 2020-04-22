package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.Configuration;
import org.dwbzen.text.util.PosUtil;

public class DictionaryManager extends AbstractPartsOfSpeechManager {

    private static final Logger logger = LogManager.getLogger(Dictionary.class);
    public static final String DEFAULT_NAME = "dictionary";
    
	private Dictionary dictionary = null;

	protected DictionaryManager() { 
		configuration = PosUtil.getConfiguration();
		configProperties = configuration.getProperties();
	}
	
	protected DictionaryManager(Dictionary dict) {
		this();
		dictionary = dict;
	}
	
	static DictionaryManager instance(String posFile) {
		Dictionary dict = Dictionary.instance(posFile);
		return new DictionaryManager(dict);
	}
	
	static DictionaryManager instance(List<String> posFiles, String name) {
		Dictionary dict = Dictionary.instance(posFiles, name);
		return new DictionaryManager(dict);
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

	@Override
	public IPartsOfSpeechManager getInstance() {
		return new DictionaryManager();
	}

	@Override
	public IPartsOfSpeechManager getInstance(List<String> posFiles) {
		return DictionaryManager.instance(posFiles, DEFAULT_NAME);
	}

	@Override
	public void configure() {
		// TODO Auto-generated method stub
		
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}
	
	@Override
	public String getPosFileType() {
		return "json";
	}
	
}
