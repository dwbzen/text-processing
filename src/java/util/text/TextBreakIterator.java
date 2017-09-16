package util.text;

import java.util.Locale;

public class TextBreakIterator  {

	public static java.text.BreakIterator getVerseInstance() {
		return new VerseBreakIterator();
	}
	
	public static java.text.BreakIterator getVerseInstance(Locale locale) {
		return new VerseBreakIterator(locale);
	}

	public static java.text.BreakIterator getSentenceInstance() {
		return java.text.BreakIterator.getSentenceInstance();
	}
	
	public static java.text.BreakIterator getSentenceInstance(Locale locale) {
		return java.text.BreakIterator.getSentenceInstance(locale);
	}
	
	public static java.text.BreakIterator getWordInstance() {
		return java.text.BreakIterator.getWordInstance();
	}
	
	public static java.text.BreakIterator getWordInstance(Locale locale) {
		return java.text.BreakIterator.getWordInstance(locale);
	}

	public static java.text.BreakIterator getLineInstance() {
		return java.text.BreakIterator.getLineInstance();
	}
	
	public static java.text.BreakIterator getLineInstance(Locale locale) {
		return java.text.BreakIterator.getLineInstance(locale);
	}

	public static java.text.BreakIterator getCharacterInstance() {
		return java.text.BreakIterator.getCharacterInstance();
	}
	
	public static java.text.BreakIterator getCharacterInstance(Locale locale) {
		return java.text.BreakIterator.getCharacterInstance(locale);
	}

}
