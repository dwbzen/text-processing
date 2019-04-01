package org.dwbzen.text.pos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.Configuration;

/**
	The format of each legacy POS entry is
	<word><tab><POS tag(s)><unix newline>
	
	Where the POS tag is one or more of the following:
	
	N	Noun
	L   Proper noun (not tagged) - N and first letter upper case
	l   Improper noun (not tagged) - N, p and first letter lower case
	H	Place name (proper noun)
	M   Male person first name
	F   Female person first name
	S   Surname (last names)
	p	Plural Noun
	h	Noun Phrase
	V	Verb (usu participle)
	t	Verb (transitive)
	i	Verb (intransitive)
	A	Adjective
	v	Adverb
	C	Conjunction
	P	Preposition
	!	Interjection
	r	Pronoun
	D	Definite Article
	I	Indefinite Article
	o	Nominative
	c	Color
	
	Derived Forms
	-------------
	G   gerund (V or t + ing)
	Z   Adjectival verb (V or t + "er", as in taker, putter etc.)
	z   derived possessive noun (N or p + "er" )
	X	present tense verb (V, t)
	x	past tense verb (V, t)
	
	B	body part
	b	body part - male
	d	body part - female
	
	Unused in legacy: E, J, K, O, Q, R, T, U, W, Y, a, e, f, g, j, k, m, n, q, s, u, w, y

*/
public class PartsOfSpeechManager {

	public static final String CONFIG_FILENAME = "/config.properties";

	private final static String POS_DIR = "/reference/pos/";
	private final static String POS_FILE = "3eslpos.txt";
	private static String ALL_UPPER =  "^[A-Z]{2,}";	// at least 2 upper case chars
	private static String PROPER_WORD = "^[A-Z][a-z]+";	// Title case
	private static String ALL_NUMERIC = "^[0-9]{2,}";
	private static String COMPOUND_WORDS = "^\\w+\\s+.*";

    private static final Logger logger = LogManager.getLogger(PartsOfSpeech.class);
    
    private static String[] Zwords = {" in", " on", " over" };
    
    private String posDir = null;
    private String userDir = null;
    private String posFile = null;
    private String firstNamesFile = null;
    private String lastNamesFile = null;
    private String slangFile = null;
    private String extendedVocabFile = null;
    private String userFile = null;
	private int upperWordsSkipped = 0;
	private int properWordsSkipped = 0;
	private int numericWordsSkipped = 0;
	private int compoundWordsSkipped = 0;
	private int fileWords = 0;	// initialized for each POS file
	/**
	 * A Map<String, List<String>> of words keyed by part of speech
	 */
	private Map<String, List<String>> wordMap;
	
	/**
	 * A MapMap<String, String> of parts of speech keyed by words
	 */
	private Map<String, String> posMap = new HashMap<String, String>();
	
	private boolean ignoreUpperCaseWords = false;
	private boolean ignoreProperWords = false;
	private boolean ignoreAllNumbers=false;
	private boolean ignoreCompoundWords = false;
	
	private List<String> posFiles = new ArrayList<String>();
	private List<String> posFileNames = new ArrayList<String>();
	private BufferedReader posFileReader;
	private Properties configProperties = null;
	private Configuration configuration = null;

