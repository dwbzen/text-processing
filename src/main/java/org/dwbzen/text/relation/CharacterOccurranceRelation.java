package org.dwbzen.text.relation;

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
	public boolean isElement(Tupple<Character> element, Word unit) {
		// TODO Auto-generated method stub
		return false;
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
	
	public static void main(String...strings) {
		Word word = new Word("eliquis");
		CharacterOccurranceRelation relation = new CharacterOccurranceRelation();
		Set<Tupple<Character>> partitions = relation.partition(word, 2);
		System.out.println(partitions);
	}


}
