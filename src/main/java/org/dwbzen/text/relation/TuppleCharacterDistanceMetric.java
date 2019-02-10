package org.dwbzen.text.relation;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.dwbzen.text.util.model.Word;

import mathlib.Tupple;
import mathlib.relation.Partitions;

/**
 * Given a Tupple<Character>, and a Word instance, is computes the mean distance
 * (where distance is the number of characters separating a given pair of characters),
 * of the Tupple elements within the Word.<br>
 * Example: Tupple = "ae", Word = "Donatella", avg. distance = (2 + 3)/2 = 2.5<br>
 * Example: Tupple = "aex", Word = "Venlafaxine", distance = 4.5<br>
 * In other words, the #characters separating each 2-Character combination in the Tupple.<br>
 * This is a measure of how "spread out" the letters are in the Word.
 * By default the metric ignores the case of Characters within the Word, but NOT the
 * Characters in the Tupple, when doing comparisons.
 * 
 */
public class TuppleCharacterDistanceMetric implements  BiFunction<Tupple<Character>, Word, Double> {
	private boolean ignoreCase = true;

	public TuppleCharacterDistanceMetric() {}
	
	public TuppleCharacterDistanceMetric(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	@Override
	public Double apply(Tupple<Character> tupple, Word word) {
		double distance = 0.0;
		double sum = 0.0;
		int count = 0;
		// step 1 - get all the 2-Character combinations of tupple characters
		Set<Tupple<Character>> tupplePartitions = getPartitions(tupple, 2);
		// step 2 - for each combo, get the indexes in Word
		Set<Integer> c1Indexes = new TreeSet<Integer>();
		Set<Integer> c2Indexes = new TreeSet<Integer>();
		for(Tupple<Character> tpart : tupplePartitions) {
			int ind = 0;
			for(Character c : word) {
				Character cc = ignoreCase ? Character.toLowerCase(c) : c;
				if(cc.equals(tpart.index(0))) { 
					c1Indexes.add(ind); 
				}
				if(cc.equals(tpart.index(1))) { 
					c2Indexes.add(ind); 
				}
				ind++;
			}
		}
		// step 3 - sum up the absolute values of the pairwise differences in the two index lists
		for(Integer c1Ind : c1Indexes) {
			for(Integer c2Ind : c2Indexes) {
				if(c1Ind != c2Ind) {
					sum += Math.abs(c1Ind - c2Ind);
					count++;
				}
			}
		}
		distance = (count == 0) ? 0 : sum / count;
		return distance;
	}
	
	public static Set<Tupple<Character>> getPartitions(Tupple<Character> tupple, int len) {
		Partitions<Character> part = new Partitions<>(tupple, len);
		Set<Tupple<Character>> partitions = part.getPartitions();
		return partitions;
	}
	
}
