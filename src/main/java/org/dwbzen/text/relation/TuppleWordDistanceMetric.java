package org.dwbzen.text.relation;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.dwbzen.text.util.model.Word;

import mathlib.Tupple;
import mathlib.relation.OccurrenceRelation;

public class TuppleWordDistanceMetric implements  BiFunction<Tupple<Character>, Word, Double> {

	/**
	 * Given a Tupple<Character>, and a Word instance, is computes the distance(s)
	 * of the Tupple elements within the Word.<br>
	 * Example: Tupple = "ae", Word = "Donatella", avg. distance = (2 + 3)/2 = 2.5<br>
	 * Example: Tupple = "aex", Word = "Venlafaxine", distance = (3 + 5 + 6 + 4 + 3 + 1 + 6 + 3)/8 = 3.875<br>
	 * In other words, the #characters separating each 2-Character combination in the Tupple.<br>
	 * This is a measure of how "spread out" the letters are in the Word.
	 * 
	 */
	@Override
	public Double apply(Tupple<Character> tupple, Word word) {
		double distance = 0.0;
		double sum = 0.0;
		int count = 0;
		// step 1 - get all the 2-Character combinations of tupple characters
		Set<Tupple<Character>> tupplePartitions = getPartitions(tupple);
		// step 2 - for each combo, get the indexes in Word
		Set<Integer> c1Indexes = new TreeSet<Integer>();
		Set<Integer> c2Indexes = new TreeSet<Integer>();
		for(Tupple<Character> tpart : tupplePartitions) {
			int ind = 0;
			for(Character c : word) {
				if(c.equals(tpart.index(0))) { c1Indexes.add(ind); }
				if(c.equals(tpart.index(1))) { c2Indexes.add(ind); }
				ind++;
			}
		}
		// step 3 - sum up the absolute values of the pairwise differences in the two index lists
		for(Integer c1Ind : c1Indexes) {
			for(Integer c2Ind : c2Indexes) {
				sum += Math.abs(c1Ind - c2Ind);
				count++;
			}
		}
		distance = (count == 0) ? 0 : sum / count;
		return distance;
	}
	
	public static Set<Tupple<Character>> getPartitions(Tupple<Character> tupple) {
		int len = tupple.size();
		Set<Tupple<Character>> partitions = new TreeSet<>();
		if(len <= 2) {
			partitions.add(tupple);
			return partitions;
		}
		int degree = 2;
		int index = 0;
		String tstring = tupple.toString();
		double nSets = Math.pow(2, len);	// Power set cardinality
		for(int i = 1; i<nSets; i++) {
			if(OccurrenceRelation.nbits(i) == degree) {
				Tupple<Character> partitionTupple = new Tupple<>(degree);
				int j = 0;
				do {
					if((1 & (i>>j)) == 1) {
						index = len - 1 - j;
						partitionTupple.add(tstring.charAt(index));
					}
				} while(Math.pow(2, j++) <= nSets);
				partitions.add(partitionTupple);
			}
		}
		return partitions;
	}
	
	public static void main(String...strings) {
		CharacterOccurrenceRelation cor = new CharacterOccurrenceRelation(new Word("ael"), 2);
		System.out.println(cor.getPartitions());
		
		Tupple<Character> t1 = new Tupple<>(2);
		t1.add('a').add('e');
		Set<Tupple<Character>> partitions = getPartitions(t1);
		System.out.println(partitions);

		Tupple<Character> t2 = new Tupple<>(3);
		t2.add('a').add('e').add('x');
		partitions = getPartitions(t2);
		System.out.println(partitions);
		
		Tupple<Character> t3 = new Tupple<>(4);
		t3.add('x').add('a').add('e').add('l');
		partitions = getPartitions(t3);
		System.out.println(partitions);
		
		Word word1 = new Word("Donatella");
		Word word2 = new Word("Venlafaxine");
		TuppleWordDistanceMetric metric = new TuppleWordDistanceMetric();
		double d1 = metric.apply(t1, word1);
		double d2 = metric.apply(t1, word2);
		System.out.println("tupple: " + t1 + " word: " + word1 + " distance: " + d1);
		System.out.println("tupple: " + t1 + " word: " + word2 + " distance: " + d2);
		double d3 = metric.apply(t2, word2);
		System.out.println("tupple: " + t2 + " word: " + word2 + " distance: " + d3);
		
	}
}
