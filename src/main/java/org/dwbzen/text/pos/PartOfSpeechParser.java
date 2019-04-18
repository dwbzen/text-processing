package org.dwbzen.text.pos;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.dwbzen.common.util.IJson;
import org.dwbzen.text.util.DataSourceType;
import org.dwbzen.text.util.FunctionManager;
import org.dwbzen.text.util.exception.ConfigurationException;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parse POS pattern text into PatternWords for text generation.
 * 
 * @author don_bacon
 *
 */
public class PartOfSpeechParser implements IPatternParser, IJson {

	private static final long serialVersionUID = 1L;

	public PartOfSpeechParser() { }
	
	@JsonProperty("words")	private List<String> words = new ArrayList<String>();		// Sentence parsed into Words
	@JsonProperty("valid")	private boolean valid = true;
	@JsonProperty("error")	private String error = "";
	@JsonProperty("patterns")	private List<PatternWord> patternWords = new ArrayList<PatternWord>();
	@JsonProperty			private  Set<String> partsOfSpeechSet = PartsOfSpeechManager.getLoadedPartsOfSpeech();

	/**
	 * replacement variables: $1, ... $9
	 * $1=x will save the value of 'x' in variable $1
	 * ($1) uses that saved value
	 * 
	 * @return true if pattern is valid, false otherwise
	 */
	public boolean parse(String sentence) {

		int parsePosition = 0;
		valid = true;
		int rangeDelim = -1;
		int parenDelim = -1;
		int length = (sentence != null) ? sentence.length() : 0;
		int lastCharPosition = length-1;
		boolean isChoice = false;
		boolean isRange = false;
		boolean isText = false;
		int rangeLow = -1;
		int rangeHigh = 1;
		StringBuffer choices = null;
		List<String> textChoices = new ArrayList<String>();
		String inlineText = null;
		String word = null;
		char previousChar = 0;
		char variable  = 0;
		char nextChar = 0;
		PatternWord patternWord = null;
		char c = 0;
		String text = null;
		while(parsePosition <= lastCharPosition) {
			previousChar = c;
			c = sentence.charAt(parsePosition++);
			nextChar = (parsePosition >= lastCharPosition) ? 0 : sentence.charAt(parsePosition);
			
			if(c == ' ' || c == '|' || c == '\t') continue;
			if(c == '[') {
				// parse choices [a | b | c etc. ]
				isChoice = true;
				choices = new StringBuffer();
				textChoices = new ArrayList<String>();
				if(isText) {	// close out inline Text if needed
					words.add(inlineText);
					patternWord = new PatternWord(1, 1, null, inlineText);
					patternWords.add(patternWord);
					isText = false;
					inlineText = null;
				}
			}
			else if(c == ']') {
				// close out the choice
				isChoice = false;
				if(parsePosition > lastCharPosition || !(sentence.charAt(parsePosition) == '{' || sentence.charAt(parsePosition) == '?') ) {
					if(textChoices.size() > 0) {
						words.add(textChoices.toString());
						patternWord = new PatternWord(1, 1, null, textChoices);
						patternWords.add(patternWord);
						textChoices =  new ArrayList<String>();
					}
					else {
						word = choices.toString();
						words.add(word);
						patternWord = new PatternWord(word);
						patternWords.add(patternWord);
						choices = null;
					}
				}
				continue;
			}
			else if(c == '{') {
				// parse range: x{n} or x{m,n}
				rangeDelim = parsePosition;
				isRange = true;
				continue;
			}
			else if(c == ',') {
				if(isText)
					continue;
				rangeLow = Integer.parseInt(sentence.substring(rangeDelim, parsePosition-1));
				rangeDelim = parsePosition;
				continue;
			}
			else if(c == '}') {
				rangeHigh = Integer.parseInt(sentence.substring(rangeDelim, parsePosition-1));
				if(rangeLow < 0) {
					rangeLow = rangeHigh;
				}
				if(variable != 0) {
					patternWord =new PatternWord(rangeLow, rangeHigh, null, variable);
				}
				else if(textChoices.size()>1) {
					words.add(textChoices.toString());
					patternWord = new PatternWord(rangeLow, rangeHigh, null, textChoices);
				}
				else if(choices != null && choices.length()>0) {
					word = choices.toString();
					words.add(word);
					patternWord = new PatternWord(rangeLow, rangeHigh, word);
				}
				else {
					words.add(word);
					patternWord = new PatternWord(rangeLow, rangeHigh, word, inlineText);
				}
				patternWords.add(patternWord);
				rangeLow = -1;
				rangeHigh = 1;
				isRange = false;
				isText = false;
				inlineText = null;
				choices = null;
				word = null;
				variable = 0;
				continue;
			}
			else if(c == '?' && !isText) {		// (?) punctuation
				words.add(word);
				patternWord = new PatternWord(0, 1, word);
				patternWords.add(patternWord);				
			}
			else if(c == '(') {	// start of inline text or inline text choices
				parenDelim = parsePosition;
				isText = true;
				//isChoice = false;
			}
			else if(c == ')') {	// end of inline text
				if(parenDelim <0 || parenDelim > parsePosition-1) {
					valid = false;
					error = "parser error";
					return valid;	// invalid pattern
				}
				inlineText = sentence.substring(parenDelim, parsePosition-1);
				text = inlineText.equals("\\n") ? "\n" : inlineText;	// end of line - continuation
				if(isChoice) {
					textChoices.add(text);
				}
				else {
					if(nextChar != '{') {
						if(variable != 0) {
							patternWord =new PatternWord(1, 1, null, variable);
							variable = 0;
						}
						else {
							words.add(text);
							patternWord = new PatternWord(1, 1, null, text);
							inlineText = null;
						}
						patternWords.add(patternWord);
					}
				}
				isText = false;
			}
			else if(c == '$') {
				// use of variable: $1...$9
				// "($1)" or "$1=x"
				//
				if(previousChar == '(') {	// as in "($1)"
					variable  = sentence.charAt(parsePosition++);
				}
				else {	// as in "$1=x"
					variable = sentence.charAt(parsePosition++);
					parsePosition++;
					patternWord = new PatternWord(1, 1, String.valueOf(sentence.charAt(parsePosition++)), variable);
					patternWords.add(patternWord);
					variable = 0;
				}
			}
			else if(c=='%') {
				// defining or using a lambda expression. Ending delimiter is ';'
				// fast forward to the trailing ; delimiter and hand the whole
				// thing off for processing
				int ind = sentence.indexOf(';', parsePosition);
				if(ind < 0) {
					return false;
				}
				// substring retains the initial % but omits the trailing ';'
				String lambdaString = sentence.substring(parsePosition-1, ind);
				parsePosition = ind +1;
				if(lambdaString.contains("=")) {
					createFunctionReference(lambdaString);
				}
				else {
					// PatternWord treat the lambda name (%aname) as in-line text, empty choices
					patternWord = new PatternWord(1, 1, "", lambdaString);
					patternWords.add(patternWord);
				}
			}
			else {
				if(isChoice && !isText) {
					choices.append(c);
				}
				else if(isRange || isText) {
					continue;
				}
				else {
					word = String.valueOf(c);
					if(isText) {
						words.add(inlineText);
						patternWord = new PatternWord(1, 1, null, inlineText);
						patternWords.add(patternWord);
						isText = false;
						inlineText = null;
					}
					if(parsePosition > lastCharPosition || !(sentence.charAt(parsePosition) == '{' || sentence.charAt(parsePosition) == '?') ) {
						if(partsOfSpeechSet.contains(word)) {
							words.add(word);
							patternWord = new PatternWord(word);
							patternWords.add(patternWord);
						}
						else {
							valid = false;
							error = "invalid part of speech: " + word;
							return valid;	// invalid pattern
						}
					}
				}
			}
		}
		return valid;
	}

