package org.dwbzen.text.relation;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.Tupple;
import mathlib.cp.Relation;

public class CharacterOccurranceRelation extends Relation<Character, Word, Sentence>  {
	
	public CharacterOccurranceRelation() {
		
	}

	@Override
	public boolean isElement(Tupple<Character> tupple, Word unit) {
		Collection<Integer> keys = partitionKeys(unit, tupple.size());
		return keys.contains( tupple.getKey());
	}

	@Override
	/**
	 * Partitions a given Word into degree-length subsets.
	 * @return Returns a sorted Collection of all degree-length Character subsets of a given Word as a Collection of Tuplet<Character>
	 * 
	 */
	public Set<Tupple<Character>> partition(Word unit, int degree) {
		Set<Tupple<Character>> partitions = new TreeSet<>();
		if(unit.size() == degree) {
			partitions.add(new Tupple<Character>(unit));
		}
		else if(unit.size() > degree) {
			partitions = super.partition(unit, degree);
		}
		return partitions;
	}
	
	public Collection<Integer> partitionKeys(Word unit, int degree) {
		return super.createPartitionKeys(unit, degree).values();
	}
	
	public static void main(String...strings) {
		Word word = new Word(strings[0]);
		int degree = Integer.parseInt(strings[1]);
		CharacterOccurranceRelation relation = new CharacterOccurranceRelation();
		Set<Tupple<Character>> partitions = relation.partition(word, degree);
		System.out.println(partitions);
		System.out.println(relation.partitionKeys(word, degree));
		
		Tupple<Character> t1 = new Tupple<>('q', 'i', 'e', 'e');
		System.out.println(t1.toString() + " key : " +  t1.getKey());
		System.out.println(relation.isElement(t1, word));
	}


}
