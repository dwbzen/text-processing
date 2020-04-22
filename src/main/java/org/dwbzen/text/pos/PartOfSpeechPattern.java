package org.dwbzen.text.pos;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  (\n) is an embedded '\n'
 *  A pattern can be spread across multiple lines, lines ending with '+' indicate this</br>
 *  Text enclosed in () are included literally, for example (I am) or (\n) for newline</br>
 *  Choices are enclosed in brackets [] and separated by |, for example [p|h] which is a plural noun or a noun phrase. </br>
 *  Literal text may also be included in choices elements as in [(Where)|(Is)|(Why)|(What)]</br>
 *  A repeat range is inclosed in curly brackets as in A{1,3} which indicates 1 to 3 adjectives.</br>
 *  An optional POS element is indicated with a ? as in v? which is an optional adverb.</br>
 *  	This is a shorthand for [0,1]
 *  	Choices may also be optional as in [M|F](?) which is an optional male first name
 *  	or female first name.
 *  Comment lines in pattern files (called templates) start with //</p>
 *  
 *  It is possible to inject BiFunction<DataSourceType, String, String> function as a kind of lambda.</br>
 *  For example, this can be the result of a separate TextGenerator or WordProducer.</br>
 *  To use this feature, the lambda function is first declared in the pattern (or template)</br>
 *  by giving the full class path with arguments assigning to a %name, for example:</p>
 *  <code>
 *  %bands=org.dwbzen.text.pos.TextGenerator(TextFile,bin/main/reference/bandNamePatterns.txt,TC);</code></p>
 *  The first argument of TextGenerator is a valid DataSourceType.</br>
 *  The second argument is an inline pattern or name of a template pattern file<br>
 *  Third argument indicates post-processing: TC, LC, etc. </br>
 *  To use, insert the %variable name in the pattern or template. For example:</p>
 *  <code>
 *  (We went to a)%bands;(concert in)Q(,)R(.) 
 *  </code></br>
 *  Note the use of % and ; delimeters. </br>
 *  Under the hood, the class must implement the interfaces:</br>
 *  Function<Integer, String> - String apply(Integer n), where n=number of strings to generate.</br>
 *  BiConsumer<DataSourceType, String> - for instance creation
 *  </p>
 *  Samples patterns:</br>
 *  	[M|F|N](and)(the)A?p  - band name</br>
 *  	!(where did you get that)A{1,3}[b|B](?) - insult</br>
 *  	hiv+</br>
 *  	!(!)[(Will)|(Is)|(How)|(What)]ANrvtp(?) -  poetry</br>
 *		ANp - simple example</br>
 *
 * @see org.dwbzen.text.pos.TextGenerator
 * @author don_bacon
 *
 */
public class PartOfSpeechPattern extends TextPattern {

	@JsonProperty("rawPattern")	private String sentence;
	@JsonIgnore					private String currentWord = null;
	@JsonIgnore					private int currentIndex = 0;
	@JsonIgnore					private List<String> words;		// Sentence parsed into Words
	@JsonProperty("patternWords")	private List<PatternWord> patternWords;
	@JsonProperty("valid")		private boolean valid;
	@JsonProperty("error")	private String error = "";
	@JsonIgnore				private IPatternParser parser = null;
	
	public final static String errorMessage = "ERROR: invalid Part of Speech Pattern";
	
	public PartOfSpeechPattern(String sentence) {
		setVocabulary(PartsOfSpeechManager.getLoadedPartsOfSpeech());
		setSentence(sentence);
		parse();
	}
	
	
	public PartOfSpeechPattern(String[] sentences) {
		setVocabulary(PartsOfSpeechManager.getLoadedPartsOfSpeech());
		setSentence(sentences.toString());
		parse();
	}
	
	public IPatternParser getPatternParser() {
		if(this.parser == null) {
			parser = new PartOfSpeechParser();
		}
		return parser;
	}
	
	@Override
	public boolean hasNext() {
		return (currentIndex < patternWords.size());
	}

	/**
	 * Iterator returns WordInstance
	 */
	@Override
	public PatternWord next() {
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		return patternWords.get(currentIndex++);
	}
	
	@Override
	public void setVocabulary(Set<String> vocabulary) {
		this.vocabulary = vocabulary;
	}

	private boolean parse() {
		parser = getPatternParser();
		valid = parser.parse(sentence);
		if(valid) {
			words = parser.getWords();
			patternWords = parser.getPatternWords();
		}
		else {
			error = errorMessage + ": " + parser.getError();
			System.err.println(error);
		}
		return valid;
	}
		
	public static void main(String[] args) {
		for(int i=0; i<args.length; i++) {
			PartOfSpeechPattern p = new PartOfSpeechPattern(args[i]);
			if(p.isValid()) {
				System.out.println("PartOfSpeechPattern: " + p.toString());
				System.out.println("           instance: " + p.createInstance());
				System.out.println(p.toJson(true));
				int n = 0;
				while(p.hasNext()) {
					PatternWord pw = p.next();
					System.out.println((++n) + "  " + pw.toString() + " : " + pw.getInstance());
				}
			}
			else {
				System.err.println(p.getError());
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(PatternWord pw : patternWords) {
			sb.append(pw.toString());
		}
		return sb.toString();
	}
	
	/**
	 * Creates an instance of this pattern (with choices resolved)
	 * @return the String instance or if pattern is not valid, the String "ERROR: invalid pattern"
	 */
	public String createInstance() {
		StringBuffer sb = new StringBuffer();
		if(valid) {
			for(PatternWord pw : patternWords) {
				sb.append(pw.getInstance());
			}
		}
		else {
			error = errorMessage;
			sb.append(error);
		}
		return sb.toString();		
	}
	
	public int getLength() {
		return words.size();
	}

	public String getCurrentWord() {
		return currentWord;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public boolean isValid() {
		return valid;
	}

	public String getError() {
		return error;
	}

	@Override
	public void forEachRemaining(Consumer<? super PatternWord> arg0) {
		// TODO Auto-generated method stub
		
	}

}
