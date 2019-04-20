package org.dwbzen.text.relation;

import java.util.Set;

import org.dwbzen.common.math.Tupple;
import org.dwbzen.common.relation.OccurrenceRelation;
import org.dwbzen.text.element.Sentence;
import org.dwbzen.text.element.Word;

/**
 * 
 * @author don_bacon
 *
 */
public class CharacterOccurrenceRelation extends OccurrenceRelation<Character, Word, Sentence>  {

	private static final long serialVersionUID = 6745410389085910664L;
	private boolean ignoreCase = true;
	
	protected CharacterOccurrenceRelation() {
		super();
	}
	
	public CharacterOccurrenceRelation(Word unit, int degree) {
		this(unit, degree, false);
	}
	
	public CharacterOccurrenceRelation(Word unit, int degree, boolean ignoreCaseFlag) {
		ignoreCase = ignoreCaseFlag;
		this.unit = ignoreCase ? unit.toLowerCase() : unit;
		Word word = new Word(this.unit);
		super.partition(word, degree);
	}

	@Override
	public boolean isElement(Tupple<Character> tupple, Word unit) {
		return getPartitionKeys().contains( tupple.getKey());
	}
	
	public static void main(String...strings) {
		Word word = new Word(strings[0]);
		int degree = Integer.parseInt(strings[1]);
		CharacterOccurrenceRelation relation = new CharacterOccurrenceRelation(word, degree, true);
		Set<Tupple<Character>> partitions = relation.getPartitions();
		System.out.println("#of level " + degree + " partitions: " + partitions.size());
		partitions.forEach(t -> System.out.println(t.toString(false)));
		//System.out.println(word + "\n" + partitions);

	}

}
