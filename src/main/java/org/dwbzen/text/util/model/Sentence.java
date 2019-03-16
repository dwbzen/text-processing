package org.dwbzen.text.util.model;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.cp.ICollectable;
import org.dwbzen.common.util.IJson;
import org.dwbzen.common.util.INameable;

/**
 * A Sentence is a collection of Words separated by white space.
 * Each Word will include any punctuation and space between words.
 * For example, the source string:  “Of course, you’ll need training,” he tells me in a very serious voice.
 * has 20 Words: “, Of, <space>, course, <comma>, etc. with the ignoreWhiteSpace set to true.
 * Note that it treats the single left quote (’) same as double left & right quotes (“”) and separates contractions 
 * like "you’ll" or "1’st" into 3 Words if a single left or right quote is used.
 * Use a normal straight single quote (not a left or right) to keep contracted words together.
 * The above example: “Of course, you'll need training,” he tells me in a very serious voice.
 * has 18 words and treats the left and right double quotes as separate words.
 * 
 * To ignore all punctuation set ignorePunctuation to true. Punctuation (in English) include
 * period (.) comma (,) bang (!) question mark (?), normal double and single quote ( "  ' )
 * For contractions, this also has the effect of eliminating the enclosing word.
 * White space can be ignored (not stored as words) by setting the ignoreWhiteSpace to true.
 * Punctuation is defined by the Pattern "\\p{Punct}"
 * White space is defined by the Pattern "\\s+"
 * 
 * For correct behavior use left and right single (‘’) and double quote characters (“”)
 * for quotations and normal straight quote for contractions.
 * 
 * For certain kinds of text, such as poetry or verse, it is appropriate to
 * treat the end of line characters (\r, \n) as a terminator.
 * 
 * A Sentence has an optional unique id that is provided in the constructor.
 * If null, then one is assigned to be the hash of the source (which cannot be null)
 * 
 * @see java.text.BreakIterator
 * @see java.util.regex.Pattern
 * @author don_bacon
 *
 */
public class Sentence implements Comparable<Sentence>, List<Word>, Supplier<Word>, ICollectable<Word>, INameable, IJson {

	private static final long serialVersionUID = 5982270956795205537L;
	private static Pattern WHITE_SPACE = Pattern.compile("\\s+");	// white space
	private static Pattern PUNCTUATION = Pattern.compile("\\p{Punct}", Pattern.UNICODE_CHARACTER_CLASS);
	static Word DELIM = new Word(".");
	
	@JsonIgnore			private ArrayList<Word> words = new ArrayList<Word>();
	@JsonIgnore			private String source = null;
	@JsonProperty("text")	private String rawText = null;		// original, unaltered text
	@JsonIgnore			private BreakIterator boundary = null;
	@JsonIgnore			private int index = -1;
	@JsonIgnore			private boolean ignoreWhiteSpace = true;
	@JsonIgnore			private boolean ignorePunctuation = false;
	/*
	 * If not provided, defaults to "hash:<source hash code>" or "Unnamed".
	 * name is optional and does not need to be unique
	 */
	@JsonProperty		private String name = null;
	
	/*
	 * Optional unique key, usually a property value depending on the context and source data.
	 * If not provided, defaults to <source hash code>
	 */
	@JsonProperty		private String id = null;
	
	/** Used to represent a NULL key in a Map - since it can't really be a null */
	public static Word NULL_VALUE = new Word(Word.NULL_VALUE);
	
	/** Used to represent a Terminal state in a Markov Chain */
	public static Word TERMINAL = new Word(Word.TERMINAL);		// Character('¶')
	
	static List<Word> DELIMITERS = new ArrayList<>();	// Sentence delimiters: . ? !
	static {
		DELIMITERS.add(DELIM);				// a period delimits sentences
		DELIMITERS.add(new Word("?"));		// so does a question mark
		DELIMITERS.add(new Word("!"));		// and a bang
		DELIMITERS.add(new Word(".”"));		// and an end of a quote with period
		DELIMITERS.add(new Word("!”"));		// and an end of a quote with bang
		DELIMITERS.add(new Word("?”"));		// and an end of a quote with question mark
	}
	
