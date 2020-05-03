package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.List;

public class DictionaryManager extends AbstractPartsOfSpeechManager {

	private Dictionary dictionary = null;

	/**
	 * Creates DictionaryManager with the configured POS_FILE
	 */
	protected DictionaryManager() {
		configure();
		dictionary = Dictionary.instance(posFile);
	}
	
	protected DictionaryManager(Dictionary dict) {
		dictionary = dict;
		configure();
	}
	
	public static DictionaryManager instance() {
		return new DictionaryManager();
	}
	
	public static DictionaryManager instance(String posFile) {
		Dictionary dict = Dictionary.instance(posFile);
		return new DictionaryManager(dict);
	}
	
	public static DictionaryManager instance(List<String> posFiles, String name) {
		Dictionary dict = null;
		DictionaryManager dictionaryManager = null;
		if(posFiles != null && posFiles.size()>0) {
			dict = Dictionary.instance(posFiles, name);
			dictionaryManager = new DictionaryManager(dict);
		}
		else {
			dictionaryManager = new DictionaryManager();
		}
		return dictionaryManager;
	}

	/**
	 * Load all the configured pos file names to use. Names don't include extensions, ".json" in this case.
	 */
	@Override
	public void configure()  {
		super.configure();
    }
	
	@Override
	public int loadWords(String posFileName) {
		if(dictionary == null) {
			dictionary = Dictionary.instance(posFileName);
		}
		else {
			dictionary.addDictionary(Dictionary.instance(posFileName));
		}
		for(WordPos wordPos : dictionary.getWords()) {
			wordPos.getPos().forEach(w -> saveWord(wordPos.getWord(),w));
		}
		return dictionary.getWords().size();
	}

	
	public Dictionary getDictionary() {
		return dictionary;
	}
	
	@Override
	public String getPosFileType() {
		return Dictionary.POS_FILE_TYPE;
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
