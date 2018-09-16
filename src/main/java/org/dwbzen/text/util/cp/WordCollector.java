package org.dwbzen.text.util.cp;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.Configuration;
import org.dwbzen.text.util.TextFileReader;
import org.dwbzen.text.util.WordListUtils;
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
	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String[] punctuation = {".", "?", "!", ",", ":", "&", "+" };

	private int order;
	private String text = null;
	private MarkovChain<Word, Sentence> markovChain;
	private Book book = new Book();
	private Book.TYPE	bookType = TYPE.PROSE;	// default
	private static TextFileReader reader = null;
	private List<String> filterWords = new ArrayList<String>();
	private Properties configProperties = null;
	private Configuration configuration = null;
	private boolean ignoreCase = false; 
	private boolean isFilteringInputText = false;
	private boolean isFilteringPunctuation = false;
	private boolean substituteWordVariants = false;
	private Map<String, String> variantMap = null;

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
			if(type.equals(TYPE.TECHNICAL)) {
				// TODO
			}
			sourceText = reader.getFileText() ;
		}
		WordCollector collector = getWordCollector(order, sourceText, type, ignorecaseflag);
		collector.setBookType(type);
		return collector;
	}
	
	public static WordCollector getWordCollector(int order, String text, TYPE type, boolean ignorecaseflag) throws IOException {
		WordCollector collector = new WordCollector();
		collector.setIgnoreCase(ignorecaseflag);
		collector.setOrder(order);
		collector.setText(text);	// also filters unwanted words
		Book book = new Book(collector.getText());
		book.setType(type);
		collector.setBook(book);
		collector.setMarkovChain(new MarkovChain<Word, Sentence>(order));
		
		return collector;
	}
	
	protected WordCollector() {
		configure();
	}
	
	protected WordCollector(int order, boolean ignorecaseflag) {
		this.order = order;
		this.ignoreCase = ignorecaseflag;
		configure();
	}
	
	private boolean configure()  {
		boolean okay = true;
		try {
			configuration = Configuration.getInstance(CONFIG_FILENAME);
			configProperties = configuration.getProperties();
			isFilteringInputText = configProperties.getProperty("filterWordsToIgnore", "false").equalsIgnoreCase("true");
			isFilteringPunctuation  = configProperties.getProperty("filterPunctuation", "false").equalsIgnoreCase("true");
			substituteWordVariants = configProperties.getProperty("substituteWordVariants", "false").equalsIgnoreCase("true");
			if(isFilteringInputText && configProperties.containsKey("WORDS_TO_IGNORE")) {
				String ignoreThese = configProperties.getProperty("WORDS_TO_IGNORE");
				String[] wordsToIgnore = ignoreThese.split(",");
				for(String word : wordsToIgnore) { filterWords.add(word); }
			}
			if(isFilteringPunctuation) {
				for(String p : punctuation) { filterWords.add(p); }
			}
			if(substituteWordVariants) {
				String inputFileName = configProperties.getProperty("VARIANTS_FILENAME");
				WordListUtils wordListUtils = new WordListUtils(inputFileName, null);
				wordListUtils.createWordVariantMap();
				variantMap = wordListUtils.getVariantMap();
			}
		} catch(Exception e) {
			System.err.println("Configuration error: " + e.getMessage());
			e.printStackTrace(System.err);
			okay = false;
		}
		return okay;
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
    	if(sentence.size() > order-1) {	// blank line
	    	log.debug("apply: '" + sentence + "'");
	    	int numberOfTokens = sentence.size();
			int lim = numberOfTokens - order + 1;
			for(int i=0; i< lim; i++) {
				int index = i+order;
				if(index <= numberOfTokens) {		// don't run off the end of the List
					subset = sentence.subset(i, index);
					nextWord = (index == numberOfTokens) ? Sentence.TERMINAL : sentence.get(i+order);
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

	/**
	 * Sets the text string after filtering out words to ignore in filterWords
	 * and substituting word variants if so configured.
	 * @param text
	 */
	public void setText(String text) {
		if(isFilteringInputText && filterWords.size() > 0) {
			StringBuilder sb = new StringBuilder();
			BreakIterator boundry = BreakIterator.getWordInstance(Locale.US);
			boundry.setText(text);
			int start = 0;
			int end = 0;
			while((end=boundry.next()) != BreakIterator.DONE) {
				String temp = text.substring(start, end).trim();
				temp = ignoreCase ? temp.toLowerCase() : temp;
				String variant = (substituteWordVariants && variantMap.containsKey(temp)) ?
						variantMap.get(temp) : temp;
				if(variant.length() > 0 && !filterWords.contains(variant)) {
					sb.append(variant + " ");
				}
				start = end;
			}
			this.text = sb.toString();
		}
		else {
			this.text = text;
		}
	}

	public Book.TYPE getBookType() {
		return bookType;
	}

	public void setBookType(Book.TYPE bookType) {
		this.bookType = bookType;
	}
	
	public boolean isFilteringInputText() {
		return isFilteringInputText;
	}

	public void setFilteringInputText(boolean isFilteringInputText) {
		this.isFilteringInputText = isFilteringInputText;
	}

	public boolean isFilteringPunctuation() {
		return isFilteringPunctuation;
	}

	public void setFilteringPunctuation(boolean isFilteringPunctuation) {
		this.isFilteringPunctuation = isFilteringPunctuation;
	}
	
	public boolean isSubstituteWordVariants() {
		return substituteWordVariants;
	}

	public void setSubstituteWordVariants(boolean substituteWordVariants) {
		this.substituteWordVariants = substituteWordVariants;
	}

	public static void main(String...args) throws IOException {
		String inputFile = null;
		String text = null;
		String textType = "prose";
		boolean displayMarkovChain = false;
		boolean displaySummaryMap = false;
		boolean displayJson = false;
		boolean ignoreCase = false;
		String orderstring = null;
		List<Integer> orderList = new ArrayList<Integer>();
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-displayMarkovChain")) {
				displayMarkovChain = true;
			}
			else if(args[i].equalsIgnoreCase("-displaySummaryMap")) {
				displaySummaryMap = true;
			}
			else if(args[i].equalsIgnoreCase("-displayJson")) {
				displayJson = true;
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}			
			else if(args[i].equalsIgnoreCase("-order")) {	// can be a list
				orderstring = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-type")) {
				textType = args[++i];
			}
			else {
				text = args[i];
			}
		}
		Book.TYPE type = TYPE.PROSE;
		if(textType.equalsIgnoreCase("verse")) {
			type = TYPE.VERSE;
		}
		else if(textType.equalsIgnoreCase("technical")) {
			type = TYPE.TECHNICAL;
		}
		if(orderstring == null) {
			orderList.add(2);	// default order is 2 if not specified
		}
		else {
			for(String order : orderstring.split(",")) {
				orderList.add(Integer.parseInt(order));
			}
		}
		Map<Integer, MarkovChain<Word,Sentence>> markovChains = new TreeMap<Integer, MarkovChain<Word,Sentence>>();
		for(Integer order : orderList) {
			WordCollector collector = (inputFile != null) ?
					WordCollector.getWordCollector(order, inputFile, ignoreCase, type) :
					WordCollector.getWordCollector(order, text, type, ignoreCase);
			collector.collect();
			MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
			markovChains.put(order, markovChain);
		}
		if(orderList.size() == 1) {
			Integer ord = orderList.get(0);
			MarkovChain<Word, Sentence> markovChain = markovChains.get(ord);
			if(displaySummaryMap) { markovChain.displaySummaryMap(); }
			if(displayMarkovChain) { markovChain.display(); }
			if(displayJson) { System.out.println(markovChain.toJson()); }
		}
		else {
			displayMultichains(markovChains);
		}

	}

	private static void displayMultichains(Map<Integer, MarkovChain<Word, Sentence>> markovChains) {
		
	}
	
}
