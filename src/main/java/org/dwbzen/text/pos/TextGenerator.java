package org.dwbzen.text.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.ITextGenerator;

/**
 * Generates random text (words) from POS (part-of-speach) patterns.
 * Usage: TextGenerator [options] [pattern1 pattern2...]
 * Where: 
 * 	pattern1, pattern2...  :  POS pattern to use for text generation. 
 * Options:<br>
 * 	-pattern <text> : inline pattern to use<br>
 * 	-lib <filename>	: path to text file containing patterns to select from<br>
 *  -n <number>		: number of strings to generate<br>
 *  -format			: output format(s) processing:<br>
 *  					UC = convert to UPPER CASE, <br>
 *						LC = lower case,<br>
 *						TC = Title Case,<br>
 *						PW = PasswordFormat (title case w/white space removed)
 *						SC = Convert to sentence case.<br>
 *						NC = no conversion (default)<br>
 *						CSV = comma-separated words<br>
 *						TAB = tab-separated words<br>
 *						JSON:fieldname = as JSON array<br>
 * config.properties specifies the names and locations of POS files.</p>
 * Sample patterns:
 *  	[M|F|N](and)(the)A?p  - band name
 *  	!(where did you get that)A{1,3}[b|B](?) - puerile insults
 *  	hiv+
 *  	!(!)[(Will)|(Is)|(How)|(What)]ANrvtp(?) -  poetry
 *		ANp - simple example
 *
 * @author don_bacon
 * @see org.dwbzen.text.pos.PartsOfSpeach for valid parts of speach
 * @see org.dwbzen.text.pos.PartOfSpeachPattern for valid patterns.
 *
 */
public class TextGenerator implements ITextGenerator {
	protected static final Logger log = LogManager.getLogger(TextGenerator.class);
	public final static String QUOTE = "\"";
	public final static String SPACE = " ";
	
