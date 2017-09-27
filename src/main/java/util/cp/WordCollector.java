package util.cp;
import util.text.Sentence;
import util.text.Word;
import util.text.Book.TYPE;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.TextFileReader;
import util.text.Book;

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
 * @author bacond6
 *
 */
public class WordCollector implements ICollector<Sentence, MarkovChain<Word, Sentence>, Book> {
	protected static final Logger log = LogManager.getLogger(WordCollector.class);
	private boolean ignoreCase = false; 
	private int keylen;
	private String text = null;
	private MarkovChain<Word, Sentence> markovChain = new MarkovChain<Word, Sentence>(null);
	private Book book = new Book();
	private Book.TYPE	bookType = TYPE.PROSE;	// default
	private static TextFileReader reader = null;
	
	/**
	 * Factory method. Uses default Book TYPE of PROSE
	 * @param keylen length of the key in #of Words, usually 2 or 3
	 * @param inputFile full path to the input file.
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * @return WordCollector instance
	 */
	public static WordCollector getWordCollector(int keylen, String inputFile, boolean ignorecaseflag) throws IOException {
		String sourceText = "";
		if(inputFile != null) {
			reader = TextFileReader.getInstance(inputFile);
			sourceText = reader.getFileText() ;
		}
		WordCollector collector = getWordCollector(keylen, sourceText, TYPE.PROSE);
		collector.setIgnoreCase(ignorecaseflag);
		return collector;
	}
	
	public static WordCollector getWordCollector(int keylen, String inputFile, boolean ignorecaseflag, TYPE type) throws IOException {
		String sourceText = "";
		if(inputFile != null) {
			reader = (type.equals(TYPE.VERSE)) ? TextFileReader.getInstance(inputFile, "\n") : TextFileReader.getInstance(inputFile);
			if(type.equals(TYPE.VERSE)) {
				
			}
			sourceText = reader.getFileText() ;
		}
		WordCollector collector = getWordCollector(keylen, sourceText, type);
		collector.setBookType(type);
		collector.setIgnoreCase(ignorecaseflag);
		return collector;
	}
	
	public static WordCollector getWordCollector(int keylen, String text, TYPE type) throws IOException {
		WordCollector collector = new WordCollector();
		collector.setKeylen(keylen);
		collector.setText(text);
		Book book = new Book(text);
		book.setType(type);
		collector.setBook(book);
		return collector;
	}
	
	protected WordCollector() {
	}
	
	protected WordCollector(int keylen, boolean ignorecaseflag) {
		this.keylen = keylen;
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
			int lim = numberOfTokens - keylen + 1;
			for(int i=0; i< lim; i++) {
				int index = i+keylen;
				if(index <= numberOfTokens) {		// don't run off the end of the List
					subset = sentence.subset(i, index);
					nextWord = (index == numberOfTokens) ? Sentence.TERMINAL : sentence.get(i+keylen);
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

	public int getKeylen() {
		return keylen;
	}

	public void setKeylen(int keylen) {
		this.keylen = keylen;
	}
	
	public MarkovChain<Word, Sentence> getMarkovChain() {
		return markovChain;
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
		int keylen = 2;
		boolean ignoreCase = false;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-keylen")) {
				keylen = Integer.parseInt(args[++i]);
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
				WordCollector.getWordCollector(keylen, inputFile, ignoreCase, type) :
				WordCollector.getWordCollector(keylen, text, type);
		collector.collect();
		MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
		markovChain.display();
		markovChain.displaySummaryMap();
		System.out.println(markovChain.toJSON());
	}
	
}
