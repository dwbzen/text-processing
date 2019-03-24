package org.dwbzen.text.pos;

public enum PosCategory {
	noun(1), verb(2), adjective(3), adverb(4), pronoun(5), preposition(6), conjunction(7), interjection(8), determiner(9);
	PosCategory(int val) { this.value = val;}
	private int value;
    public int value() { return value; }
}
