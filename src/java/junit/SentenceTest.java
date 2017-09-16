package junit;

import org.junit.Test;

import junit.framework.TestCase;
import util.text.Sentence;
import util.text.Word;

public class SentenceTest extends TestCase {

	/**
	 * the test string uses regular single and double quotes: " (0x22) and ' (0x27)
	 * as well as start and end double quote: “ (0x93) and ” (0x94) and single quotes: ‘ (0x91) ’ (0x92)
	 */
	public String testString = "“Of    course, you’ll need training,” he tells me in a serious voice.  \"Do this 1'st?\" I ask.";
	
	public String testString2 = "“To Quote, ‘You'll do this first, do that 2'nd’. Okay?”";
	
	public String testString3 = "\"To Quote, 'You'll do this first, do that 2'nd. Okay?'\"";
	
	@Test
	public void test1_SentenceDefaults1() {
		System.out.println(testString);
		Sentence sentence = new Sentence(testString);
		Word w = null;
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((w = sentence.get()) != null) {
			System.out.println((++i)+ ": "+ w.toString());
		}
	}
	
	@Test
	public void test2_SentenceDefaults2() {
		System.out.println(testString2);
		Sentence sentence = new Sentence(testString2);
		Word w = null;
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((w = sentence.get()) != null) {
			System.out.println((++i)+ ": "+ w.toString());
		}
	}
	
	@Test
	public void test3_IgnoreWhiteSpace() {
		System.out.println(testString);
		Sentence sentence = new Sentence();
		sentence.setIgnoreWhiteSpace(true);
		sentence.setSource(testString);
		Word w = null;
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((w = sentence.get()) != null) {
			System.out.println((++i)+ ": "+ w.toString());
		}

	}
	
		@Test
		public void test4_IgnorePunctuationAndWhiteSpace1() {
		System.out.println(testString2);
		Sentence sentence = new Sentence();
		sentence.setIgnoreWhiteSpace(true);
		sentence.setIgnorePunctuation(true);
		sentence.setSource(testString2);
		Word w = null;
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((w = sentence.get()) != null) {
			System.out.println((++i)+ ": "+ w.toString());
		}
	}

	@Test
	public void test5_IgnorePunctuationAndWhiteSpace2() {
		System.out.println(testString);
		Sentence sentence = new Sentence();
		sentence.setIgnoreWhiteSpace(true);
		sentence.setIgnorePunctuation(true);
		sentence.setSource(testString);
		Word w = null;
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((w = sentence.get()) != null) {
			System.out.println((++i)+ ": "+ w.toString());
		}
	}
	
	@Test
	public void test6_IgnorePunctuationAndWhiteSpace3() {
		System.out.println(testString3);
		Sentence sentence = new Sentence();
		sentence.setIgnoreWhiteSpace(true);
		sentence.setIgnorePunctuation(true);
		sentence.setSource(testString3);
		Word w = null;
		System.out.println("#words: " + sentence.size());
		int i = 0;
		while((w = sentence.get()) != null) {
			System.out.println((++i)+ ": "+ w.toString());
		}
	}

	
}
