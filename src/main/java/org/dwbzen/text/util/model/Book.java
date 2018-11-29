package org.dwbzen.text.util.model;

import java.io.IOException;
import java.io.Serializable;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.TextFileReader;

import mathlib.util.INameable;

/**
 * A Book is a Supplier of Sentences.
 * Optional mark-up in the text can be used to distinguish book properties:
 * Title	(TITLE)
 * Author	(AUTHOR)
 * Part		(PART)
 * Chapter	(CHAPTER)
 * Notation	(NOTATION)
 * Markup is <PROPERTY>:.... :
 * 
 * Everything else is book text. If no mark-up is used, all the text is treated equally.
 * TODO: Change the interface and remove Collection<Word>. Keep sentences in a private List<Sentence>
 * The get() then becomes a simple iterator over the Sentence List.
 * 
 * @author don_bacon
 *
 */
public class Book implements Supplier<Sentence>, Serializable, INameable {
	private static final long serialVersionUID = -6403206930194239270L;
	protected static final Logger log = LogManager.getLogger(Book.class);

	public final static String COLON = ":";
	public static final String TITLE = "Title";
	public static final String AUTHOR = "Author";
	public static final String PART = "Part";			// array of Part names
	public static final String CHAPTER = "Chapter";		// array of Chapter names
	public static final String NOTATION = "Notation";	// array of Notation text
	public static final String LANGUAGE = "Language";
	public static final String CLASS = "Class";
	
	/**
	 * The Book Type determines how Sentences are processed.
	 * @author don_bacon
	 *
	 */
	public static enum ContentType  {PROSE, VERSE, TECHNICAL };
	public static enum ContentFormat {PLAIN_TEXT, JSON, XML };
	
	/**
	 * Sections apply to JSON-formatted text.
	 * 
	 */
	public static List<String> sections = new ArrayList<String>();
	static {
		sections.add(TITLE);
		sections.add(AUTHOR);
		sections.add(PART);
		sections.add(CHAPTER);
		sections.add(NOTATION);
		sections.add(LANGUAGE);
		sections.add(CLASS);
	}
	
	private String name = null;
	private List<Sentence> sentences = new ArrayList<>();
	private String source = "";
	private BreakIterator boundary = null;
	private Properties properties = new Properties();
	private int start = 0;
	private int end = 0;
	private String workingText = null;
	private ContentType type = ContentType.PROSE;
	
	public Book() {
		name = INameable.DEFAULT_NAME;
	}
	
	public Book(String sourceText) {
		this(sourceText, ContentType.PROSE);
	}

	public Book(String sourceText, ContentType type) {
		this.type = type;
		setSource(sourceText);
	}
	
	/**
	 * Supplies a Sentence one at a time.
	 * @return Sentence, null when it runs out.
	 */
	@Override
	public Sentence get() {
		Sentence sentence = null;
		if(boundary == null) {
			boundary = (type.equals(ContentType.VERSE)) ? 
					TextBreakIterator.getVerseInstance() :
					TextBreakIterator.getSentenceInstance();
			boundary.setText(source);
			log.debug(boundary.getClass().getName());
			start = boundary.first();
		}
		end = boundary.next();	// end will be set to the start of the next sentence
		log.debug("start,end: " + start + ", " + end);
		if(end == BreakIterator.DONE) {
			boundary = null;	// reset for next time through
		}
		else {
			sentence = new Sentence(source.substring(start, end), true);
			log.trace(sentence);
			start = end;
		}
		return sentence;
	}
	
	public String toString() {
		return getSource();
	}
	
	public String getSource() {
		return source;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * TODO the : metacharacter for title, author etc. are also word boundaries and get stripped. So need to fix that.
	 * 
	 * @param sourceText
	 */
	public void setSource(String sourceText) {
		workingText = sourceText;
		String title = null;
		for(String section : sections) {
			int startInd = workingText.indexOf(section + COLON);
			if(startInd >= 0) {
				int e = workingText.indexOf(COLON, startInd + section.length()+1);
				title = workingText.substring(startInd + section.length() + 1, e);
				properties.setProperty(section, title);
				if(startInd > 0) {
					workingText = workingText.substring(0, startInd-1).concat(workingText.substring(e+1));
				}
				else {
					workingText = workingText.substring(e+1);
				}
			}
		}
		name = (title == null) ? "Untitled" : title;
		source = workingText;
		boundary = null;
	}
	
	
	public ContentType getType() {
		return type;
	}

	public void setType(ContentType type) {
		this.type = type;
	}

	public void reset() {
		boundary = null;
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
		Sentence sentence = null;
		Word word = null;
		int n = 0;
		while((sentence = book.get())!= null) {
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

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name==null ? "Untitled" : name;
	}

}
