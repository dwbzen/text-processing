package org.dwbzen.text.junit;

import org.dwbzen.text.util.model.Word;
import org.junit.Test;

import junit.framework.TestCase;

public class WordTest extends TestCase {
	private Word word = null;
	private String testString1 = "Now is the time for 1,2,3.";
	
	public WordTest() {
		super();
	}
	
	@Test
	public void testWord1() {
		word = new Word(testString1);
		String wordChars = showCharacters(testString1 + ":");
		assertTrue(wordChars.equals("'N' 'o' 'w' ' ' 'i' 's' ' ' 't' 'h' 'e' ' ' 't' 'i' 'm' 'e' ' ' 'f' 'o' 'r' ' ' '1' ',' '2' ',' '3' '.' "));
	}
	
	@Test
	public void testNullWord() {
		word = new Word("");
		assertTrue(word != null & word.size()==0);
	}
	
	@Test
	public void testTrim() {
		word = new Word(" gentyx ");
		word.trim();
		assertEquals(6, word.size());
		assertTrue(word.getWordString().equals("gentyx"));
}

	@Test
	public void testTrim2() {
		word = new Word("  gentyx  ");
		word.trim();
		assertEquals(6, word.size());
		assertTrue(word.getWordString().equals("gentyx"));
	}

	public String showCharacters(String s) {
		StringBuilder sb = new StringBuilder();
		for(Character c : word) {
			sb.append("'" + c + "' ");
		}
		return sb.toString();
	}
	
}
