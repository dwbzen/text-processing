package org.dwbzen.text.junit;

import org.dwbzen.text.element.Word;
import org.dwbzen.text.relation.TuppleCharacterDistanceMetric;
import org.junit.Test;

import junit.framework.TestCase;
import org.dwbzen.common.math.Tupple;

public class TuppleDistanceMetricTest  extends TestCase {
	
	private TuppleCharacterDistanceMetric metric = new TuppleCharacterDistanceMetric();
	
	public TuppleDistanceMetricTest() {
		
	}
	 @Test
	 public void testTuppleCharacterDistanceMetric() {
			Tupple<Character> t1 = new Tupple<>('a', 'e');
			Tupple<Character> t2 = new Tupple<>('a', 'e', 'x');
			Tupple<Character> t3 = new Tupple<>('a', 'e', 'x', 'l');
			Tupple<Character> t4 = new Tupple<>('d', 'x', 'm');
			
			Word word1 = new Word("Donatella");
			Word word2 = new Word("Venlafaxine");
			Word word3 = new Word("Dream");
			
			double d1 = metric.apply(t1, word1);
			System.out.println("tupple: " + t1 + " word: " + word1 + " distance: " + d1);
			assertEquals(2.5, d1);
			
			double d2 = metric.apply(t1, word2);
			System.out.println("tupple: " + t1 + " word: " + word2 + " distance: " + d2);
			assertEquals(4.5, d2);
			
			double d3 = metric.apply(t2, word2);
			System.out.println("tupple: " + t2 + " word: " + word2 + " distance: " + d3);
			assertEquals(4.9, d3);
			
			double d4 = metric.apply(t3, word1);
			System.out.println("tupple: " + t3 + " word: " + word1 + " distance: " + d4);
			
			double d5 = metric.apply(t4, word3);
			System.out.println("tupple: " + t4 + " word: " + word3 + " distance: " + d5);
			assertEquals(4.0, d5);

	 }

}
