package org.dwbzen.text.pos;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import mathlib.util.IJson;

/**
 * A Pattern combines the following ideas:
 * 0. Atom - the universe of all objects of a particular class
 * 1. Vocabulary  - a non-empty subset of well-formed Atoms.
 *    Each is given a context that serves as a sort of definition.
 * 2. Word - a vocabulary element + InstanceRule
 *    an InstanceRule gives the creation context for the Word
 * 3. Sentence - a string of Words
 * 4. WordInstance - a specific instance of a Word that
 *    satisfies the creation context
 * 5. Pattern - a particular ordered-list of WordInstances derived
 *    from a given sentence
 * Example: parts of speech
 * Atom - single characters (char type)
 * Vocabulary - { 'N', 'L', 'p', 'V', etc. }
 * Word - defined as follows:
 *   if x, y are elements of the Vocabulary,
 *   then the following are Words:
 *   x?     - x is optional same as x{0,1}
 *   x{1}   - x appears exactly once
 *   x{0,4}  - x appears 0 to 4 times
 *   x{2,5} - x appears 2 to 5 times
 *  [x | y] - select x or y
 *  [x | y]{1,3}  - select x or y up to 3 times
 * WordInstances of "N{2,5}" are "NN" "NNN" "NNNN" and "NNNNN"
 * Sentence: "[F|M]vA{1,3}N"
 * An example Sentence instance: "FvAAN", so is "MvN" and "MvAAAN"
 * A TextPattern can also include constants (inline text) that
 * is left uninterpreted. Inline text is delimited by parens
 * as in (include me). If (x) is inline text, the following
 * is also legal: (x){m,n}   (x){n} [(x)|(y)] etc.
 * A TextPattern has a Vocabulary of single characters.
 * 
 * @author don_bacon
 *
 */
public abstract class TextPattern implements java.util.Iterator<PatternWord>, IJson {

	private static final long serialVersionUID = 1L;
	@JsonIgnore	protected Set<String> vocabulary;
	
	public Set<String> getVocabulary() {
		return vocabulary;
	}

	public abstract void setVocabulary(Set<String> vocabulary);

	public abstract boolean hasNext();

	public abstract PatternWord next();
	
	public abstract IPatternParser getPatternParser();
	
	public abstract String createInstance();

	public void remove() {
		// not implemented
	}

}
