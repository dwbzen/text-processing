package org.dwbzen.text.pos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.Configuration;

public abstract class PartsOfSpeechManager implements IPartsOfSpeechManager {

	protected static final Logger logger = LogManager.getLogger(TextGenerator.class);
	public static final String POS_FILE = "3eslpos";

    private static String[] Zwords = {" in", " on", " over" };

	protected static String ALL_UPPER =  "^[A-Z]{2,}";	// at least 2 upper case chars
	protected static String PROPER_WORD = "^[A-Z][a-z]+";	// Title case
	protected static String ALL_NUMERIC = "^[0-9]{2,}";
	protected static String COMPOUND_WORDS = "^\\w+\\s+.*";
	protected static String TAG_POS = "#[0-9]{2}";
	
	protected static Set<String> LoadedPartsOfSpeech = PartsOfSpeech.PartsOfSpeechMap.keySet();
	
	/**
	 * A Map<String, List<String>> of words keyed by part of speech
	 */
	protected Map<String, List<String>> wordMap = null;
	
	/**
	 * A MapMap<String, String> of parts of speech keyed by words
	 */
	protected Map<String, String> posMap = new HashMap<String, String>();
	
	protected List<String> posFiles = new ArrayList<String>();
	protected List<String> customPosFiles = new ArrayList<String>();
	protected Properties configProperties = null;
	protected Configuration configuration = null;
	private int fileWords = 0;	// initialized for each POS file
    protected String posDir = null;
    protected String posFile = null;
    private String firstNamesFile = null;
    private String lastNamesFile = null;
    private String slangFile = null;
    private String extendedVocabFile = null;
    private String userDir = null;
    private String userFile = null;

	
	public PartsOfSpeechManager() {
		wordMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
	}
	
	public IPartsOfSpeechManager getInstance(List<String> posFiles, String name) {
		return getInstance(posFiles, null);
	}
	
	public Map<String, List<String>> getWordMap() {
		return wordMap;
	}
	
