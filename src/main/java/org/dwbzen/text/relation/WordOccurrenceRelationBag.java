package org.dwbzen.text.relation;


import java.util.Map;

import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.SourceOccurrenceProbability;
import mathlib.Tupple;
import mathlib.cp.OutputStyle;
import mathlib.relation.OccurrenceRelationBag;

public class WordOccurrenceRelationBag extends OccurrenceRelationBag<Word, Sentence, Book> {
	private static final long serialVersionUID = 1L;
	static OutputStyle outputStyle = OutputStyle.TEXT;
	static boolean trace = true;
	
	public WordOccurrenceRelationBag(int degree) {
		super(degree);
		setMetricFunction(new TuppleWordDistanceMetric());
	}
	
	/**
	 * Copy constructor (shallow)
	 * @param WordOccurrenceRelationBag otherBag
	 * @param Map<Tupple<Wortd>, SourceOccurrenceProbability<Word, Sentence>> optionalMap
	 */
	public WordOccurrenceRelationBag(WordOccurrenceRelationBag otherBag,  Map<Tupple<Word>, SourceOccurrenceProbability<Word,Sentence>> optionalMap) {
		super(otherBag.getDegree());
		setSupressSourceOutput(otherBag.isSupressSourceOutput());
		setTotalOccurrences(otherBag.getTotalOccurrences());
		setMetricFunction(new TuppleWordDistanceMetric());
		if(optionalMap != null) {
			setSourceOccurrenceProbabilityMap(optionalMap);
		}
		recomputeProbabilities();
	}

	
}
