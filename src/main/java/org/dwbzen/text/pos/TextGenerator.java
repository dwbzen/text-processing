package org.dwbzen.text.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.services.TextDao;
import org.dwbzen.text.util.DataSourceType;
import org.dwbzen.text.util.FunctionManager;
import org.dwbzen.text.util.ITextGenerator;
import org.dwbzen.text.util.PosUtil;;

/**
 * Generates random text (words) from POS (part-of-speach) patterns.</br>
 * Usage: TextGenerator [options] [pattern1 pattern2...]</br>
 * Where: </br>
 * 	pattern1, pattern2...  :  POS pattern to use for text generation. </br>
 * Options:<br>
 * 	-pattern <text> : inline pattern to use<br>
 * 	-template <filename>   : path to text file containing patterns to select from<br>
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
 * Sample patterns:</br><code>
 *  	[M|F|N](and)(the)A?p  - band name</br>
 *  	!(where did you get that)A{1,3}[b|B](?) - puerile insults</br>
 *  	hiv+</br>
 *  	!(!)[(Will)|(Is)|(How)|(What)]ANrvtp(?) -  poetry</br>
 *		ANp - simple example</br>
 * </code>
 * @author don_bacon
 * @see org.dwbzen.text.pos.PartsOfSpeach PartsOfSpeach
 * @see org.dwbzen.text.pos.PartOfSpeachPattern PartOfSpeachPattern
 *
 */
public class TextGenerator implements ITextGenerator, Function<Integer, String>, BiConsumer<DataSourceType, String[]> {
	protected static final Logger log = LogManager.getLogger(TextGenerator.class);
	public final static String QUOTE = "\"";
	public final static String SPACE = " ";
	
	private IPartsOfSpeechManager partsOfSpeechManager = null;
	
	private Map<String, List<String>> wordMap = null;
	private Map<String, Integer> posCount = new HashMap<>();
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
	private String delimiter = ",";		// default field separator for CSV
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	
	/**
	 * Support both legacy POS files and new JSON format POS files via the selected IPartsOfSpeechManager<br>
	 * If files are not provided by the runner, the configured POS_FILE is used.
	 * 
	 * @param posManager the IPartsOfSpeechManager to use
	 * @return TextGenerator instance
	 */
	public static TextGenerator newInstance(IPartsOfSpeechManager posManager) {
		return new TextGenerator(posManager);
	}

	/**
	 * Default constructor uses DictionaryManager for IPartsOfSpeechManager
	 */
	public  TextGenerator() {
		setPartsOfSpeechManager(DictionaryManager.instance());
	}
	
	public TextGenerator(IPartsOfSpeechManager posManager) {
		setPartsOfSpeechManager(posManager);
	}
	
		
	/**
	 * Sample patterns: "AANp" "ANp" "NvV" "Np" "Ap" "vVp" "!AN" "ALp"
	 * Choice/range patterns: "[F|M]vtPAp" "N{2,5}" "[F|M]C[F|M]vtPAp"
	 * 
	 * @param numberToGenerate
	 * @param pattern
	 */
	public void generate(int numberToGenerate, String pattern) {
		log.info("pattern: " + pattern);
		addPattern(pattern);
		generate(numberToGenerate);
	}
	
