package util.cp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.cp.CollectorStats;
import mathlib.cp.MarkovChain;
import mathlib.cp.ICollector;
import util.TextFileReader;
import util.text.Sentence;
import util.text.Word;

/**
 * Collects statistics on Characters within Words
 * The corresponding producer is WordProducer.
 * CharacterCollector implements the following functional interfaces through ICollector:
 * 	Function<Word, MarkovChain<Character, Word>> : apply(Word w) takes Word input, produces MarkovChain result
 * 	Consumer<Sentence>  : accept(Sentence s) consumes Sentence(s) to create the MarkovChain
 * 
 * The resulting MarkovChain (CollectorStatsMap) uses 2 special characters to indicate terminal and null values.
 * For example the testWordSample.txt has 5 Sentences (in this example each Sentence is a single Word)
 	DON
	DONALD
	DONNA
	ALDO
	NEDA
 * The resulting MarkovChain includes the following which can be verified by inspecting the data.
 * 		'ALD'	2
 * 		'O'	1	1,1		0.5
 * 		'¶'	1	2,2		0.5
 * The key "ALD" occurs twice (DONALD and ALDO)
 * it is followed by an 'O' once (in ALDO), and by a logical end of word (the TERMINAL '¶') in DONALD
 * 
 * 		'DA¶'	1
 *		 '§'	1	1,1		1.0
 * The key 'DA¶' occurs once in NEDA (it ends in "DA")
 * As is evident from the data, there is no Word where DA is followed by a Character.
 * This is indicated by the stand-in Character value for null '§'
 * Running the CharacterCollector on the sample in trace mode shows how the data is processed.
 * 
 * 
 * @author don_bacon
 *
 */
public class CharacterCollector implements ICollector<Word, MarkovChain<Character, Word>, Sentence> {
	protected static final Logger log = LogManager.getLogger(CharacterCollector.class);
	private boolean ignoreCase = true; 
	private int order; 
	private String fileName = null;
	private String text = null;
	private boolean trace = false;
	private MarkovChain<Character, Word> markovChain;
	private static TextFileReader reader = null;
	
	/**
	 * Factory method.
	 * @param order length of the key in #of characters, usually 2 or 3
	 * @param inputFile full path to the input file. Use null for STDIN
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * @return CharacterCollector instance
	 */
	public static CharacterCollector getCharacterCollector(int order, String inputFile, boolean ignorecaseflag) throws IOException {
		CharacterCollector collector = new CharacterCollector(order, inputFile, ignorecaseflag);
		collector.setMarkovChain(new MarkovChain<Character, Word>(order));
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
	 * @param order length of the key in #of characters, usually 2 or 3
	 * @param filename full path to the input file. Use null for STDIN
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * 
	 * @throws FileNotFoundException
	 */
	protected CharacterCollector(int order, String filename, boolean ignorecaseflag) {
		this.order = order;
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
		log.debug("accept sentence: " + sentence);
		Word word = null;
		while((word = sentence.get()) != null) {
			apply(word);
		}
	}

	@Override
	public MarkovChain<Character, Word> apply(Word theWord) {
		Word word = new Word(Word.DELIM_STRING, theWord);	// space is the word delimiter
		Word subset = null;
		Character nextChar = null;
		logMessage("apply(word): '" + word + "'");
		int numberOfTokens = word.size();
		int lim = numberOfTokens - order + 1;
		for(int i=0; i< lim; i++) {
			int index = i+order;
			if(index <= numberOfTokens) {		// don't run off the end of the List
				subset = word.subset(i, index);
				nextChar = (index == numberOfTokens) ? Word.TERMINAL : word.get(i+order);
				logMessage("  subset: '" + subset + "' next char: '" + nextChar + "'");
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
	
	void setMarkovChain( MarkovChain<Character, Word> markovChain) {
		this.markovChain = markovChain;
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

	public int getOrder() {
		return order;
	}

	public String getFileName() {
		return fileName;
	}
	
	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
		markovChain.setTrace(trace);
	}
	
	private void logMessage(String text) {
		log.debug(text);
		if(trace) {
			System.out.println(text);
		}
	}

	public static void main(String...args) throws IOException {
		String filename = null;
		String text = null;
		int order = 2;
		boolean ignoreCase = false;
		boolean trace = false;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				filename = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				order = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = true;
			}
			else {
				text = args[i];
			}
		}
		CharacterCollector collector = CharacterCollector.getCharacterCollector(order, filename, ignoreCase);
		if(filename == null) {
			collector.setText(text);
		}
		collector.setTrace(trace);
		collector.collect();
		MarkovChain<Character, Word> markovChain = collector.getMarkovChain();
		markovChain.display();
		markovChain.displaySummaryMap();
		System.out.println(markovChain.toJson());
		
	}

}