	private PartsOfSpeechRunner partsOfSpeechRunner = null;
	private Map<String, List<String>> wordMap = null;
	private Map<String, Integer> posCount = new HashMap<String, Integer>();
	private List<PartOfSpeechPattern> partOfSpeechPatterns = new ArrayList<PartOfSpeechPattern>();
	private List<String> patternList = new ArrayList<String>();	// raw patterns
	private List<String>generatedText = new ArrayList<String>();
	private List<String>postProcessing = new ArrayList<String>();
	private boolean upperCase = false;
	private boolean sentenceCase = false;
	private boolean titleCase = false;
	private boolean lowerCase = false;
	private boolean addDelimiter = true;
	private boolean removeWhiteSpace = false;
	private boolean jsonOutput = false;
	private boolean csvOutput = false;
	private boolean tabSeparatedOutput = false;
	private String jsonFieldname = null;
	private String delimiter = null;
	private ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * Generates text from pattern(s) using configured PartsOfSpeech files
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TextGenerator generator = null;
		int numberToGenerate = 20;
		String defaultPattern = "ANp";
		List<String> patternList = new ArrayList<String>();
		List<String>postProcessing = new ArrayList<String>();
		String patternLib = null;
		String delim = ",";	// for CSV output - field separator
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-n")) {
				numberToGenerate = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-pattern")) {
				patternList.add(args[++i]);				
			}
			else if(args[i].equalsIgnoreCase("-lib")) {	// pattern library
				patternLib = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-format")) {
				// format processing
				postProcessing.add(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-delim")) {
				delim = args[++i];
			}
			else {
				patternList.add(args[i]);
			}
		}

		generator = TextGenerator.newInstance();
		if(patternList.size() == 0 && patternLib == null) {
			patternList.add(defaultPattern);
		}
		if(patternLib != null) {
			File f = new File(patternLib);
			if(f.canRead()) {
				int ret = generator.setInputPatterns(f);
				if(ret != 0) {
					return;
				}
			}
			else {
				throw new RuntimeException("Cannot read pattern library:" + patternLib);
			}
		}
		else {
			generator.setPatternList(patternList);
		}

		if(postProcessing.size() > 0) {
			generator.setPostProcessing(postProcessing);
			generator.setDelimiter(delim);
		}
		else {
			generator.setPostProcessing("NC");	// no conversion
		}
		generator.generate(numberToGenerate);
		List<String> generatedText = generator.getGeneratedText();
		
		if(generatedText != null) {
			for(Iterator<String> it=generatedText.iterator(); it.hasNext();) {
				System.out.println(it.next());
			}
		}
	}
	
	public static TextGenerator newInstance() {
		PartsOfSpeechRunner pos = null;
		try {
				pos = PartsOfSpeechRunner.newInstance();
		}
		catch(IOException ex)  {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		return new TextGenerator(pos);
	}
	
	public TextGenerator() {}
	
	public TextGenerator(PartsOfSpeechRunner pos) {
		partsOfSpeechRunner = pos;
		wordMap = partsOfSpeechRunner.getWordMap();
		Set<String> loadedPOS = PartsOfSpeechRunner.getLoadedPartsOfSpeech();
		for(Iterator<String> it = loadedPOS.iterator(); it.hasNext(); ) {
			String ps = it.next();
			posCount.put(ps, wordMap.get(ps).size());
		}
	}
		
	/**
	 * Sample patterns: "AANp" "ANp" "NvV" "Np" "Ap" "vVp" "!AN" "ALp"
	 * Choice/range patterns: "[F|M]vtPAp" "N{2,5}" "[F|M]C[F|M]vtPAp"
	 * 
	 * @param numberToGenerate
	 * @param pattern
	 */
	public void generate(int numberToGenerate, String pattern) {
		this.addPattern(pattern);
		generate(numberToGenerate);
	}
	
	/**
	 * Picks pattern randomly from an array of patterns
	 * @param numberToGenerate
	 */
	public void generate(int numberToGenerate) {
		int len = partOfSpeechPatterns.size();
		String text = null;
		for(int i=0; i< numberToGenerate; i++) {
			int patInd = random.nextInt(len);
			PartOfSpeechPattern posPattern = partOfSpeechPatterns.get(patInd);
			if(!posPattern.isValid()) {
				text = posPattern.getError();
			}
			else {
				text = generateText(posPattern);
			}
			generatedText.add(text);
			if(text.startsWith("ERROR")) {
				break;
			}
		}
		return;
	}
	
	/**
	 * Picks pattern randomly from an array of patterns
	 * and generates a single instance of text
	 * @return String generated text
	 */
	public String generateText() {
		int patInd = random.nextInt(partOfSpeechPatterns.size());
		return generateText(partOfSpeechPatterns.get(patInd));
	}
	
	public List<String> generateText(int numberToGenerate) {
		generate(numberToGenerate);
		return generatedText;
	}
	
	/**
	 * Generates a text String from a given PartOfSpeechPattern
	 * @return a String that starts with "ERROR" if any errors encountered.
	 */
	public String generateText(PartOfSpeechPattern posPattern) {
		StringBuffer sb = new StringBuffer();
		String pattern = posPattern.createInstance();
		int nwords = pattern.length();
		List<String> generatedList = new ArrayList<String>();
		Map<String, String> variables = new HashMap<String, String>();
		boolean isText = false;
		boolean setVariable = false;
		boolean useVariable = false;
		String variable = null;
		for(int j=0; j<nwords; j++) {
			String c = pattern.substring(j, j+1);
			if(c.equals(PatternWord.TEXT_DELIMETER)) {
				if(isText) {
					if(setVariable) {
						variable = sb.toString();
					}
					else if(useVariable) {
						generatedList.add(variables.get(sb.toString()));
						useVariable = false;
					}
					else
						generatedList.add(sb.toString());
				}
				else {
					sb = new StringBuffer();
				}
				isText = !isText;
				continue;
			}
			else if(c.equals("$")) {	// use a variable
				useVariable = true;
				continue;
			}
			else if(c.equals("=")) {	// set a variable
				setVariable = true;
				continue;
			}
			if(isText) {
				sb.append(c);
			}
			else {
				if(posCount.containsKey(c)) {
					int n = posCount.get(c);
					if(n > 0) {
						/*
						 * word can actually be 2 words, for example "human rights"
						 * 
						 */
						String word = wordMap.get(c).get(random.nextInt(n));
						int ind = word.indexOf(' ');
						if(ind > 0 && removeWhiteSpace) {
							word = word.substring(0, ind) 
									+  String.valueOf(word.charAt(ind+1)).toUpperCase()
									+ word.substring(ind+2);
						}
						if(setVariable) {
							variables.put(variable, word);
							setVariable = false;
						}
						generatedList.add(word);
					}
				}
				else {	// invalid POS key used
					return "ERROR: Invalid part of speech: " + c;
				}
			}
		}
		return format(generatedList);
	}
	
	public static final String PUNCUTATION = ".?!;:,\"";
	/**
	 * Formats a generated pattern instance for punctuation & post processing
	 * @param generatedList a List<String> that is a generated pattern instance
	 * @return formatted string
	 */
	public String format(List<String> generatedList) {
		StringBuilder sb = new StringBuilder();
		if(jsonOutput) {
			formatJSON(sb, generatedList);
		}
		else {
			String outputDelim = csvOutput ? "," : (tabSeparatedOutput? "\t" : (addDelimiter ? SPACE : ""));
			int len = generatedList.size();
			boolean startOfSentence = true;
			
			for(int i=0; i<len; i++){
				String s = generatedList.get(i);
				log.debug("'" + s + "'");
				int punctIndex = PUNCUTATION.indexOf(s);	// >=0 if the string is a single PUNCTUATION character
				int blen = sb.length();
				if(punctIndex >= 0) {
					sb.replace(blen-1, blen, s);
				}
				else {
					if(upperCase) {
						sb.append( s.toUpperCase());
					}
					else if(lowerCase) {
						sb.append(s.toLowerCase());
					}
					else if(titleCase) {
						sb.append(s.substring(0, 1).toUpperCase());
						sb.append(s.substring(1));
					}
					else if(sentenceCase) {
						if(startOfSentence) {
							sb.append(s.substring(0, 1).toUpperCase());
							sb.append(s.substring(1));
							startOfSentence = false;
						}
						else {
							sb.append(s);
						}
					}
					else { // no conversion
						sb.append(s);
					}
				}
				startOfSentence = (s.endsWith(".") || s.endsWith("?") || s.endsWith("!"));
				if(i <len-1) {
					sb.append(outputDelim);
				}
			}
		}
		return sb.toString();
	}

	public void formatJSON(StringBuilder sb, List<String> generatedList) {
		// example:    "keywords" : ["cnn", "politics", "united states"]
		int i=0;
		sb.append(QUOTE + jsonFieldname + QUOTE);
		if(delimiter.equals(",")) {
			// assume an array
			sb.append(": [");
			for(String s : generatedList) {
				if(lowerCase) {
					sb.append(QUOTE + s.toLowerCase() + QUOTE);
				}
				else if(upperCase) {
					sb.append(QUOTE + s.toUpperCase() + QUOTE);
				}
				else if(titleCase) {
					sb.append(QUOTE + s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + QUOTE);
				}
				else {
					sb.append(QUOTE + s + QUOTE);
				}
				if(i++ < generatedList.size()-1) {
					sb.append(delimiter);
				}
			}
			sb.append("]");
		}
		else {
			sb.append(":" + QUOTE);
			for(String s : generatedList) {
				if(lowerCase) {
					sb.append(s.toLowerCase());
				}
				else if(upperCase) {
					sb.append(s.toUpperCase());
				}
				else if(titleCase) {
					sb.append(s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() );
				}
				else {
					sb.append(s);
				}
				if(i++ < generatedList.size()-1) {
					sb.append(delimiter);
				}
			}
			sb.append(QUOTE);
		}

	}

	public PartsOfSpeechRunner getPartsOfSpeech() {
		return partsOfSpeechRunner;
	}

	public void setPartsOfSpeech(PartsOfSpeechRunner partsOfSpeech) {
		this.partsOfSpeechRunner = partsOfSpeech;
	}

	public List<String> getPatternList() {
		return patternList;
	}

	public void setPatternList(List<String> patterns) {
		for(String pattern : patterns) {
			addPattern(pattern);
		}
	}
	
	public void setPatternList(String[] patterns) {
		for(int i=0; i<patterns.length; i++) {
			addPattern(patterns[i]);
		}
	}
	
	public void addPattern(String pattern) {
		patternList.add(pattern);
		partOfSpeechPatterns.add(new PartOfSpeechPattern(pattern));
	}
	
	public List<String> getGeneratedText() {
		return generatedText;
	}
	
	public void clearGeneratedText() {
		generatedText.clear();
	}

	public void addGeneratedText(List<String> text) {
		generatedText.addAll(text);
	}
	
	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public List<String> getPostProcessing() {
		return postProcessing;
	}

	public void setPostProcessing(List<String> ppList) {
		postProcessing.addAll(ppList);
		setPostProcessingFlags();
	}

	public void setPostProcessing(String[] pp) {
		for(int i=0; i<pp.length; i++) {
			postProcessing.add(pp[i]);
		}
		setPostProcessingFlags();
	}
	
	public void setPostProcessing(String pp) {
		postProcessing.add(pp);
		setPostProcessingFlags();
	}
	
	private void setPostProcessingFlags() {
		for(String pp : postProcessing) {
			if(pp.equalsIgnoreCase("UC")) {
				upperCase = true;
			}
			else if(pp.equalsIgnoreCase("TC")) {
				titleCase = true;
			}
			else if(pp.equalsIgnoreCase("PW")) {
				titleCase = true;
				addDelimiter = false;
				removeWhiteSpace = true;
			}
			else if(pp.equalsIgnoreCase("SC")) {
				sentenceCase = true;
			}
			else if(pp.equalsIgnoreCase("LC")) {
				lowerCase = true;
			}
			else if(pp.equalsIgnoreCase("CSV")) {
				csvOutput = true;
			}
			else if(pp.equalsIgnoreCase("TAB")) {
				tabSeparatedOutput = true;
			}
			else if(pp.startsWith("JSON")) {
				jsonOutput = true;
				jsonFieldname = pp.substring(pp.indexOf(":") + 1);
			}
		}
	}
	
	public int setInputPatterns(File f) {
		int ret = 0;
		List<String> patterns = new ArrayList<String>();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(f));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while((line = fileReader.readLine()) != null) {
				if(line.startsWith("//"))
					continue;
				if(line.endsWith("+")) {
					sb.append(line.substring(0, line.length() - 1));
				}
				else if(sb.length() > 0 ) {
					sb.append(line);
					patterns.add(sb.toString());
					sb = new StringBuffer();
				}
				else
					patterns.add(line);
			}
			fileReader.close();
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
			ret = -1;
		}
		setPatternList(patterns);
		return ret;
	}
}