	public Sentence() {
		this(null, true, null, null, INameable.DEFAULT_NAME, false, null);
	}
	
	public Sentence(String string, boolean skipws, Sentence otherSentence, Word word, String aname, boolean skipPunctuation, String anId) {
		super();
		assert(string != null);
		ignoreWhiteSpace = skipws;
		ignorePunctuation = skipPunctuation;
		setSource(string);
		this.rawText = source;
		if(otherSentence != null) {
			for(Word w: otherSentence) {
				add(w);
			}
			if(word != null) {
				add(word);
			}
			setSource();
		}
		this.name = (aname != null && aname.length()>0 && aname.length()<=20) ? aname : createName(aname);
		this.id = (anId == null) ? createId() : anId;
	}
	
	private String createName(String aname) {
		String createdName = null;
		if(aname != null && aname.length() > 20) {
			createdName = aname.substring(0, 19) + "...";
		}
		else {
			createdName = (source != null) ? "hash:"+source.hashCode() : "Unnamed";
		}
		return createdName;
	}
	
	private String createId() {
		String anid = (source != null) ? String.valueOf(source.hashCode()) : String.valueOf(serialVersionUID);
		return anid;
	}

	public Sentence(String text) {
		this(text, true, null, null, null, false, null);
	}
	
	public Sentence(String text, String rawText) {
		this(text, true, null, null, null, false, null);
		this.rawText = rawText;
	}

	public Sentence(String string, boolean skipws) {
		this(string, skipws, null, null, null, false, null);
	}
	
	public Sentence(String string, boolean skipws, boolean skipPunctuation) {
		this(string, skipws, null, null, null, skipPunctuation, null);
	}
	
	public Sentence(Sentence otherSentence) {
		this(otherSentence.rawText, true, otherSentence, null, null, false, otherSentence.getId());
	}
	
	public Sentence(Sentence otherSentence, Word word) {
		this(otherSentence.rawText, true, otherSentence, word, null, false,null);
		id = createId();
	}