	public Map<String, String> getPosMap() {
		return posMap;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public Properties getConfigProperties() {
		return configuration.getProperties();
	}
	
	public List<String> getWords(String pos) {
		return wordMap.get(pos);
	}
	public String getFirstNamesFile() {
		return firstNamesFile;
	}

	public void setFirstNamesFile(String firstNamesFile) {
		this.firstNamesFile = firstNamesFile;
	}

	public String getLastNamesFile() {
		return lastNamesFile;
	}

	public void setLastNamesFile(String lastNamesFile) {
		this.lastNamesFile = lastNamesFile;
	}

	public String getSlangFile() {
		return slangFile;
	}

	public void setSlangFile(String slangFile) {
		this.slangFile = slangFile;
	}

	public String getExtendedVocabFile() {
		return extendedVocabFile;
	}

	public void setExtendedVocabFile(String extendedVocabFile) {
		this.extendedVocabFile = extendedVocabFile;
	}

	public String getUserFile() {
		return userFile;
	}

	public void setUserFile(String userFile) {
		this.userFile = userFile;
	}
	
	public void addPosFile(String posFileName) throws IOException {
		posFiles.add(posFileName);
		loadWords(posFileName);
	}
	
	private String addPosFileName(String posFileName) {
		posFiles.add(posFileName);
		return posFileName;
	}
	
	public void addCustomPosFile(String posFileName) throws IOException {
		customPosFiles.add(posFileName);
	}

	public static Set<String> getLoadedPartsOfSpeech() {
		return LoadedPartsOfSpeech;
	}

	public static Set<String> getPartsOfSpeech() {
		return PartsOfSpeech.PartsOfSpeechMap.keySet();
	}
	
	public String getStats() {
		StringBuffer sb = new StringBuffer();
		Iterator<String> keyIt = wordMap.keySet().stream().sorted().iterator();
		while(keyIt.hasNext()) {
			String key = keyIt.next();
			int size = wordMap.get(key).size();
			if(size > 0) {
				sb.append(
					(PartsOfSpeech.PartsOfSpeechMap.containsKey(key) ? PartsOfSpeech.PartsOfSpeechMap.get(key) : "Tag" )
					+ " (" + key + "): " ).append(size + "\n");
			}
		} 
		return sb.toString();
	}
	
	public void configure() {
		configuration = Configuration.getInstance(CONFIG_FILENAME);
		configProperties = configuration.getProperties();
		boolean customPos = customPosFiles.size() > 0;
		// set file names & locations
		posDir = configProperties.getProperty("POS_DIR", POS_DIR);
		posFile = configProperties.getProperty("POS_FILE", POS_FILE);
		if(customPos) {
			customPosFiles.forEach(f -> addPosFileName(posDir + f));
		}
		else {
			addPosFileName(posDir + posFile);
		}
		
		/*
		 * The other parts-of-speech files are all optional
		 * There are no defaults
		 */
		if(!customPos && configProperties.containsKey("EXTENDED_VOCAB_FILE")) {
			extendedVocabFile = configProperties.getProperty("EXTENDED_VOCAB_FILE");
			addPosFileName(posDir + extendedVocabFile);
		}
		if(!customPos && configProperties.containsKey("FIRSTNAMES")) {
			firstNamesFile = configProperties.getProperty("FIRSTNAMES");
			addPosFileName(posDir + firstNamesFile );
		}
		if(!customPos && configProperties.containsKey("LASTNAMES")) {
			lastNamesFile = configProperties.getProperty("LASTNAMES");
			addPosFileName(posDir + lastNamesFile );
		}
		if(!customPos && configProperties.containsKey("SLANG_FILE")) {
			slangFile = configProperties.getProperty("SLANG_FILE");
			addPosFileName(posDir + slangFile );
		}
		userDir = configProperties.getProperty("USER_DIR", "C:/data/text/");
		if(!customPos && configProperties.containsKey("USER_FILE")) {
			userFile = configProperties.getProperty("USER_FILE");
			addPosFileName(userDir + userFile );
		}
		
		/*
		 * Initialize wordMap and load words from each POS file
		 */
		PartsOfSpeech.PartsOfSpeechMap.keySet().forEach(s -> wordMap.put(s, new ArrayList<String>()));
		posFiles.forEach(f -> loadWords(f));
	}
	
	/**
	 * Default implementation does nothing.
	 */
	public int analyzeAndSaveWord(String line) {
		return 0;
	}
	

	public Map<String, List<String>> getWordsForPos(String[] poss) {
		Map<String, List<String>> posWordMap = new TreeMap<>();
		for(String key : poss) {
			posWordMap.put(key, wordMap.get(key));
		}
		return posWordMap;
	}
	
	public String lookup(String word) {
		return posMap.get(word);
	}
	
	public List<String> getPosFiles() {
		return this.posFiles;
	}

	public List<String> getPosFileNames() {
		return posFiles;
	}
	
	public String getPosDir() {
		return posDir;
	}

	public void setPosDir(String posDir) {
		this.posDir = posDir;
	}

	public String getPosFile() {
		return posFile;
	}

	public void setPosFile(String posFile) {
		this.posFile = posFile;
	}

	protected int readFileLines(Reader reader)  throws IOException {
		fileWords = 0;
		BufferedReader posFileReader = new BufferedReader(reader);
		String line = null;
		while((line = posFileReader.readLine()) != null) {
			analyzeAndSaveWord(line);
		}
		posFileReader.close();
		return fileWords;
	}

	protected void saveWord(String word, String pos) {
		posMap.put(word, pos);
			switch(pos) {
			case "|":
			case "o":
				break;
			case "N":
			case "p":
			case "h":
				addWord(word, pos);
				addDerrived(word, "z");
				break;
			case "!":
				addWord(word.concat("!"), pos);
				break;
			case "L":
			case "l":
			case "M":
			case "H":
			case "F":
			case "S":
			case "A":
			case "v":
			case "C":
			case "P":
			case "r":
			case "D":
			case "i":
			case "I":
			case "B":
			case "b":
			case "d":
			case "c":
			case "w":
			case "E":
			case "J":
			case "K":
			case "Q":
			case "R":
			case "T":
			case "W":
			case "`op`":
			case "`sp`":
			case "`np`":
			case "`nF`":
			case "`nB`":
				addWord(word, pos);
				break;
			case "V":
			case "t":
				addWord(word, pos);
				addDerrived(word, "G");
				addDerrived(word, "Z");
				addDerrived(word, "X");
				addDerrived(word, "x");
				break;
			default: /* invalid, non-legacy character, or a tag (#nn) */
				if(pos.matches(TAG_POS)) {
					addWord(word, pos);
				}
				else {
					logger.warn("Warning Invalid part of speech: '" + pos + "' " + word + " ignored");
				}
			}
	}
	
	protected void addWord(String word, String pos) {
		if(wordMap.containsKey(pos)) {
			wordMap.get(pos).add(word);
		}
		else {	// add Tag
			wordMap.put(pos, new ArrayList<String>());
			wordMap.get(pos).add(word);
		}
	}
	
	protected void addDerrived(String word, String pos) {
		if(word.endsWith("ing") && pos == "G") {
			wordMap.get(String.valueOf(pos)).add(word);
		}
		else {
			String nWord = null;
			int n = word.length()- 1;
			char c = word.charAt(n);
			if("AEIOUaeiou".indexOf(c) >= 0 && (pos == "G" || pos == "Z" || pos == "x")) {
				nWord = word.substring(0, n);
			}
			else {
				nWord = word;
			}
			if(pos == "G") {
				wordMap.get(String.valueOf(pos)).add(nWord + "ing");
			}
			else if(pos == "Z") {
				addDerrivedZ(nWord);
				//wordMap.get(String.valueOf(pos)).add(nWord + "er");
			}
			else if(pos == "z") {
				wordMap.get(String.valueOf(pos)).add(nWord + "er");
			}
			else if(pos == "X") {
				if(c == 'e' || c == 'E')
					wordMap.get(String.valueOf(pos)).add(nWord + "s");
				else
					wordMap.get(String.valueOf(pos)).add(nWord + "s");
			}
			else if(pos == "x") {
				wordMap.get(String.valueOf(pos)).add(nWord + "ed");
			}
		}
	}

	/*
	 * Eliminates the trailing "in", "on", "over"
	 */
	protected void addDerrivedZ(String nWord) {
		for(String zword : Zwords) {
			int ind = nWord.indexOf(zword);
			if(ind > 0) {
				wordMap.get("Z").add(nWord.substring(0, ind) + "er");
				return;
			}
		}
		wordMap.get("Z").add(nWord + "er");
	}

	/**
	 * Displays parts of speech statistics for the specified POS file.<br>
	 * This assumes a JSON POS file unless -legacy is specified.<br>
	 * Command line arguments<br>
	 * -show pos1,pos2..		show words having these parts of speech<br>
	 * 
	 * TODO: implement -filter
	 * 
	 */
	public static void main(String[] args) {
		IPartsOfSpeechManager partsOfSpeechManager = null;
		List<String> posToIgnore = new ArrayList<>();
		List<String> customFiles = new ArrayList<>();
		String[] posToShow = {};
		boolean useLegacy = false;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-filter")) {
				String[] sa = args[++i].split(",");
				for(int ind=0;ind<sa.length;ind++) { posToIgnore.add(sa[ind]); }
			}
			else if(args[i].equalsIgnoreCase("-show")) {
				posToShow = args[++i].split(",");
			}
			else if(args[i].equalsIgnoreCase("-legacy")) {
				useLegacy = true;
			}
			else {
				customFiles.add(args[i]);
			}
		}
		try {
			if(customFiles.size() == 0) {
				partsOfSpeechManager = useLegacy ? LegacyPartsOfSpeechManager.newInstance() : DictionaryManager.instance();
			}
			else {
				partsOfSpeechManager =  useLegacy ? LegacyPartsOfSpeechManager.newInstance(customFiles) : DictionaryManager.instance(customFiles, "custom");
			}
			
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		System.out.println(partsOfSpeechManager.getStats());
		if(posToShow.length > 0) {
			Map<String, List<String>> map = partsOfSpeechManager.getWordsForPos(posToShow);
			map.keySet().forEach(k -> {
				System.out.println(PartsOfSpeech.PartsOfSpeechMap.get(k));
				System.out.println(map.get(k));
			});
		}
	}
}
