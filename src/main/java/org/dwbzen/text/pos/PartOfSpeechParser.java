package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.IJson;

/**
 * TODO: migrate to JavaCC
 * @author Don_Bacon
 *
 */
public class PartOfSpeechParser implements IPatternParser, IJson {

	private static final long serialVersionUID = 1L;

	public PartOfSpeechParser() { }
	
	@JsonProperty("words")	private List<String> words = new ArrayList<String>();		// Sentence parsed into Words
	@JsonProperty("valid")	private boolean valid = true;
	@JsonProperty("error")	private String error = "";
	@JsonIgnore				private List<PatternWord> patternWords = new ArrayList<PatternWord>();
	@JsonProperty			private  Set<String> partsOfSpeechSet = PartsOfSpeech.getLoadedPartsOfSpeech();

	public static void main(String[] args) {

	}
	
	/**
	 * replacement variables: $1, ... $9
	 * $1=x will save the value of 'x' in variable $1
	 * ($1) uses that saved value
	 * 
	 * $1=x parses into the PatternWord:
	 * (=1)x - This tells the generator to save the value of
	 *  	   next generated word in $1, x  - a valid part of speech (V, t, N etc)
	 *  
	 * ($1) parses to ($1) to signal in-line text
	 * @return true if pattern is valid, false otherwise
	 * TODO: move to JavaCC grammar
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
			else if(c == '?') {
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

}
