package util.cp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.TextFileReader;
import util.text.Sentence;
import util.text.Word;

/**
 * Collects statistics on Characters within Words
 * The corresponding producer is WordProducer.
 * 
 * @author don_bacon
 *
 */
public class CharacterCollector implements ICollector<Word, MarkovChain<Character, Word>, Sentence> {
	protected static final Logger log = LogManager.getLogger(CharacterCollector.class);
	private boolean ignoreCase = true; 
	private int keylen; 
	private String fileName = null;
	private String text = null;
	private MarkovChain<Character, Word> markovChain = new MarkovChain<Character, Word>(null);
	private static TextFileReader reader = null;
	
	/**
	 * Factory method.
	 * @param keylen length of the key in #of characters, usually 2 or 3
	 * @param inputFile full path to the input file. Use null for STDIN
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * @return CharacterCollector instance
	 */
	public static CharacterCollector getCharacterCollector(int keylen, String inputFile, boolean ignorecaseflag) throws IOException {
		CharacterCollector collector = new CharacterCollector(keylen, inputFile, ignorecaseflag);
		if(inputFile != null) {
			reader = TextFileReader.getInstance(inputFile);
			collector.setText( (ignorecaseflag?  
							reader.getFileText().toLowerCase() :
							reader.getFileText()) );
		}	// else use setText() method
		return collector;
	}
	
	/**
	 * 
	 * @param keylen length of the key in #of characters, usually 2 or 3
	 * @param filename full path to the input file. Use null for STDIN
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * 
	 * @throws FileNotFoundException
	 */
	protected CharacterCollector(int keylen, String filename, boolean ignorecaseflag) {
		this.keylen = keylen;
		this.fileName = filename;
		this.ignoreCase = ignorecaseflag;
	}
	
	@Override
	public void collect() {
		Sentence sentence = new Sentence(text, true);
		accept(sentence);
	}

	@Override
	public void accept(Sentence sentence) {
		log.info("accept sentence: " + sentence);
		Word word = null;
		while((word = sentence.get()) != null) {
			apply(word);
		}
	}

	@Override
	public MarkovChain<Character, Word> apply(Word theWord) {
		Word word = new Word(" ",theWord);	// space is the word delimiter
		Word subset = null;
		Character nextChar = null;
		log.debug("word: '" + word + "'");
		int numberOfTokens = word.size();
		int lim = numberOfTokens - keylen + 1;
		for(int i=0; i< lim; i++) {
			int index = i+keylen;
			if(index <= numberOfTokens) {		// don't run off the end of the List
				subset = word.subset(i, index);
				nextChar = (index == numberOfTokens) ? Word.TERMINAL : word.get(i+keylen);
				log.debug("  subset: '" + subset + "' next char: '" + nextChar + "'");
				addOccurrence(subset, nextChar);
			}
		}
		// add terminal state
		subset = word.subset(lim);
		subset.append(Word.TERMINAL);
		log.debug("  terminal state, subset: '" + subset + "'");
		addOccurrence(subset, Word.NULL_VALUE);
		return markovChain;
	}
	
	private void addOccurrence(Word theWord, Character theChar) {
		boolean terminal = theChar.equals(Word.NULL_VALUE);
		if(markovChain.containsKey(theWord)) {
			CollectorStats<Character, Word> collectorStats = markovChain.get(theWord);
			collectorStats.addOccurrence(theChar);
			collectorStats.setTerminal(terminal);
		}
		else {
			CollectorStats<Character, Word> collectorStats = new CollectorStats<Character, Word>();
			collectorStats.setSubset(theWord);
			collectorStats.addOccurrence(theChar);
			collectorStats.setTerminal(terminal);
			markovChain.put(theWord, collectorStats);
		}
	}

	public Map<Word, Integer> getSummaryMap() {
		return getMarkovChain().getSummaryMap();
	}
	
	public MarkovChain<Character, Word> getMarkovChain() {
		return markovChain;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public int getKeylen() {
		return keylen;
	}

	public String getFileName() {
		return fileName;
	}
	
	public static void main(String...args) throws IOException {
		String filename = null;
		String text = null;
		int keylen = 2;
		boolean ignoreCase = false;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				filename = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-keylen")) {
				keylen = Integer.parseInt(args[++i]);
			}
			else {
				text = args[i];
			}
		}
		CharacterCollector collector = CharacterCollector.getCharacterCollector(keylen, filename, ignoreCase);
		if(filename == null) {
			collector.setText(text);
		}
		collector.collect();
		MarkovChain<Character, Word> markovChain = collector.getMarkovChain();
		markovChain.display();
		markovChain.displaySummaryMap();
		System.out.println(markovChain.toJSON());
		
	}

}