	private void breakIntoWords(String stringToExamine) {
		Predicate<String> whiteSpaceTester = WHITE_SPACE.asPredicate();
		Predicate<String> punctuationTester = PUNCTUATION.asPredicate();
		boundary = BreakIterator.getWordInstance();
        boundary.setText(stringToExamine);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
        	String s = source.substring(start, end);
        	if(ignoreWhiteSpace && whiteSpaceTester.test(s)) {
       			continue;
        	}
        	if(ignorePunctuation && punctuationTester.test(s)) {
       			continue;
        	}
        	Word word = new Word(s);
        	this.add(word);
        }
	}
	
	/**
	 * Returns a new Sentence of the portion of this Sentence
	 * between the specified fromIndex, inclusive, and toIndex, exclusive.
	 * So it works like String.substring(start, end)
	 * @param startIndex beginning index to get, inclusive
	 * @param endIndex  end index to get, exclusive
	 * @return Sentence
	 */
	public Sentence subset(int fromIndex, int toIndex) {
		if(fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException("Check arguments: " + fromIndex + "," + toIndex);
		}
		Sentence sub = new Sentence();
		for(int i = fromIndex; i<toIndex; i++) {
			sub.add(get(i));
		}
		sub.setSource();
		return sub;
	}
	
	/**
	 * Returns a new Sentence of the portion of this Sentence
	 * from the specified fromIndex, inclusive to the end of the String
	 * @param startIndex beginning index to get, inclusive
	 * @return Sentence
	 */
	public Sentence subset(int startIndex) {
		return subset(startIndex, this.size());
	}

	public String toString() {
		String sentenceString = this.stream()
				.map(w -> w.toString())
				.collect(Collectors.joining(" "));
		return sentenceString;
	}
	
	public String toString(boolean useRawText) {
		return useRawText ? rawText : toString();
	}
	
	/**
	 * Returns a new Sentence where the Words are all in lower case
	 * This is not modified.
	 * @return Sentence in lower case
	 */
	public Sentence toLowerCase() {
		Sentence lc = new Sentence(this.toString().toLowerCase());
		lc.setId(this.id);
		return lc;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String sourceString) {
		if(sourceString != null) {
			source = sourceString;
			breakIntoWords(sourceString);	
		}
	}
	
	/**
	 * Sets the source text from the internal List<Word>
	 */
	public void setSource() {
		Iterator<Word> iw = this.iterator();
		StringBuffer sb = new StringBuffer();
		while(iw.hasNext()) {
			Word sw = iw.next();
			sb.append(sw.toString());
			sb.append(' ');
		}
		source = sb.toString().trim();
	}

	@Override
	public int compareTo(Sentence o) throws NullPointerException  {
		return getSource().compareTo(o.getSource());
	}

	/**
	 * Gets Word(s) one at a time. Returns null when it runs out of Words.
	 * @return Word or null when done.
	 */
	@Override
	public Word get() {
		Word w = null;
		if(++index <= size()-1 ) {
			w = get(index);
		}
		return w;
	}
	
	
	public void append(Word w) {
		this.add(w);
	}
	
	public static Word getDelimiter() {
		return DELIM;
	}
	
	public static List<Word> getDelimiters() {
		return DELIMITERS;
	}
	
	public Word getTerminal() {
		return Sentence.TERMINAL;
	}
	
	public Word getNullValue() {
		return Sentence.NULL_VALUE;
	}
	
	public boolean isIgnoreWhiteSpace() {
		return ignoreWhiteSpace;
	}

	public void setIgnoreWhiteSpace(boolean ignoreWhiteSpace) {
		this.ignoreWhiteSpace = ignoreWhiteSpace;
	}

	public boolean isIgnorePunctuation() {
		return ignorePunctuation;
	}

	public void setIgnorePunctuation(boolean ignorePunctuation) {
		this.ignorePunctuation = ignorePunctuation;
	}

	public static void main(String... args) throws IOException {
		String text = args[0];
		Sentence sentence = new Sentence(text);
		sentence.setIgnorePunctuation(false);
		sentence.setIgnoreWhiteSpace(true);
		sentence.setId("1234929");
		Word word = null;
		System.out.println(text);
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((word = sentence.get()) != null) {
			System.out.println((++i)+ ": "+ word.toString());
		}
				
		System.out.println(sentence.toString());
		System.out.println(sentence.toJson(true));
		
		Sentence sub = sentence.subset(0, 3);
		System.out.println("subset: " + sub.toString());
		while((word = sub.get()) != null) {
			System.out.println((++i)+ ": "+ word.toString());
		}
	}

	public String getRawText() {
		return rawText;
	}

	@Override
	/** 
	 * The sentence name is anything that can identify the sentence- an Id, summary whatever.
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int size() {
		return words.size();
	}

	@Override
	public boolean isEmpty() {
		return words.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return words.contains(o);
	}

	@Override
	public Iterator<Word> iterator() {
		return words.iterator();
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(Word e) {
		return words.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return words.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Word> c) {
		return words.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Word> c) {
		return words.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return words.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return words.retainAll(c);
	}

	@Override
	public void clear() {
		words.clear();
	}

	@Override
	public Word get(int index) {
		return words.get(index);
	}

	@Override
	public Word set(int index, Word element) {
		return words.set(index, element);
	}

	@Override
	public void add(int index, Word element) {
		words.add(index, element);
	}

	@Override
	public Word remove(int index) {
		return words.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return words.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return words.lastIndexOf(o);
	}

	@Override
	public ListIterator<Word> listIterator() {
		return words.listIterator();
	}

	@Override
	public ListIterator<Word> listIterator(int index) {
		return words.listIterator(index);
	}

	@Override
	public List<Word> subList(int fromIndex, int toIndex) {
		return words.subList(fromIndex, toIndex);
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

}
