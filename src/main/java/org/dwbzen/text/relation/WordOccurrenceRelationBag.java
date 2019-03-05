package org.dwbzen.text.relation;


import java.util.Map;

import org.dwbzen.text.util.Configuration;
import org.dwbzen.text.util.IDataSource;
import org.dwbzen.text.util.TextConfigurator;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;
import org.dwbzen.text.util.model.Book.ContentType;

import mathlib.SourceOccurrenceProbability;
import mathlib.Tupple;
import mathlib.cp.OutputStyle;
import mathlib.relation.OccurrenceRelationBag;

public class WordOccurrenceRelationBag extends OccurrenceRelationBag<Word, Sentence, Book> {
	private static final long serialVersionUID = 1L;
	static OutputStyle outputStyle = OutputStyle.TEXT;
	static boolean trace = true;
	
	private Book.ContentType contentType = ContentType.TECHNICAL;	// default
	private Configuration configuration = null;
	private boolean ignoreCase = false; 
	private String schemaName = "text";
	private IDataSource<String> dataSource = null;
	private TextConfigurator textConfigurator = null;
	private String text = null;
	private Book book = null;
	
	public WordOccurrenceRelationBag(int degree, boolean ignorecaseflag, String schema, ContentType type, IDataSource<String> dataSource) {
		super(degree);
		setMetricFunction(new TuppleWordDistanceMetric());
		this.ignoreCase = ignorecaseflag;
		this.schemaName = schema;
		this.contentType = type;
		if(dataSource != null) {
			this.dataSource = dataSource;
		}
		configure();
	}
	
	/**
	 * Copy constructor (shallow)
	 * @param WordOccurrenceRelationBag otherBag
	 * @param Map<Tupple<Wortd>, SourceOccurrenceProbability<Word, Sentence>> optionalMap
	 */
	public WordOccurrenceRelationBag(WordOccurrenceRelationBag otherBag,  Map<Tupple<Word>, SourceOccurrenceProbability<Word,Sentence>> optionalMap) {
		super(otherBag.getDegree());
		setConfiguration(otherBag.getConfiguration());
		setTextConfigurator(otherBag.getTextConfigurator());
		setSupressSourceOutput(otherBag.isSupressSourceOutput());
		setTotalOccurrences(otherBag.getTotalOccurrences());
		setMetricFunction(new TuppleWordDistanceMetric());
		if(optionalMap != null) {
			setSourceOccurrenceProbabilityMap(optionalMap);
		}
		recomputeProbabilities();
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


	public String getText() {
		return text;
	}

	/**
	 * Uses current TextConfigurator to format raw text.
	 * @param text raw String to be formatted (or not, depending on the configuration)
	 * @return Book instance
	 */
	public Book setText(String sourceText) {
		this.text = sourceText;
		book = textConfigurator.formatText(sourceText);
		Sentence sentence = null;
		while((sentence = book.get()) != null) {
			WordOccurranceRelation wor = new WordOccurranceRelation(sentence, getDegree(), ignoreCase);
			this.addOccurrenceRelation(wor);
		}
		return book;
	}
	
	public Book.ContentType getContentType() {
		return contentType;
	}

	protected void setContentType(Book.ContentType contentType) {
		this.contentType = contentType;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	protected void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public TextConfigurator getTextConfigurator() {
		return textConfigurator;
	}

	protected void setTextConfigurator(TextConfigurator textConfigurator) {
		this.textConfigurator = textConfigurator;
	}
	
	public IDataSource<String> getDataSource() {
		return dataSource;
	}

	public Book getBook() {
		return book;
	}

	protected void setBook(Book book) {
		this.book = book;
	}

}
