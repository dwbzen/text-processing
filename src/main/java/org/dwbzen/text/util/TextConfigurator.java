package org.dwbzen.text.util;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.element.Book;
import org.dwbzen.text.element.Sentence;
import org.dwbzen.text.element.Book.ContentType;

/**
 * Encapsulates config.properties.</br>
 * Formats source text using configured values. This includes case sensitivity,
 * sentence structure, word substitution and filtering.
 * @author DBacon
 *
 */
public class TextConfigurator {
	protected static final Logger log = LogManager.getLogger(TextConfigurator.class);
	private Configuration configuration = null;
	private Properties configProperties = null;
	private String schemaName = null;
	private boolean ignoreCase = false; 
	private boolean isFilteringInputText = false;
	private boolean isFilteringPunctuation = false;
	private boolean substituteWordVariants = false;
	private boolean ignoreWordsContainingNumbers = false;
	private boolean ignoreWordsInUppercase = false;
	private boolean ignoreInternetAndFileAddresses = false;
	private String dataFormatterClassName = null;
	private IDataFormatter<String> dataFormatter = null;
	private ContentType contentType = null;
	private Map<String, String> variantMap = null;
	
	private Book book = null;	// formatted text as a Book instance
	
	
	public static Map<Book.ContentType, String> contentTypes = new HashMap<>();
	public static final String[] punctuation = {",", "“", "”" };		// additions to configured PUNCTUATION
	public final static String TRUE = "true";
	public final static String FALSE = "false";
	public static final Pattern numberPattern = Pattern.compile("^[0-9]+\\.?[0-9]*$");
	private static List<String> filterWords = new ArrayList<String>();
	private static List<String> filterPunctuation = new ArrayList<String>();
	
	static {
		contentTypes.put(ContentType.PROSE, "PROSE");
		contentTypes.put(ContentType.VERSE, "VERSE");
		contentTypes.put(ContentType.TECHNICAL, "TECHNICAL");
		contentTypes.put(ContentType.OTHER, "OTHER");
		contentTypes.put(ContentType.ANY, "ANY");
	}
	
	public TextConfigurator() {
		this("text", ContentType.ANY, false);
	}
	