	/**
	 * Command line arguments:
	 * -include flag1,flag2...	TODO: types of words to include (load) that are normally ignored
	 * 							uc  = words in all UPPER CASE
	 * 							num = words entirely numeric chars
	 * 							pn  = proper nouns, phrases
	 * 							comp= compound words (phrases)
	 * -filter pos1,pos2..		TODO: parts of speech to ignore (see above list)
	 * -show pos1,pos2..		show words having these parts of speech
	 * 
	 * 
	 */
	public static void main(String[] args) {
		PartsOfSpeechManager partsOfSpeechManager = null;
		List<String> posToIgnore = new ArrayList<>();
		List<String> wordsToInclude = new ArrayList<>();
		String[] posToShow = {};
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-filter")) {
				String[] sa = args[++i].split(",");
				for(int ind=0;ind<sa.length;ind++) { posToIgnore.add(sa[ind]); }
			}
			if(args[i].equalsIgnoreCase("-include")) {
				String[] sa = args[++i].split(",");
				for(int ind=0;ind<sa.length;ind++) { wordsToInclude.add(sa[ind]); }
			}
			if(args[i].equalsIgnoreCase("-show")) {
				posToShow = args[++i].split(",");
			}
		}
		try {
			partsOfSpeechManager = PartsOfSpeechManager.newInstance();
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		System.out.println(partsOfSpeechManager.getStats());
		if(posToShow.length > 0) {
			Map<String, List<String>> map = partsOfSpeechManager.getWordsForPos(posToShow);
			map.keySet().forEach(k -> {
				System.out.println(PartsOfSpeech.partsOfSpeechLegacy.get(k));
				System.out.println(map.get(k));
			});
		}
	}
	
	/**
	 * Creates a new PartsOfSpeech that takes input from the configured parts of speech files
	 * Part Of Speech Database, July 23, 2000
	 * Compiled by Kevin Atkinson <kevina@users.sourceforge.net>
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public static PartsOfSpeechManager newInstance()throws IOException {
		PartsOfSpeechManager pos = new PartsOfSpeechManager();
		return pos;
	}
	
	protected PartsOfSpeechManager() throws IOException {
		initialize();	// set configuration and load wordMap
	}

	protected void initialize() throws IOException {
		configure();
		wordMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
		partsOfSpeech.keySet().forEach(s -> wordMap.put(s, new ArrayList<String>()));
		posFiles.forEach(f -> loadWords(f));
	}
	
	/**
	 * Load all the configured pos file names to use
	 */
	private void configure()  {
		configuration = Configuration.getInstance(CONFIG_FILENAME);
		configProperties = configuration.getProperties();
		// set file names & locations
		posDir = configProperties.getProperty("POS_DIR", POS_DIR);		// mandatory
		posFile = configProperties.getProperty("POS_FILE", POS_FILE);	// mandatory
		addPosFileName(posDir + posFile);
		/*
		 * The other parts-of-speech files are all optional
		 * There are no defaults
		 */
		if(configProperties.containsKey("EXTENDED_VOCAB_FILE")) {
			extendedVocabFile = configProperties.getProperty("EXTENDED_VOCAB_FILE");
			addPosFileName(posDir + extendedVocabFile);
		}
		if(configProperties.containsKey("FIRSTNAMES")) {
			firstNamesFile = configProperties.getProperty("FIRSTNAMES");
			addPosFileName(posDir + firstNamesFile);
		}
		if(configProperties.containsKey("LASTNAMES")) {
			lastNamesFile = configProperties.getProperty("LASTNAMES");
			addPosFileName(posDir + lastNamesFile);
		}
		if(configProperties.containsKey("SLANG_FILE")) {
			slangFile = configProperties.getProperty("SLANG_FILE");
			addPosFileName(posDir + slangFile);
		}
		userDir = configProperties.getProperty("USER_DIR", "C:/data/text/");
		if(configProperties.containsKey("USER_FILE")) {
			userFile = configProperties.getProperty("USER_FILE");
			addPosFileName(userDir + userFile);
		}
    }
    
	private static Map<String, String> partsOfSpeech = PartsOfSpeech.partsOfSpeechLegacy;
	private static Set<String> loadedPartsOfSpeech = PartsOfSpeech.partsOfSpeechLegacy.keySet();

	public static Set<String> getLoadedPartsOfSpeech() {
		return loadedPartsOfSpeech;
	}

	public static Set<String> getPartsOfSpeech() {
		return partsOfSpeech.keySet();
	}
	
	protected void loadWords(String aPosFile) {
		fileWords = 0;
		try {
			if(aPosFile == null) {
				readFileLines(new InputStreamReader(System.in));
			}
			else if(aPosFile.contains(":")) {	// like C:/data/text/
					readFileLines(new FileReader(aPosFile));
			}
			else {
				InputStream is = this.getClass().getResourceAsStream(aPosFile);
				if(is != null) {
					try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {
						stream.forEach(s -> analyzeAndSaveWord(s));
					}
				}
				else {
					logger.error("Unable to open " + aPosFile);
				}
			}
		}
		catch(FileNotFoundException e) {
			logger.error("File not found: " + aPosFile);
		}
		catch(IOException e) {
			logger.error("Unable to read: " + aPosFile);
		}
		
		logger.debug(fileWords + " words analyzed/saved " + aPosFile);
		if(properWordsSkipped > 0)
			logger.debug(properWordsSkipped + " proper words skipped");
		if(upperWordsSkipped > 0)
			logger.debug(upperWordsSkipped + " upper case words skipped");
		if(numericWordsSkipped > 0)
			logger.debug(numericWordsSkipped + " numeric words skipped");
		if(compoundWordsSkipped > 0)
			logger.debug(compoundWordsSkipped + " compound words skipped");
	}

	private int readFileLines(Reader reader)  throws IOException {
		fileWords = 0;
		posFileReader = new BufferedReader(reader);
		String line = null;
		while((line = posFileReader.readLine()) != null) {
			analyzeAndSaveWord(line);
		}
		posFileReader.close();
		return fileWords;
	}
	
	private void analyzeAndSaveWord(String line) {
		int tab = line.indexOf('\t');
		if(tab <= 0) return;
		String word = line.substring(0,tab);
		String pos = line.substring(tab+1);
		if(word.length() <= 1) return;
		if(ignoreUpperCaseWords &&  word.matches(ALL_UPPER)) {upperWordsSkipped++ ; return; }
		if(ignoreProperWords && word.matches(PROPER_WORD)) {properWordsSkipped++; return; }
		if(ignoreAllNumbers && word.matches(ALL_NUMERIC)) { numericWordsSkipped++; return; }
		if(ignoreCompoundWords && word.matches(COMPOUND_WORDS )) { 
			compoundWordsSkipped++;
			return; 
		}
		for(int i=0; i<pos.length();i++) {
			// a word can have multiple parts of speech
			String apos = String.valueOf(pos.charAt(i));
			boolean isproper = word.matches(PROPER_WORD);
			int nind = apos.indexOf('N');
			if(nind >= 0) {
				if(isproper) {
					saveWord(word, apos + "L");
				}
				else {
					saveWord(word, apos + "l");
				}
			}
			else {
				saveWord(word, apos);
			}
			fileWords++;
		}
		return;
	}

	private void saveWord(String word, String pos) {
		posMap.put(word, pos);
		for(char c : pos.toCharArray()) {
			switch(c) {
			case '|':
			case 'o':
				break;
			case 'N':
			case 'p':
			case 'h':
				addWord(word, c);
				addDerrived(word, 'z');
				break;
			case '!':
				addWord(word.concat("!"),c);
				break;
			case 'L':
			case 'l':
			case 'M':
			case 'H':
			case 'F':
			case 'S':
			case 'A':
			case 'v':
			case 'C':
			case 'P':
			case 'r':
			case 'D':
			case 'i':
			case 'I':
			case 'B':
			case 'b':
			case 'd':
			case 'c':
			case 'E':
			case 'J':
			case 'K':
			case 'Q':
			case 'R':
			case 'T':				
				addWord(word, c);
				break;
			case 'V':
			case 't':
				addWord(word, c);
				addDerrived(word, 'G');
				addDerrived(word, 'Z');
				addDerrived(word, 'X');
				addDerrived(word, 'x');
				break;
			default: /* invalid or non-legacy character */
				logger.debug("Warning Invalid character: '" + c + "' " + word + " ignored");
			}
		}
	}
	
	private void addDerrived(String word, char pos) {
		if(word.endsWith("ing") && pos == 'G') {
			wordMap.get(String.valueOf(pos)).add(word);
		}
		else {
			String nWord = null;
			int n = word.length()- 1;
			char c = word.charAt(n);
			if("AEIOUaeiou".indexOf(c) >= 0 && (pos == 'G' || pos == 'Z' || pos == 'x')) {
				nWord = word.substring(0, n);
			}
			else {
				nWord = word;
			}
			if(pos == 'G') {
				wordMap.get(String.valueOf(pos)).add(nWord + "ing");
			}
			else if(pos == 'Z') {
				addDerrivedZ(nWord);
				//wordMap.get(String.valueOf(pos)).add(nWord + "er");
			}
			else if(pos == 'z') {
				wordMap.get(String.valueOf(pos)).add(nWord + "er");
			}
			else if(pos == 'X') {
				if(c == 'e' || c == 'E')
					wordMap.get(String.valueOf(pos)).add(nWord + "s");
				else
					wordMap.get(String.valueOf(pos)).add(nWord + "s");
			}
			else if(pos == 'x') {
				wordMap.get(String.valueOf(pos)).add(nWord + "ed");
			}
		}
	}

	private void addWord(String word, char pos) {
		wordMap.get(String.valueOf(pos)).add(word);
	}
	
	
	/**
	 * Creates a count of words by part of speech
	 * @return String the stats formatted
	 */
	public String getStats() {
		StringBuffer sb = new StringBuffer();
		wordMap.keySet().forEach(key -> sb.append(partsOfSpeech.get(key) + " (" + key + "): " ).append(wordMap.get(key).size() + "\n"));
		return sb.toString();
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
	
	public void addPosFile(String posFileName) throws IOException {
		loadWords(addPosFileName(posFileName));
	}
	
	private String addPosFileName(String posFileName) {
		posFileNames.add(posFileName);
		posFiles.add(posFileName);
		return posFileName;
	}
	
	public boolean isIgnoreUpperCaseWords() {
		return ignoreUpperCaseWords;
	}

	public void setIgnoreUpperCaseWords(boolean ignoreUpperCaseWords) {
		this.ignoreUpperCaseWords = ignoreUpperCaseWords;
	}

	public boolean isIgnoreProperWords() {
		return ignoreProperWords;
	}

	public void setIgnoreProperWords(boolean ignoreProperWords) {
		this.ignoreProperWords = ignoreProperWords;
	}

	public boolean isIgnoreAllNumbers() {
		return ignoreAllNumbers;
	}

	public void setIgnoreAllNumbers(boolean ignoreAllNumbers) {
		this.ignoreAllNumbers = ignoreAllNumbers;
	}

	public boolean isIgnoreCompoundWords() {
		return ignoreCompoundWords;
	}

	public void setIgnoreCompoundWords(boolean ignoreCompoundWords) {
		this.ignoreCompoundWords = ignoreCompoundWords;
	}

	public List<String> getPosFileNames() {
		return posFileNames;
	}

	public Map<String, List<String>> getWordMap() {
		return wordMap;
	}
	
	public List<String> getWords(String pos) {
		return wordMap.get(pos);
	}

	public Map<String, String> getPosMap() {
		return posMap;
	}
	
	/*
	 * Eliminates the trailing "in", "on", "over"
	 */
	private void addDerrivedZ(String nWord) {
		for(String zword : Zwords) {
			int ind = nWord.indexOf(zword);
			if(ind > 0) {
				wordMap.get("Z").add(nWord.substring(0, ind) + "er");
				return;
			}
		}
		wordMap.get("Z").add(nWord + "er");
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

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
}
