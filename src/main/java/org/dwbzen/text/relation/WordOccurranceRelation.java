package org.dwbzen.text.relation;

import java.util.Map;

import org.dwbzen.text.util.WordTupple;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import org.dwbzen.common.math.Tupple;
import org.dwbzen.common.relation.OccurrenceRelation;

public class WordOccurranceRelation extends OccurrenceRelation<Word, Sentence, Book> {
	
	private static final long serialVersionUID = -72193910166450859L;
	private boolean ignoreCase = true;
	private String id = null;

	protected WordOccurranceRelation() {
		super();
	}
	
	public WordOccurranceRelation(Sentence unit, int degree) {
		this(unit, degree, false);
	}
	
	public WordOccurranceRelation(Sentence unit, int degree, boolean ignoreCaseFlag) {
		ignoreCase = ignoreCaseFlag;
		this.unit = ignoreCase ? unit.toLowerCase() : unit;
		id = unit.getId();
		super.partition(unit, degree);
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	@Override
	public int getKey(Tupple<Word> word) {
		return word.toString().trim().hashCode();
	}
	
	public String getId() {
		return id;
	}

	@Override
	public boolean isElement(Tupple<Word> tupple, Sentence unit) {
		return getPartitionKeys().contains( tupple.getKey());
	}

	public static void main(String...strings) {
		Sentence sentence = new Sentence(strings[0], true, true);
		int degree = Integer.parseInt(strings[1]);
		String testString = strings[2];
		WordOccurranceRelation relation = new WordOccurranceRelation(sentence, degree, true);
		Map<Tupple<Word>, Integer> partitionKeyMap = relation.getPartitionKeyMap();
		System.out.println(sentence + "\n" + partitionKeyMap);
		
		WordTupple t1 = new WordTupple(new Sentence(testString));
		System.out.println("\n" + t1.toString(true) + " key : " +  t1.getKey() + " isElement: " + relation.isElement(t1, sentence));
		
	}
}
