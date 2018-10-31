package org.dwbzen.text.cp;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.Configuration;
import org.dwbzen.text.util.IDataFormatter;
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
 * can be used by a Producer class.</p>
 * For a given string (inline or from a file/stream), this examines all the word collections
 * of a given length 1 to n (n <= 5), and records the #instances of each word that
 * follows that word collection. Sentences formed left to right advancing  1 Word each iteration.
 * </p>
 * Definitions:<br>
 * A Word is a non-empty String (length >0) treated as a unit.<br>
 * A Sentence consists of Word(s) delimited by white space (matches regex \s+, [ \t\n\x0B\f\r] )<br>
 * A Word consist of one or more word characters, 0 or more dashes, 0 or more single quotes. 
 * i.e. regex [a-zA-Z_0-9-']</p>
 * 
 *
 * @author don_bacon
 *
 */
public class WordCollector implements ICollector<Sentence, MarkovChain<Word, Sentence>, Book> {
	protected static final Logger log = LogManager.getLogger(WordCollector.class);
	public static final String CONFIG_FILENAME = "/config.properties";
	public final static String[] CONFIG_FILES = {"/config.properties"};
	public static final String[] punctuation = {".", "?", "!", ",", ":", "&", "+", "�", "�" };

	private int order;
	private String text = null;
	private MarkovChain<Word, Sentence> markovChain;
	private Book book = new Book();
	private Book.TYPE	bookType = TYPE.PROSE;	// default
	private List<String> filterWords = new ArrayList<String>();
	private Properties configProperties = null;
	private Configuration configuration = null;
	private boolean ignoreCase = false; 
	private boolean isFilteringInputText = false;
	private boolean isFilteringPunctuation = false;
	private boolean substituteWordVariants = false;
	private String dataFormatterClassName = null;
	private IDataFormatter<String> dataFormatter = null;
	private String schema = "text";
	private Map<String, String> variantMap = null;

	
	protected WordCollector() {
		configure();
	}
	
	protected WordCollector(int order, boolean ignorecaseflag, String schema) {
		this.order = order;
		this.ignoreCase = ignorecaseflag;
		this.schema = schema;
		configure();
	}
	
	@SuppressWarnings("unchecked")
	public boolean configure()  {
		boolean okay = true;
		try {
			configuration = Configuration.getInstance(CONFIG_FILES);
			configProperties = configuration.getProperties();
			isFilteringInputText = configProperties.getProperty("filterWordsToIgnore", "false").equalsIgnoreCase("true");
			isFilteringPunctuation  = configProperties.getProperty("filterPunctuation", "false").equalsIgnoreCase("true");
			substituteWordVariants = configProperties.getProperty("substituteWordVariants", "false").equalsIgnoreCase("true");
			dataFormatterClassName = configProperties.getProperty("dataFormatterClass." + schema);
			if(dataFormatterClassName != null && !schema.equalsIgnoreCase("none")) {
				try {
					Class<IDataFormatter<String>> formatterClass = (Class<IDataFormatter<String>>)Class.forName(dataFormatterClassName);
					this.dataFormatter = formatterClass.getDeclaredConstructor().newInstance();
				}
				catch(Exception e) {
	    			String errorMessage = "Could not create DataFormatter " + dataFormatterClassName;
	    			log.error(errorMessage);
	    			log.error(e.toString() );
				}
			}
			if(isFilteringInputText && configProperties.containsKey("WORDS_TO_IGNORE")) {
				String ignoreThese = configProperties.getProperty("WORDS_TO_IGNORE");
				String[] wordsToIgnore = ignoreThese.split(",");
				for(String word : wordsToIgnore) { filterWords.add(word); }
			}
			if(isFilteringPunctuation) {
				for(String p : punctuation) { filterWords.add(p); }
			}
			if(substituteWordVariants) {
				String inputFileName = configProperties.getProperty("VARIANTS_MAP_FILENAME");
				variantMap = WordListUtils.getVariantMap(inputFileName);
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

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * Sets the text string after filtering out words to ignore in filterWords
	 * and substituting word variants if so configured.
	 * If ignoreCase is set, text is converted to lower case.
	 * @param text
	 */
	public void setText(String text) {
		if(dataFormatter != null) {
			text = dataFormatter.format(text);
		}
		String convertedText = ignoreCase ? text.toLowerCase() : text;
		if(isFilteringInputText && filterWords.size() > 0) {
			StringBuilder sb = new StringBuilder();
			BreakIterator boundry = BreakIterator.getWordInstance(Locale.US);
			boundry.setText(convertedText);
			int start = 0;
			int end = 0;
			while((end=boundry.next()) != BreakIterator.DONE) {

				String temp = convertedText.substring(start, end).trim();
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
			this.text = convertedText;
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

}
