package org.dwbzen.text.relation;

import java.util.Set;

import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.Tupple;
import mathlib.relation.OccurrenceRelationBag;

public class CharacterOccurrenceRelationBag extends OccurrenceRelationBag<Character, Word, Sentence> {

	public CharacterOccurrenceRelationBag(int degree) {
		super(degree);
	}
	
	public static void main(String...args) {
		Sentence sentence = new Sentence(args[0]);
		int degree = Integer.parseInt(args[1]);
		CharacterOccurrenceRelationBag occurrenceRelationBag = new CharacterOccurrenceRelationBag(degree);
		for(Word word:sentence) {
			CharacterOccurrenceRelation occurrenceRelation = new CharacterOccurrenceRelation(word, degree, true);
			Set<Tupple<Character>> partitions = occurrenceRelation.getPartitions();
			System.out.println(word + "\n" + partitions);
			occurrenceRelationBag.addOccurrenceRelation(occurrenceRelation);
		}
		System.out.println(occurrenceRelationBag.toJson(true));
	}

}
