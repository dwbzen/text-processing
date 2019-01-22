package org.dwbzen.text.util.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.cp.ICollectable;
import mathlib.util.IJson;

/**
 * A Word is a String that has an underlying structure of List<Character>.
 * 
 * @author don_bacon
 *
 */
public class Word extends ArrayList<Character> 
	implements Comparable<Word>, List<Character>, Supplier<Character>, IntConsumer, ICollectable<Character>, IJson {

	private static final long serialVersionUID = 7157404340284643268L;
	@JsonProperty("word")	private StringBuffer wordString = new StringBuffer();
	
	public static Character DELIM_CHARACTER =  ' ';
	public static String DELIM_STRING = String.valueOf(DELIM_CHARACTER);
	
	/** 0xB6 Used to represent a Terminal character */
	public static Character TERMINAL = '¶';
	
	/** 0xA7 Used to represent a NULL key in a Map - since it can't really be a null */
	public static Character NULL_VALUE = '§';
	@JsonIgnore	private int index = -1;
	
	public Word() {
	}
	
	public Word(Character c) {
		if(c != null) {
			wordString.append(c);
			toCharacterList();
		}
	}
	
	public Word(String string) {
		if(string != null) {
			wordString.append(string);
			toCharacterList();
		}
		else throw new NullPointerException();
	}
	
	public Word(String padLeftString, Word ws) {
		this(padLeftString, ws, null);
	}

	public Word( String padLeftString, Word ws, String padRightString) {
		if(ws != null) {
			if(padLeftString != null) {
				wordString.append(padLeftString);
			}
			wordString.append(ws);
			if(padRightString != null) {
				wordString.append(padRightString);
			}
			toCharacterList();
		}
		else throw new NullPointerException();
	}
	
	/**
	 * Deep copy constructor.
	 * @param otherWord
	 */
	public Word(Word otherWord) {
		if(otherWord != null && otherWord.getWordString() != null) {
			wordString.append(otherWord.getWordString());
			toCharacterList();
		}
		else throw new NullPointerException();
	}
	
	@Override
	public int compareTo(Word other) throws NullPointerException {
		int res = 0;
		if(other.toString() != null && wordString != null) {
			res = wordString.toString().compareTo(other.toString());
		}
		else throw new NullPointerException();
		return res;
	}
	
	private void toCharacterList() {
		if(wordString != null) {
			wordString.toString().chars().forEach(c -> accept(c));
		}
	}
	
	/**
	 * Returns a new Word of the portion of this Word
	 * between the specified fromIndex, inclusive, and toIndex, exclusive.
	 * So it works like String.substring(start, end)
	 * @param startIndex beginning index to get, inclusive
	 * @param endIndex  end index to get, exclusive
	 * @return Word
	 */
	public Word subset(int fromIndex, int toIndex) {
		return new Word(wordString.substring(fromIndex, toIndex));
	}
	
	/**
	 * Returns a new Word of the portion of this Word
	 * from the specified fromIndex, inclusive to the end of the String
	 * So it works like String.substring(start)
	 * @param startIndex beginning index to get, inclusive
	 * @return Word
	 */
	public Word subset(int fromIndex) {
		return new Word(wordString.substring(fromIndex));
	}

	public Word trim() {
		wordString = new StringBuffer();
		stream().filter(c -> c != ' ').forEach(c -> wordString.append(c) );
		while(remove(DELIM_CHARACTER)) { remove(DELIM_CHARACTER); }
		return this;
	}
	public static Character getDelimiter() {
		return DELIM_CHARACTER;
	}
	
	@Override
	public Character getTerminal() {
		return Word.TERMINAL;
	}
	
	@Override
	public Character getNullValue() {
		return Word.NULL_VALUE;
	}
	
	
	@Override
	public String toString() {
		return wordString.toString();
	}
	
	public String getWordString() {
		return wordString.toString();
	}

	public void setWordString(String wordString) {
		this.wordString = new StringBuffer(wordString);
	}
	
	public void append(Character c) {
		add(c);
		wordString.append(c);
	}
	
	public String lower() {
		return wordString.toString().toLowerCase();
	}
	
	public Word toLowerCase() {
		return new Word(lower());
	}

	@Override
	public void accept(int value) {
		add((char) value);
	}

	/**
	 * Gets characters one at a time. Returns null when it runs out of characters.
	 */
	@Override
	public Character get() {
		Character c = null;
		if(++index <= size()-1 ) {
			c = get(index);
		}
		return c;
	}
	
	@Override
	public String toJson() {
		return "[\"" + wordString.toString() + "\"]";
	}
	
	@Override
	public String toJson(boolean pretty) {
		return this.toJson();
	}

}