	/**
	 * Picks pattern randomly from an array of patterns
	 * @param numberToGenerate
	 */
	public void generate(int numberToGenerate) {
		int len = partOfSpeechPatterns.size();
		String text = null;
		int counter = 0;
		while(counter < numberToGenerate) {
			int patInd = random.nextInt(len);
			PartOfSpeechPattern posPattern = partOfSpeechPatterns.get(patInd);
			if(!posPattern.isValid()) {
				text = posPattern.getError();
			}
			else {
				text = generateText(posPattern);
			}
			if(text.startsWith("ERROR")) {
				break;
			}
			else if(text.length() > 0) {
				/*
				 * generateText returns an empty String if the pattern is a lambda definition
				 */
				generatedText.add(text);
				counter++;
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
	 * Generates a text String from a given PartOfSpeechPattern.
	 * A PartOfSpeechPattern that defines a lambda function (%name=..) has
	 * a length of 0. The resulting generated text will be an empty String.
	 * @return generated text String or blank, A String that starts with "ERROR" if any errors encountered. 
	 */
	public String generateText(PartOfSpeechPattern posPattern) {
		StringBuffer sb = new StringBuffer();
		List<String> patternTokens = parseInstance(posPattern);
	
		List<String> generatedList = new ArrayList<String>();
		Map<String, String> variables = new HashMap<String, String>();
		boolean isText = false;
		boolean setVariable = false;
		boolean useVariable = false;
		boolean isLambda = false;
		String variable = null;
		for(String posToken : patternTokens) {
			if(posToken.equals(PatternWord.TEXT_DELIMETER)) {
				if(isText) {
					if(setVariable) {
						variable = sb.toString();
					}
					else if(useVariable) {
						generatedList.add(variables.get(sb.toString()));
						useVariable = false;
					}
					else {
						if(!isLambda) {
							generatedList.add(sb.toString());
						}
						else {
							// we have a lambda name
							String lambdaKey = sb.toString();
							Function<Integer, String> lambdaFunction = FunctionManager.getInstance().getFunction(lambdaKey);
							if(lambdaFunction != null) {
								String lambdaResult = lambdaFunction.apply(1);
								generatedList.add(lambdaResult);
							}
							isLambda = false;
						}
						
					}
				}
				else {
					sb = new StringBuffer();
				}
				isText = !isText;
				continue;
			}
			else if(posToken.length() > 1 && posToken.charAt(0) == '$') {	// use a variable 
				useVariable = true;
				sb.append(String.valueOf(posToken.charAt(1)));
				continue;
			}
			else if(posToken.equals("=")) {	// set a variable
				setVariable = true;
				continue;
			}
			else if(posToken.equals("%") && isText) {
				// the inline text is a lambda name and NOT literal text to insert
				isLambda = true;
			}
			if(isText) {
				sb.append(posToken);
			}
			else {
				if(posToken.equalsIgnoreCase("w")) {
					if(posToken.equals("w")) {
						// generate a random number 0 to 9
						generatedList.add(String.valueOf(random.nextInt(10)));
					}
					else {
						// generate a random number 1 to 9
						generatedList.add(String.valueOf(random.nextInt(1, 10)));
					}
				}
				else if(posCount.containsKey(posToken)) {
					int n = posCount.get(posToken);
					if(n > 0) {
						/*
						 * word can actually be 2 words, for example "human rights"
						 * 
						 */
						String word = wordMap.get(posToken).get(random.nextInt(n));
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
					System.err.println("ERROR: TextGenerator: Invalid part of speech: " + posToken);
					return "ERROR: TextGenerator: Invalid part of speech: " + posToken;
				}
			}
		}
		return format(generatedList);
	}
	
	public static List<String> parseInstance(PartOfSpeechPattern posPattern) {
		return PosUtil.parseInstance(posPattern.createInstance(), false);		// returns delimiter with the pos or tag
	}
	
	
	public static final String PUNCUTATION = ".?!;:,\"'";
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
			boolean startOfQuote = false;
			for(int i=0; i<len; i++){
				String s = generatedList.get(i);
				if(s == null || s.length() == 0) {
					log.error("format: null string");
					continue;
				}
				log.debug("'" + s + "'");
				int punctIndex = PUNCUTATION.indexOf(s);	// >=0 if the string is a single PUNCTUATION character
				int blen = sb.length();
				if(punctIndex >= 0 && sb.charAt(blen-1) == ' ') {
					// substitute the final character only if it's a blank
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
						if(isArticle(s) && i > 0 ) {
							sb.append(s);
						}
						else {
							sb.append(s.substring(0, 1).toUpperCase());
							sb.append(s.substring(1));	
						}
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
				startOfQuote = s.length() == 1 && ( s.startsWith("\"") || s.startsWith("'"));
				if(i <len-1 && !startOfQuote) {
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
					if( isArticle(s) && i > 0  ) {
						// don't convert to title case if "and" or "the" and not the first word in the generatedList
						sb.append(QUOTE + s + QUOTE);
					}
					else {
						sb.append(QUOTE + s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + QUOTE);
					}
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
					if( isArticle(s) && i > 0  ) {
						// don't convert to title case if "and" or "the" and not the first word in the generatedList
						sb.append(QUOTE + s + QUOTE);
					}
					else {
						sb.append(QUOTE + s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + QUOTE);
					}
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
	
	public static boolean isArticle(String s) {
		// Strings NOT to convert to title case
		return s.equalsIgnoreCase("and") || s.equalsIgnoreCase("the") || s.equalsIgnoreCase("a");
	}

	public IPartsOfSpeechManager getPartsOfSpeechManager() {
		return partsOfSpeechManager;
	}
	
	public void setPartsOfSpeechManager(IPartsOfSpeechManager manager) {
		partsOfSpeechManager = manager;
		wordMap = partsOfSpeechManager.getWordMap();
		Set<String> loadedPos = LegacyPartsOfSpeechManager.getLoadedPartsOfSpeech();
		loadedPos.forEach(ps -> posCount.put(ps, wordMap.get(ps).size()));
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
			String thePattern = patterns[i];
			addPattern(thePattern);
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
	
	public int setInputPatterns(List<String> patterns) {
		int ret = 0;
		
		return ret;
	}
	
	public int setInputPatterns(File f) {
		int ret = 0;
		List<String> patterns = new ArrayList<String>();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(f));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = fileReader.readLine()) != null) {
				if(line.startsWith("//"))
					continue;
				if(line.endsWith("+")) {
					sb.append(line.substring(0, line.length() - 1));
				}
				else if(sb.length() > 0 ) {
					sb.append(line);
					patterns.add(sb.toString());
					sb = new StringBuilder();
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

	@Override
	/**
	 * Implements Function<Integer, String>
	 */
	public String apply(Integer t) {
		generatedText.clear();
		generate(t);
		return generatedText.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
	}

	@Override
	/**
	 * Configures TextGenerator for a given DataSourceType using comma-delimited parameter arguments provided.</br>
	 * The first parameter is either a path to a template file (TextFile) or a text pattern (Text)</br>
	 * The second parameter is a valid post-processing specification: TC, UC, LC, etc.
	 * 
	 * @param dsType a valid DataSourceType  (TextFile, Resource, or Text)
	 * @param args comma-delimited String arguments
	 * @throws IllegalArgumentException if any problems such as file not found, missing params, or invalid pattern.
	 */
	public void accept(DataSourceType dsType, String[] params) throws IllegalArgumentException {
		if(params.length != 2) {
			throw new IllegalArgumentException("Two arguments are required: pattern/file and post-processing code");
		}
		if(dsType == DataSourceType.TextFile) {
			File f = new File(params[0]);
			if(f.canRead()) {
				int ret = setInputPatterns(f);
				if(ret != 0) {
					throw new IllegalArgumentException("Cannot read template file:" + params[0]);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot read template file:" + params[0]);
			}
		}
		else if(dsType == DataSourceType.Text) {
			addPattern(params[0]);
		}
		else if(dsType == DataSourceType.Resource) {
			TextDao dao = new TextDao();
			List<String> patterns = dao.loadFile(params[0]);
			setPatternList(patterns);
		}
		else  {
			throw new IllegalArgumentException("Unsupported DataSourceType: " + dsType);
		}
		this.setPostProcessing(params[1]);
	}
}
