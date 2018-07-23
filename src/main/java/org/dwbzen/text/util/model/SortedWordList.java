package org.dwbzen.text.util.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A SortedSet of unique words is maintained in a separate TreeSet<Word> collection.
 * @author don_bacon
 *
 */
public class SortedWordList extends ArrayList<Word> {

	private static final long serialVersionUID = -6592509264681790643L;
	@JsonIgnore private SortedSet<Word> sortedWords = new TreeSet<>();
	
	public SortedWordList() {
		super();
	}
	
	public SortedSet<Word> getSortedWords() {
		return sortedWords;
	}
	
	@Override
	public boolean add(Word w) {
		sortedWords.add(w);
		return super.add(w);
	}
	
	@Override
	public void add(int index, Word w) {
		sortedWords.add(w);
		super.add(index, w);
	}
	
	@Override
	public boolean addAll(Collection<? extends Word> collection) {
		sortedWords.addAll(collection);
		return super.addAll(collection);
	}
	
	@Override
	public void clear() {
		super.clear();
		sortedWords.clear();
	}
	public static void main(String... args) throws IOException {
		String text = args[0];
		Sentence sentence = new Sentence();
		sentence.setIgnorePunctuation(false);
		sentence.setIgnoreWhiteSpace(true);
		sentence.setSource(text);
		Word w = null;
		SortedWordList swl = new SortedWordList();
		System.out.println(text);
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((w = sentence.get()) != null) {
			swl.add(w);
			System.out.println((++i)+ ": "+ w.toString());
		}
		SortedSet<Word> words = swl.getSortedWords();
		System.out.println(words);
	}

}