	public TextConfigurator(String schemaName, ContentType contentType, boolean ignoreCase) {
		this.schemaName = schemaName;
		this.contentType = contentType;
		this.ignoreCase = ignoreCase;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Filtering out words to ignore and punctuation is configured by ContentType.<br>
	 * Substitution of word variants is also configured by ContentType.
	 * @return
	 */
	public Configuration configure()  {
		try {
			configuration = Util.getConfiguration();
			configProperties = configuration.getProperties();
			isFilteringInputText =  getBooleanProperty("filterWordsToIgnore");
			isFilteringPunctuation  = getBooleanProperty("filterPunctuation");
			substituteWordVariants = getBooleanProperty("substituteWordVariants");
			ignoreWordsContainingNumbers = getBooleanProperty("ignoreWordsContainingNumbers");
			ignoreWordsInUppercase = getBooleanProperty("ignoreWordsInUppercase");
			ignoreInternetAndFileAddresses = getBooleanProperty("ignoreInternetAndFileAddresses");
			// for example: dataFormatterClass.ticket=org.dwbzen.text.util.TicketDataFormatter
			// schema name is "ticket"
			dataFormatterClassName = configProperties.getProperty("dataFormatterClass." + schemaName);
			if(dataFormatterClassName != null && !dataFormatterClassName.equalsIgnoreCase("none") && !schemaName.equalsIgnoreCase("none")) {
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
			for(String word : configProperties.getProperty("WORDS_TO_IGNORE").split(",")) { filterWords.add(word); 	}
			for(String p : punctuation) { filterPunctuation.add(p); }
			for(String word : configProperties.getProperty("PUNCTUATION").split(",")) {	filterPunctuation.add(word); }
			
			variantMap = WordListUtils.getVariantMap(configProperties.getProperty("VARIANTS_MAP_FILENAME"));
			
		} catch(Exception e) {
			System.err.println("Configuration error: " + e.getMessage());
			e.printStackTrace(System.err);
		}
		return configuration;
	}
	
	/**
	 * Get the boolean property value associated with a configuration base name.
	 * The config value is the base name + "." + content type.
	 * If there is no configured value for that name+content type, it looks for name+"ANY"
	 * and if that doesn't exit it returns false;
	 * @param baseName property base name without the "." separator
	 * @return configured boolean value
	 */
	public boolean getBooleanProperty(String baseName) {
		String propertyName = baseName + "." + contentTypes.get(contentType);
		boolean value;
		if(configProperties.containsKey(propertyName)) {
			value = configProperties.getProperty(propertyName).equalsIgnoreCase(TRUE);
		}
		else {
			value = configProperties.getProperty(baseName + "." + contentTypes.get(ContentType.ANY), FALSE).equalsIgnoreCase(TRUE);
		}
		return value;
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
	 * <b>NOTE - invoke configure() first.</b>
	 * <br>
	 * @param text
	 * @return Book instance
	 */
	public Book formatText(String sourceText) {
		IDataFormatter<String> dataFormatter = getDataFormatter();
		book = new Book(this.schemaName);
		if(dataFormatter != null) {
			// format each line of sourceText
			// create a Sentence for each and add to the Book
			int start = 0;
			int end;
			String line = null;
			while((end = sourceText.indexOf('\n', start)) > 0) {
				line = sourceText.substring(start, end);
				String formattedText = dataFormatter.format(line);
				String convertedText = convertTextLine(formattedText);
				Sentence sentence = new Sentence(convertedText, formattedText);
				sentence.setId(dataFormatter.getId());
				book.add(sentence);
				start = end + 1;
			}
			if(start < sourceText.length()) {
				// check if there's a trailing line AND it has content
				line = sourceText.substring(start);
				if(line.length() > 2 ) {
					Sentence sentence = dataFormatter.formatAsSentence(line);
					book.add(sentence);
				}
			}
		}
		else {
			book = new Book(sourceText);
		}
		return book;
	}
	
	/**
	 * Filter punctuation. Convert to LC if ignoring case. Substitute word variants if configured.</br>
	 * Filter numbers, internet addresses, words in UPPER CASE if configured.
	 */
	public String convertTextLine(String sourceText) {
		String convertedText = isIgnoreCase() ? sourceText.toLowerCase() : sourceText;
		boolean isFiltering = isFilteringPunctuation() || isIgnoreWordsContainingNumbers() || isIgnoreWordsInUppercase() || isIgnoreInternetAndFileAddresses();
		if((isFilteringInputText() && getFilterWords().size() > 0) || isFiltering) {
			
			StringBuilder sb = new StringBuilder();
			BreakIterator wordBoundry = BreakIterator.getWordInstance(Locale.US);
			BreakIterator sentenceBoundry = BreakIterator.getSentenceInstance();

			int start = 0;
			int end = 0;
			sentenceBoundry.setText(convertedText);
			wordBoundry.setText(convertedText);
			while((end=wordBoundry.next()) != BreakIterator.DONE) {
				boolean saveWord = true;	// if true, save temp
				String temp = convertedText.substring(start, end).trim();
				if(temp.length() > 0) {
					if(isSubstituteWordVariants() && getVariantMap().containsKey(temp)) {
						String variant = getVariantMap().get(temp);
						saveWord = false;
						if(!filterWords.contains(variant)) {
							sb.append(variant + " ");
						}
					}
					else if((isFilteringPunctuation() && filterPunctuation.contains(temp)) || (isFilteringInputText() && filterWords.contains(temp))  ) {
							// skip the punctuation
						saveWord = false;
					}
					else if(isIgnoreWordsContainingNumbers()) {
						Matcher m = numberPattern.matcher(temp);
						saveWord = !m.matches();
					}
					if(saveWord) {
						sb.append(temp + " ");
					}
				}
				start = end;
			}
			convertedText = sb.toString();
		}
		return convertedText;
	}

	public Book getBook() {
		return book;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
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

	public boolean isIgnoreWordsContainingNumbers() {
		return ignoreWordsContainingNumbers;
	}

	public void setIgnoreWordsContainingNumbers(boolean ignoreWordsContainingNumbers) {
		this.ignoreWordsContainingNumbers = ignoreWordsContainingNumbers;
	}

	public boolean isIgnoreWordsInUppercase() {
		return ignoreWordsInUppercase;
	}

	public void setIgnoreWordsInUppercase(boolean ignoreWordsInUppercase) {
		this.ignoreWordsInUppercase = ignoreWordsInUppercase;
	}

	public boolean isIgnoreInternetAndFileAddresses() {
		return ignoreInternetAndFileAddresses;
	}

	public void setIgnoreInternetAndFileAddresses(boolean ignoreInternetAndFileAddresses) {
		this.ignoreInternetAndFileAddresses = ignoreInternetAndFileAddresses;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public boolean isSubstituteWordVariants() {
		return substituteWordVariants;
	}

	public String getDataFormatterClassName() {
		return dataFormatterClassName;
	}

	public IDataFormatter<String> getDataFormatter() {
		return dataFormatter;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public List<String> getFilterWords() {
		return filterWords;
	}

	public List<String> getFilterPunctuation() {
		return filterPunctuation;
	}

	public Map<String, String> getVariantMap() {
		return variantMap;
	}
	
}
