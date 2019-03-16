package org.dwbzen.text.relation;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import org.dwbzen.common.math.Tupple;
import org.dwbzen.common.relation.Partitions;

public class TuppleWordDistanceMetric implements  BiFunction<Tupple<Word>, Sentence, Double> {
	private boolean ignoreCase = true;

	public TuppleWordDistanceMetric() {}
	
	public TuppleWordDistanceMetric(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	@Override
	public Double apply(Tupple<Word> tupple, Sentence sentence) {
		double distance = 0.0;
		double sum = 0.0;
		int count = 0;
		// step 1 - get all the 2-Character combinations of tupple characters
		Set<Tupple<Word>> tupplePartitions = getPartitions(tupple, 2);
		// step 2 - for each combo, get the indexes in Word
		Set<Integer> c1Indexes = new TreeSet<Integer>();
		Set<Integer> c2Indexes = new TreeSet<Integer>();
		for(Tupple<Word> tpart : tupplePartitions) {
			int ind = 0;
			for(Word w : sentence) {
				Word word = ignoreCase ? w.toLowerCase() : w;
				if(word.equals(tpart.index(0))) {
					c1Indexes.add(ind);
				}
				if(word.equals(tpart.index(1))) {
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
	
	public static Set<Tupple<Word>> getPartitions(Tupple<Word> tupple, int len) {
		Partitions<Word> part = new Partitions<>(tupple, len);
		Set<Tupple<Word>> partitions = part.getPartitions();
		return partitions;
	}
	
	public static void main(String...strings ) {
		TuppleWordDistanceMetric metric = new TuppleWordDistanceMetric();
		
		Sentence s1 = new Sentence("Confirm Delete for Certificate refers to a Cancel button. There is no Cancel button");
		Word w1 = new Word("button");
		Word w2 = new Word("cancel");
		Word w3 = new Word("delete");
		Tupple<Word> t1 = new Tupple<>(w1, w2, w3);
		double d1 = metric.apply(t1, s1);
		System.out.println("tupple: " + t1.toString(true) + " sentence: [" + s1 + "] distance: " + d1);
		
		Tupple<Word> t2 = new Tupple<>(w2, w3);
		double d2 = metric.apply(t2, s1);
		System.out.println("tupple: " + t2.toString(true) + " sentence: [" + s1 + "] distance: " + d2);


	}

}
