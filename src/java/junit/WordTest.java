package junit;

import org.junit.Test;

import junit.framework.TestCase;
import util.text.Word;

public class WordTest extends TestCase {
	private Word word = null;
	private String testString1 = "Now is the time for 1,2,3.";
	
	public WordTest() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(String... args) {
	}
	
	@Test
	public void testWord1() {
		word = new Word(testString1);
		showCharacters(testString1 + ":");
	}
	
	@Test
	public void testNullWord() {
		word = new Word("");
		showCharacters("empty string: ");
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

	public void showCharacters(String s) {
		System.out.print(s);
		for(Character c : word) {
			System.out.print("'" + c + "' ");
		}
		System.out.println("");
	}
	
}
