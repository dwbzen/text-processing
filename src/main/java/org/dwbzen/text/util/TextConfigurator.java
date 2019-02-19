package org.dwbzen.text.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Book.ContentType;

/**
 * Encapsulates config.properties
 * @author DBacon
 *
 */
public class TextConfigurator {
	protected static final Logger log = LogManager.getLogger(TextConfigurator.class);
	private Configuration configuration = null;
	private Properties configProperties = null;
	private String schema = null;
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
	private List<String> filterWords = new ArrayList<String>();
	private List<String> filterPunctuation = new ArrayList<String>();
	private Map<String, String> variantMap = null;
	
	public static Map<Book.ContentType, String> contentTypes = new HashMap<>();
	public static final String[] punctuation = {",", "“", "”" };		// additions to configured PUNCTUATION
	public final static String TRUE = "true";
	public final static String FALSE = "false";
	
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
		schema = schemaName;
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
			
			dataFormatterClassName = configProperties.getProperty("dataFormatterClass." + schema);
			if(dataFormatterClassName != null && !dataFormatterClassName.equalsIgnoreCase("none") && !schema.equalsIgnoreCase("none")) {
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

	public String getSchema() {
		return schema;
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
