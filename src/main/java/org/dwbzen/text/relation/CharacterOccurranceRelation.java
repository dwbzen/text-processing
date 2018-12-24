package org.dwbzen.text.relation;

import java.util.Set;

import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.Tupple;
import mathlib.cp.OccurranceRelation;

public class CharacterOccurranceRelation extends OccurranceRelation<Character, Word, Sentence>  {
	private boolean ignoreCase = true;
	
	protected CharacterOccurranceRelation() {
		super();
	}
	
	public CharacterOccurranceRelation(Word unit, int degree) {
		this(unit, degree, false);
	}
	
	public CharacterOccurranceRelation(Word unit, int degree, boolean ignoreCaseFlag) {
		ignoreCase = ignoreCaseFlag;
		Word word = ignoreCase ? unit.toLowerCase() : unit;
		super.partition(word, degree);
	}

	@Override
	public boolean isElement(Tupple<Character> tupple, Word unit) {
		return getPartitionKeys().contains( tupple.getKey());
	}
	
	public static void main(String...strings) {
		Word word = new Word(strings[0]);
		int degree = Integer.parseInt(strings[1]);
		CharacterOccurranceRelation relation = new CharacterOccurranceRelation(word, degree, true);
		Set<Tupple<Character>> partitions = relation.getPartitions();
		System.out.println(word + "\n" + partitions);
		
		Tupple<Character> t1 = new Tupple<>(new Word("ao"));
		System.out.println("'" + t1.toString() + "' key : " +  t1.getKey() + " isElement: " + relation.isElement(t1, word));

	}

}
