package org.dwbzen.text.util.cp;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.TextFileReader;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Book.TYPE;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.cp.CollectorStats;
import mathlib.cp.ICollector;
import mathlib.cp.MarkovChain;

/**
 * Analyzes word collections (Strings delimited by white space and/or punctuation) 
 * and collects statistics in the form of a MarkovChain that
 * can be used by a Producer class.
 * For a given string (inline or from a file/stream), this examines all the word collections
 * of a given length 1 to n (n <= 5), and records the #instances of each word that
 * follows that word collection. Sentences formed left to right advancing  1 Word each iteration.
 * 
 * Definitions:
 * A Word is a non-empty String (length >0) treated as a unit.
 * A Sentence consists of Word(s) delimited by white space (matches Pattern \s+, [ \t\n\x0B\f\r] )
 * A Word consist of one or more word characters, 0 or more dashes, 0 or more single quotes.
 * i.e. Pattern [a-zA-Z_0-9-']
 *
 * @author don_bacon
 *
 */
public class WordCollector implements ICollector<Sentence, MarkovChain<Word, Sentence>, Book> {
	protected static final Logger log = LogManager.getLogger(WordCollector.class);
	private boolean ignoreCase = false; 
	private int order;
	private String text = null;
	private MarkovChain<Word, Sentence> markovChain;
	private Book book = new Book();
	private Book.TYPE	bookType = TYPE.PROSE;	// default
	private static TextFileReader reader = null;
	
	/**
	 * Factory method. Uses default Book TYPE of PROSE
	 * @param order length of the key in #of Words, usually 2 or 3
	 * @param inputFile full path to the input file.
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * @return WordCollector instance
	 */
	public static WordCollector getWordCollector(int order, String inputFile, boolean ignorecaseflag) throws IOException {
		WordCollector collector = getWordCollector(order, inputFile, ignorecaseflag, TYPE.PROSE);
		return collector;
	}
	
	public static WordCollector getWordCollector(int order, String inputFile, boolean ignorecaseflag, TYPE type) throws IOException {
		String sourceText = "";
		if(inputFile != null) {
			reader = (type.equals(TYPE.VERSE)) ? TextFileReader.getInstance(inputFile, "\n") : TextFileReader.getInstance(inputFile);
			if(type.equals(TYPE.VERSE)) {
				// TODO
			}
			sourceText = reader.getFileText() ;
		}
		WordCollector collector = getWordCollector(order, sourceText, type);
		collector.setBookType(type);
		collector.setIgnoreCase(ignorecaseflag);
		return collector;
	}
	
	public static WordCollector getWordCollector(int order, String text, TYPE type) throws IOException {
		WordCollector collector = new WordCollector();
		collector.setOrder(order);
		collector.setText(text);		
		Book book = new Book(text);
		book.setType(type);
		collector.setBook(book);
		collector.setMarkovChain(new MarkovChain<Word, Sentence>(order));
		return collector;
	}
	
	protected WordCollector() {
	}
	
	protected WordCollector(int order, boolean ignorecaseflag) {
		this.order = order;
		this.ignoreCase = ignorecaseflag;
	}
	
	@Override
	public void collect() {
		accept(book);
	}
	
	@Override
	public void accept(Book sourceBook) {
		log.debug("accept book");
		Sentence sentence = null;
		while((sentence = sourceBook.get())  != null ) {
			apply(sentence);
		}
	}

    @Override
	public MarkovChain<Word, Sentence> apply(Sentence sentence) {
    	Sentence subset = null;
    	Word nextWord = null;
    	if(sentence.size() > 0) {	// blank line
	    	log.debug("apply: '" + sentence + "'");
	    	int numberOfTokens = sentence.size();
			int lim = numberOfTokens - order + 1;
			for(int i=0; i< lim; i++) {
				int index = i+order;
				if(index <= numberOfTokens) {		// don't run off the end of the List
					subset = sentence.subset(i, index);
					nextWord = (index == numberOfTokens) ? Sentence.TERMINAL : sentence.get(i+order);
					log.debug("  subset: '" + subset + "' next word: '" + nextWord + "'");
					addOccurrence(subset, nextWord);
				}
			}
			// add terminal state
			subset = sentence.subset(lim);
			subset.append(Sentence.TERMINAL);
			log.debug("  terminal subset: '" + subset + "'");
			addOccurrence(subset, Sentence.NULL_VALUE);
    	}
		return markovChain;
	}

    private void addOccurrence(Sentence theSentence, Word theWord) {
    	boolean terminal = theWord.equals(Sentence.NULL_VALUE);
		if(markovChain.containsKey(theSentence)) {
			CollectorStats<Word, Sentence> collectorStats = markovChain.get(theSentence);
			collectorStats.addOccurrence(theWord);
			collectorStats.setTerminal(terminal);
		}
		else {
			CollectorStats<Word, Sentence> collectorStats = new CollectorStats<>();
			collectorStats.setSubset(theSentence);
			collectorStats.addOccurrence(theWord);
			collectorStats.setTerminal(terminal);
			markovChain.put(theSentence, collectorStats);
		}
    }

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
	public MarkovChain<Word, Sentence> getMarkovChain() {
		return markovChain;
	}

	void setMarkovChain(MarkovChain<Word, Sentence> markovChain) {
		this.markovChain = markovChain;
	}
	
	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Book.TYPE getBookType() {
		return bookType;
	}

	public void setBookType(Book.TYPE bookType) {
		this.bookType = bookType;
	}

	public static void main(String...args) throws IOException {
		String inputFile = null;
		String text = null;
		String textType = null;
		int order = 2;
		boolean ignoreCase = false;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				order = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-type")) {
				textType = args[++i];
			}
			else {
				text = args[i];
			}
		}
		Book.TYPE type = (textType != null && textType.equalsIgnoreCase("verse")) ? TYPE.VERSE : TYPE.PROSE;
		WordCollector collector = (inputFile != null) ?
				WordCollector.getWordCollector(order, inputFile, ignoreCase, type) :
				WordCollector.getWordCollector(order, text, type);
		collector.collect();
		MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
		markovChain.display();
		markovChain.displaySummaryMap();
		System.out.println(markovChain.toJson());
	}
	
}
