package org.dwbzen.text.util.model;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

/**
 *  (\n) is an embedded '\n'
 *  A pattern can be spread across multiple lines, lines ending with '+' indicate this
 *  Text enclosed in () are included literly, for example (I am) or (\n) for newline
 *  Choices are enclosed in brackets [] and separated by |, for example [p|h] which is 
 *  	a plural noun or a noun phrase. Literal text may also be included in choices elements
 *  	as in [(Where)|(Is)|(Why)|(What)]
 *  A repeat range is inclosed in curly brackets as in A{1,3} which indicates 1 to 3 adjectives.
 *  An optional POS element is indicated with a ? as in v? which is an optional adverb.
 *  	This is a shorthand for [0,1]
 *  	Choices may also be optional as in [M|F](?) which is an optional male first name
 *  	or female first name.
 *  Comment lines in pattern files (called libraries) start with //
 *  Samples patterns:
 *  	[M|F|N](and)(the)A?p  - band name
 *  	!(where did you get that)A{1,3}[b|B](?) - insult
 *  	hiv+
 *  	!(!)[(Will)|(Is)|(How)|(What)]ANrvtp(?) -  poetry
 *		ANp - simple example
 *	
 * @author Don_Bacon
 *
 */
public class PartOfSpeechPattern extends TextPattern {

	private String sentence;
	private String currentWord = null;
	private int currentIndex = 0;
	private List<String> words;		// Sentence parsed into Words
	private List<PatternWord> patternWords;
	private IPatternParser parser = null;
	
	public PartOfSpeechPattern(String sentence) {
		setVocabulary(PartsOfSpeech.getLoadedPartsOfSpeech());
		setSentence(sentence);
		parse();
	}
	
	
	public PartOfSpeechPattern(String[] sentences) {
		setVocabulary(PartsOfSpeech.getLoadedPartsOfSpeech());
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

	private void parse() {
		parser = getPatternParser();
		parser.parse(sentence);
		words = parser.getWords();
		patternWords = parser.getPatternWords();
	}
		
	public static void main(String[] args) {
		for(int i=0; i<args.length; i++) {
			PartOfSpeechPattern p = new PartOfSpeechPattern(args[i]);
			System.out.println("PartOfSpeechPattern: " + p.toString());
			System.out.println("           instance: " + p.createInstance());
			int n = 0;
			while(p.hasNext()) {
				PatternWord pw = p.next();
				System.out.println((++n) + "  " + pw.toString() + " : " + pw.createInstance());
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<patternWords.size(); i++) {
			sb.append(patternWords.get(i).toString());
		}
		return sb.toString();
	}
	
	public String createInstance() {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<patternWords.size(); i++) {
			sb.append(patternWords.get(i).createInstance());
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


	@Override
	public void forEachRemaining(Consumer<? super PatternWord> arg0) {
		// TODO Auto-generated method stub
		
	}

}
