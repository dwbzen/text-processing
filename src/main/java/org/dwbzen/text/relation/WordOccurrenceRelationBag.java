package org.dwbzen.text.relation;


import java.util.Map;
import java.util.function.Function;

import org.dwbzen.text.util.Configuration;
import org.dwbzen.text.util.IDataSource;
import org.dwbzen.text.util.TextConfigurator;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.text.util.model.Book.ContentType;

import mathlib.SourceOccurrenceProbability;
import mathlib.Tupple;
import mathlib.cp.OutputStyle;
import mathlib.relation.OccurrenceRelationBag;

public class WordOccurrenceRelationBag extends OccurrenceRelationBag<Word, Sentence, Book> {
	private static final long serialVersionUID = 1L;
	static OutputStyle outputStyle = OutputStyle.TEXT;
	static boolean trace = true;
	
	@JsonProperty	private Book.ContentType contentType = ContentType.TECHNICAL;	// default
	@JsonIgnore		private Configuration configuration = null;
	@JsonIgnore		private boolean ignoreCase = false; 
	@JsonProperty	private String schemaName = "text";
	@JsonIgnore		private IDataSource<String> dataSource = null;
	@JsonIgnore		private TextConfigurator textConfigurator = null;
	@JsonIgnore		private String text = null;
	@JsonIgnore		private Book book = null;
	@JsonIgnore		private SentenceIdExtractor sentenceIdExtractor = new SentenceIdExtractor();
	@JsonIgnore		private boolean supressIdOutput = false;
	
	public WordOccurrenceRelationBag(int degree, boolean ignorecaseflag, String schema, ContentType type, IDataSource<String> dataSource) {
		super(degree);
		setMetricFunction(new TuppleWordDistanceMetric());
		ignoreCase = ignorecaseflag;
		schemaName = schema;
		contentType = type;
		if(dataSource != null) {
			this.dataSource = dataSource;
		}
		configure();
		setIdExtractorFunction(new SentenceIdExtractor());
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
		setIdExtractorFunction(new SentenceIdExtractor());
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
			addOccurrenceRelation(wor);
		}
		return book;
	}
	
	@Override
	public String toJson(boolean pretty) {
		if(!pretty) {
			return super.toJson();
		}
		StringBuilder sb = new StringBuilder("{\n  \"totalOccurrences\" : " + getTotalOccurrences() + "\n");
		sb.append("  \"degree\" : " + this.getDegree() + "\n");
		sb.append("  \"sourceOccurrenceProbabilityMap\" : {\n");
		for(SourceOccurrenceProbability<Word, Sentence> sop : 	sourceOccurrenceProbabilityMap.values()) {
			sb.append("    \"" + sop.getKey().toString(true) + "\" : {\n");
			sb.append(sop.getOccurrenceProbability().toJson(indent));
			String avgDistance = indent + "\"averageDistance\" : " + sop.getAverageDistanceText();
			sb.append(",\n" + avgDistance );
			if(!isSupressSourceOutput()) {
				sb.append(",\n");
				sb.append(indent);				
				sb.append("\"sources\" : [");
				int nsources = sop.getSources().size();
				int i = 0;
				for(Sentence sentence : sop.getSources()) {
					sb.append("[" + sentence.toString(true) + "]");	// show the source as formatted but not converted
					if(++i < nsources) {sb.append(","); }
				}
				sb.append("],\n");
			}
			else {
				sb.append(",\n");
			}
			if(!isSupressIdOutput()) {
				//sb.append(",\n");
				sb.append(indent);				
				sb.append("\"ids\" : [");
				int nids = sop.getIds().size();
				int i = 0;
				for(String id : sop.getIds()) {
					sb.append( id );
					if(++i < nids) {sb.append(", "); }
				}
				sb.append("]\n");
				sb.append("    },\n");			}
			else {
				sb.append("]\n");
				sb.append("    },\n");
			}
		}
		sb.deleteCharAt(sb.length()-2);
		sb.append("}\n");
		return sb.toString();
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

	public boolean isSupressIdOutput() {
		return supressIdOutput;
	}

	public void setSupressIdOutput(boolean supressIdOutput) {
		this.supressIdOutput = supressIdOutput;
	}

}

class SentenceIdExtractor implements Function<Sentence, String> {

	@Override
	public String apply(Sentence sentence) {
		return sentence != null && sentence.getId() != null ? sentence.getId() : "";
	}
}
