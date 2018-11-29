package org.dwbzen.text.util.model;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.TextFileReader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;
import mathlib.util.INameable;

/**
 * A Book is a Supplier of Sentences.
 * Optional attributes and structure encapsulates book components:
 * Title	(TITLE)
 * Author	(AUTHOR)
 * Chapter	(CHAPTER)
 * 
 * All the text is treated equally.
 * TODO The get() becomes a simple iterator over the Sentence List.
 * 
 * @author don_bacon
 *
 */
public class Book implements Supplier<Sentence>, IJson, INameable {
	private static final long serialVersionUID = -6403206930194239270L;
	protected static final Logger log = LogManager.getLogger(Book.class);

	
	/**
	 * The ContentType determines how Sentences are processed.
	 * @author don_bacon
	 *
	 */
	public static enum ContentType  {PROSE, VERSE, TECHNICAL };
	public static enum ContentFormat {PLAIN_TEXT, JSON, XML };
	
	
	@JsonProperty("title")	private String name = INameable.DEFAULT_NAME;				// Synonymous with Title
	@JsonProperty("author")	private String author = INameable.DEFAULT_NAME;
	@JsonProperty			private List<Sentence> sentences = new ArrayList<>();		// ALL the sentences
	@JsonProperty			private Map<String, Chapter> chapters = new HashMap<>();	// optional chapters
	@JsonProperty			private String sourceText = "";
	@JsonProperty			private Properties properties = new Properties();			// optional properties can be whatever
	@JsonProperty("contentType")	private ContentType type = ContentType.PROSE;
	@JsonIgnore 	private int currentIndex = -1;
	
	public Book() {
		name = INameable.DEFAULT_NAME;
	}
	
	public Book(String sourceText) {
		this(sourceText, ContentType.PROSE);
	}

	public Book(String text, ContentType type) {
		this.type = type;
		setSource(text);
	}
	
	/**
	 * Supplies a Sentence one at a time.
	 * @return Sentence, null when it runs out.
	 */
	@Override
	public Sentence get() {
		currentIndex++;
		return  (currentIndex > sentences.size()-1) ? null : sentences.get(currentIndex);
	}
	
	public String toString() {
		return getSourceText();
	}
	
	public String getSourceText() {
		return sourceText;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * 
	 * @param sourceText
	 */
	public void setSource(String text) {
		sourceText = text;
		createSentences();
	}
	
	/*
	 * Parses the sourceText into Sentences according to ContentType
	 */
	protected void createSentences() {
		Sentence sentence = null;
		int start = 0;
		int end = 0;
		BreakIterator boundary = (type.equals(ContentType.VERSE)) ? 
					TextBreakIterator.getVerseInstance() :
					TextBreakIterator.getSentenceInstance();
		boundary.setText(sourceText);
		start = boundary.first();
		
		while((end = boundary.next()) != BreakIterator.DONE) {
			log.debug("start,end: " + start + ", " + end);
			sentence = new Sentence(sourceText.substring(start, end), true);
			log.trace(sentence);
			sentences.add(sentence);
			start = end;
		}
		return;
	}
	
	
	public ContentType getType() {
		return type;
	}

	public void setType(ContentType type) {
		this.type = type;
	}
	
	public static void main(String... args) throws IOException {
		String inputFile = null;
		String textType = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-type")) {
				textType = args[++i];
			}
		}
		Book.ContentType type = (textType != null && textType.equalsIgnoreCase("verse")) ? ContentType.VERSE : ContentType.PROSE;
		String eol = (type.equals(ContentType.VERSE)) ? "\n" : "";
		TextFileReader reader = TextFileReader.getInstance(inputFile, eol);
		String fileText = reader.getFileText();
		
		Book book = new Book(fileText);
		book.setType(type);
		Word word = null;
		int n = 0;
		Sentence sentence = null;
		while((sentence = book.get()) != null) {
			System.out.println((++n) + ". " + sentence.toString());
			System.out.println("  #words: " + sentence.size());
			while((word = sentence.get()) != null) {
				System.out.println("  " + word.toString());
			}
		}
	}

	public int size() {
		return sentences.size();
	}

	public boolean isEmpty() {
		return sentences.isEmpty();
	}


	public boolean add(Sentence e) {
		return sentences.add(e);
	}


	public void clear() {
		sentences.clear();
	}
	
	public String getTitle() {
		return getName();
	}
	public void setTitle(String title) {
		setName(title);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name==null ? "Untitled" : name;
	}

}
