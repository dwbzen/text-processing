package org.dwbzen.text.cp;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.Configuration;
import org.dwbzen.text.util.IDataSource;
import org.dwbzen.text.util.TextConfigurator;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Book.ContentType;
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
public class WordCollector implements ICollector<Sentence, MarkovChain<Word, Sentence, Book>, Book> {
	protected static final Logger log = LogManager.getLogger(WordCollector.class);
	
	private int order;
	private String text = null;
	private MarkovChain<Word, Sentence, Book> markovChain;
	private Book book = null;
	private Book.ContentType contentType = ContentType.PROSE;	// default
	private Configuration configuration = null;
	private boolean ignoreCase = false; 
	private String schemaName = "text";
	private IDataSource<String> dataSource = null;
	private boolean trace = false;
	private TextConfigurator textConfigurator = null;
	
	protected WordCollector() {
		configure();
	}
	
	protected WordCollector(int order, boolean ignorecaseflag, String schema, ContentType type, IDataSource<String> dataSource) {
		this.order = order;
		this.ignoreCase = ignorecaseflag;
		this.schemaName = schema;
		this.contentType = type;
		configure();
		if(dataSource != null) {
			this.dataSource = dataSource;
		}
	}
	
	/**
	 * Filtering out words to ignore and punctuation is configured by ContentType.<br>
	 * Substitution of word variants is also configured by ContentType.
	 * @return
	 */
	public void configure()  {
		textConfigurator  = new TextConfigurator(schemaName, contentType, ignoreCase);
		configuration = textConfigurator.configure();
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
	public MarkovChain<Word, Sentence, Book> apply(Sentence sentence) {
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
			CollectorStats<Word, Sentence, Book> collectorStats = markovChain.get(theSentence);
			collectorStats.addOccurrence(theWord);
			collectorStats.setTerminal(terminal);
		}
		else {
			CollectorStats<Word, Sentence, Book> collectorStats = new CollectorStats<>();
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
	
	public MarkovChain<Word, Sentence, Book> getMarkovChain() {
		return markovChain;
	}

	void setMarkovChain(MarkovChain<Word, Sentence, Book> markovChain) {
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

	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * Sets the text string after filtering out words to ignore in filterWords<br>
	 * and substituting word variants if so configured.<br>
	 * Also filters punctuation if configured for this type.<br>
	 * If ignoreCase is set, text is converted to lower case first.<br>
	 * Substituting word variants is configured by type. The defaults are:<br>
	 * substituteWordVariants.TECHNICAL=true<br>
	 * substituteWordVariants.PROSE=false<br>
	 * substituteWordVariants.VERSE=false</p>
	 * 
	 * The Sentence structure is preserved for TECHNICAL and PROSE content types.<br>
	 * For TECHNCIAL, each line (defined as ending in "\n") is a Sentence.<br>
	 * For PROSE, a SentenceInstance BreakIterator delimits sentences.<br>
	 * For VERSE the words are all delivered as a single sentence.<br>
	 * 
	 * TODO: make preserving sentence structure configurable.
	 * 
	 * @param text
	 * @return Book instance
	 */
	public Book setText(String sourceText) {
		String convertedText = (textConfigurator.getDataFormatter() != null) ? textConfigurator.getDataFormatter().format(sourceText) : sourceText;
		convertedText = ignoreCase ? convertedText.toLowerCase() : convertedText;
		if((textConfigurator.isFilteringInputText() && textConfigurator.getFilterWords().size() > 0) || textConfigurator.isFilteringPunctuation()) {
			List<String> filterWords = textConfigurator.getFilterWords();
			List<String> filterPunctuation = textConfigurator.getFilterPunctuation();
			
			StringBuilder sb = new StringBuilder();
			BreakIterator wordBoundry = BreakIterator.getWordInstance(Locale.US);
			BreakIterator sentenceBoundry = BreakIterator.getSentenceInstance();

			int start = 0;
			int end = 0;
			sentenceBoundry.setText(convertedText);
			wordBoundry.setText(convertedText);
			while((end=wordBoundry.next()) != BreakIterator.DONE) {
				String temp = convertedText.substring(start, end).trim();
				if(textConfigurator.isSubstituteWordVariants() && textConfigurator.getVariantMap().containsKey(temp)) {
					String variant = textConfigurator.getVariantMap().get(temp);
					if(!filterWords.contains(variant)) {
						sb.append(variant + " ");
					}
				}
				else if((textConfigurator.isFilteringPunctuation() && filterPunctuation.contains(temp)) || (textConfigurator.isFilteringInputText() && filterWords.contains(temp))  ) {
						// do nothing
				}
				else {
					sb.append(temp + " ");
				}
				start = end;
			}
			this.text = sb.toString();
		}
		else {
			this.text = convertedText;
		}
		book = new Book(text, contentType);
		return book;
	}

	public Book.ContentType getContentType() {
		return contentType;
	}

	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	public List<String> getFilterWords() {
		return textConfigurator.getFilterWords();
	}

	public Map<String, String> getVariantMap() {
		return textConfigurator.getVariantMap();
	}

	public IDataSource<String> getDataSource() {
		return dataSource;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public TextConfigurator getTextConfigurator() {
		return textConfigurator;
	}

}
