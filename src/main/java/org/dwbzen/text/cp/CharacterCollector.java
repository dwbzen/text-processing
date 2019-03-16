package org.dwbzen.text.cp;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import org.dwbzen.common.cp.CollectorStats;
import org.dwbzen.common.cp.ICollector;
import org.dwbzen.common.cp.MarkovChain;

/**
 * Collects statistics on Characters within Words. The corresponding producer is WordProducer. </p>
 * CharacterCollector implements the following functional interfaces through ICollector:</p>
 * 	Function<Word, MarkovChain<Character, Word>> : apply(Word w) takes Word input, produces MarkovChain result</p>
 * 	Consumer<Sentence>  : accept(Sentence s) consumes Sentence(s) to create the MarkovChain</p>
 * 
 * The resulting MarkovChain (CollectorStatsMap) uses 2 special characters to indicate terminal and null values.
 * For example the testWordSample.txt has 5 Sentences (in this example each Sentence is a single Word)</p>
 	DON
	DONALD
	DONNA
	ALDO
	NEDA
 * The MarkovChain returned running with -order 3 includes the following which can be verified by inspecting the data.
 * 		'ALD'	2
 * 		'O'	1	1,1		0.5
 * 		'¶'	1	2,2		0.5
 * 
 * The key "ALD" occurs twice (DONALD and ALDO)
 * it is followed by an 'O' once (in ALDO), and by a logical end of word (the TERMINAL '¶') in DONALD
 * 
 * 		'DA¶'	1
 *		 '§'	1	1,1		1.0
 * The key 'DA¶' occurs once in NEDA (it ends in "DA")
 * As is evident from the data, there is no Word where DA is followed by a Character.
 * This is indicated by the stand-in Character value for null '§'
 * Start of a Word is indicated by a leading space as in " DO" 
 * Running the CharacterCollector on the sample in trace mode shows how the data is processed.
 * 
 * 
 * @author don_bacon
 *
 */
public class CharacterCollector implements ICollector<Word, MarkovChain<Character, Word, Sentence>, Sentence> {
	protected static final Logger log = LogManager.getLogger(CharacterCollector.class);
	private boolean ignoreCase = true; 
	private int order; 
	private String[] fileNames = {};
	private String partsOfSpeach = null;	// only applies to parts of speech files
	private StringBuilder text = new StringBuilder();
	private boolean trace = false;
	private Character[] filters = {'-', '.'};	// filters words not to include if they contain these characters
	private MarkovChain<Character, Word, Sentence> markovChain = null;
	private Sentence sentence = null;	// current Sentence we are collecting Words in
	private Word word = null;	// current Word we are scanning
	
	/**
	 * 
	 * @param order length of the key in #of characters, usually 2 or 3
	 * @param filename full path to the input file. Use null for STDIN
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * 
	 * @throws FileNotFoundException
	 */
	protected CharacterCollector(int order, String[] fileNames, boolean ignoreCase) {
		this.order = order;
		this.fileNames = fileNames;
		this.ignoreCase = ignoreCase;
	}
	
	protected CharacterCollector(int order, String[] fileNames, boolean ignoreCase, String pos) {
		this(order, fileNames, ignoreCase);
		partsOfSpeach = pos;
	}
	
	@Override
	public void collect() {
		sentence = new Sentence(text.toString(), true);
		accept(sentence);
	}
	
	@Override
	public void collect(List<Word> words) {
		words.stream().forEach(w -> apply(w));
	}

	@Override
	public void accept(Sentence sentence) {
		log.debug("accept sentence: " + sentence);
		while((word = sentence.get()) != null) {
			apply(word);
		}
	}

	@Override
	public MarkovChain<Character, Word, Sentence> apply(Word theWord) {
		Word word = new Word(Word.DELIM_STRING, theWord);	// space is the word delimiter
		Word subset = null;
		Character nextChar = null;
		logMessage("apply(word): '" + word + "'");
		int numberOfTokens = word.size();
		int lim = numberOfTokens - order + 1;
		if(lim >= 0) {
			if(!shouldFilter(word)) {
				for(int i=0; i< lim; i++) {
					int index = i+order;
					if(index <= numberOfTokens) {		// don't run off the end of the List
						subset = word.subset(i, index);
						nextChar = (index == numberOfTokens) ? Word.TERMINAL : word.get(i+order);
						logMessage("  subset: '" + subset + "' next char: '" + nextChar + "'");
						boolean initial = (i==0);	// start of a Word?
						addOccurrence(subset, nextChar, initial);
					}
				}
				// add terminal state
				subset = word.subset(lim);
				subset.append(Word.TERMINAL);	// '¶'
				log.debug("  terminal state, subset: '" + subset + "'");
				addOccurrence(subset, Word.NULL_VALUE, false);	// '§'
			}
		}
		return markovChain;
	}
	
	/**
	 * the Supplier is just the Word we are currently working on as a Sentence whose name and id is the Word we are working on.
	 * @param theWord
	 * @param theChar
	 * @param initial
	 */
	protected void addOccurrence(Word theWord, Character theChar, boolean initial) {
		boolean terminal = theChar.equals(Word.NULL_VALUE);
		Sentence supplierSentence = new Sentence(null, true, null, word, word.getWordString(), false,  word.getWordString());
		if(markovChain.containsKey(theWord)) {
			CollectorStats<Character, Word, Sentence> collectorStats = markovChain.get(theWord);
			collectorStats.addOccurrence(theChar, supplierSentence);
			collectorStats.setTerminal(terminal);
			collectorStats.setInitial(initial);
		}
		else {
			CollectorStats<Character, Word, Sentence> collectorStats = new CollectorStats<Character, Word, Sentence>();
			collectorStats.setSubset(theWord);
			collectorStats.addOccurrence(theChar, supplierSentence);
			collectorStats.setTerminal(terminal);
			collectorStats.setInitial(initial);
			markovChain.put(theWord, collectorStats);
		}
	}

	public Map<Word, Integer> getSummaryMap() {
		return getMarkovChain().getSummaryMap();
	}
	
	public MarkovChain<Character, Word, Sentence> getMarkovChain() {
		return markovChain;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public void setText(StringBuilder text) {
		this.text = text;
	}

	public void setMarkovChain(MarkovChain<Character, Word, Sentence> markovChain) {
		this.markovChain = markovChain;
	}

	public String getText() {
		return text.toString();
	}

	public void setText(String str) {
		this.text.append(str);
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public int getOrder() {
		return order;
	}

	public String[] getFileNames() {
		return fileNames;
	}
	
	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
		markovChain.setTrace(trace);
	}
	
	public Character[] getFilters() {
		return filters;
	}

	public void setFilters(Character[] filters) {
		this.filters = filters;
	}

	public boolean shouldFilter(Word word) {
		boolean result = false;
		for(Character s : filters) {
			result |= word.contains(s);
		}
		return result;
	}
	
	public String getPartsOfSpeach() {
		return partsOfSpeach;
	}

	public void setPartsOfSpeach(String partsOfSpeach) {
		this.partsOfSpeach = partsOfSpeach;
	}

	private void logMessage(String text) {
		log.debug(text);
		if(trace) {
			System.out.println(text);
		}
	}

}
