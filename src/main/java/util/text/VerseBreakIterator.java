package util.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.Locale;

/**
 * A BreakIterator for poetry/verse which may not follow the standard
 * sentence structure.
 * 
 * @author don_bacon
 *
 */
public class VerseBreakIterator extends java.text.BreakIterator {

	private Locale locale = null;
	private String text = null;	// the text to scan
	int start = 0;
	int end = 0;
	
	protected VerseBreakIterator() {
		super();
	}
	
	protected VerseBreakIterator(Locale l) {
		super();
		locale = l;
	}

	@Override
	/**
	 * Returns the first boundary. The iterator's current position is set to the first text boundary.
	 */
	public int first() {
		return 0;
	}

	@Override
	/**
	 * Returns the last boundary. The iterator's current position is set to the last text boundary.
	 */
	public int last() {
		return text.indexOf('\n');
	}

	@Override
	public int next(int pos) {
		if(pos >= text.length()) {
			end = BreakIterator.DONE;
		}
		else {
			end = text.indexOf('\n', start) + 1;
			start = end;
		}
		return end;
	}

	@Override
	public int next() {
		return next(end);
	}

	@Override
	public int previous() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int following(int offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int current() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	/**
	 * Set a new text for scanning. The current scan position is reset to first().
	 */
	public void setText(String newText) {
		this.text = newText;
	}

	@Override
	public CharacterIterator getText() {
		BreakIterator bi = BreakIterator.getCharacterInstance();
		bi.setText(this.text);
		return (CharacterIterator) bi;
	}

	@Override
	public void setText(CharacterIterator newText) {
		StringBuffer sb = new StringBuffer(newText.first());
		Character c = null;
		while((c=newText.next()) != CharacterIterator.DONE) {
			sb.append(c);
		}
		this.text = sb.toString();
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