	/**
	 * example lambda string: %bands=org.dwbzen.text.pos.TextGenerator(TextFile,bin/main/reference/bandNamePatterns.txt,TC)</br>
	 * Function references stored in functionMap, key is the name including the initial '%'.</br>
	 * The class must implement BiConsumer<DataSourceType, String> for creation semantics and have a default constructor.
	 * 
	 * @param lambdaString
	 */
	@SuppressWarnings("unchecked")
	public boolean createFunctionReference(String lambdaString) throws ConfigurationException {
		boolean valid = true;
		String className = null;
		String[] params = null;
		String dsType = null;
		Class<BiConsumer<DataSourceType, String[]>> consumerClass = null;
		BiConsumer<DataSourceType, String[]> consumer = null;
		DataSourceType t = null;
		Function<Integer, String> function = null;
		
		int endIndex = lambdaString.indexOf('=');
		String key = lambdaString.substring(0, endIndex);
		int parenIndexOpen = lambdaString.indexOf('(');
		int parenIndexClose = lambdaString.lastIndexOf(')');
		int commaIndex = lambdaString.indexOf(',', parenIndexOpen);
		if(parenIndexOpen > 0 && parenIndexClose > parenIndexOpen && commaIndex > 0) {
			className = lambdaString.substring(endIndex+1, parenIndexOpen);
			params = lambdaString.substring(commaIndex+1, parenIndexClose).split(",");
			dsType = lambdaString.substring(parenIndexOpen+1, commaIndex);
			try {
				consumerClass = (Class<BiConsumer<DataSourceType, String[]>>)Class.forName(className);
				consumer = (BiConsumer<DataSourceType, String[]>)consumerClass.getDeclaredConstructor().newInstance();
				switch (dsType) {
					case "TextFile": t = DataSourceType.TextFile;
					break;
					case "Text": t = DataSourceType.Text;
					break;
					case "JsonText": t = DataSourceType.JsonText;
					break;
					case "Resource": t = DataSourceType.Resource;
					break;
				}
				consumer.accept(t, params);
				function = (Function<Integer, String>)consumer;
				FunctionManager manager = FunctionManager.getInstance();
				manager.addFunction(key, function);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("Configuration error: " + e.getMessage());
				e.printStackTrace();
				throw new ConfigurationException(e.getMessage() + " " + className);
			}
		}
		else {
			valid = false;
		}
		return valid;
	}

	public List<String> getWords() {
		return words;
	}

	public List<PatternWord> getPatternWords() {
		return patternWords;
	}

	public boolean isValid() {
		return valid;
	}

	public String getError() {
		return error;
	}

	public Set<String> getPartsOfSpeechSet() {
		return partsOfSpeechSet;
	}

	
	public static void main(String...strings ) {
		// simple test
		PartOfSpeechParser parser = new PartOfSpeechParser();
		String lambdaString = strings[0];
		boolean valid = parser.createFunctionReference(lambdaString);
		System.out.println("valid: " + valid);
		Function<Integer, String> function = FunctionManager.getInstance().getFunction("%band");
		String bandName = function.apply(1);
		System.out.println(bandName);
		
	}
}
