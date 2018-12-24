package org.dwbzen.text.util;

import java.util.List;

import org.dwbzen.text.util.model.Word;

import mathlib.Tupple;

public class WordTupple extends Tupple<Word> {

	private static final long serialVersionUID = 8455018677104575801L;
	
	public WordTupple() {
		super();
	}
	
	public WordTupple(Word[] ts) {
		super(ts);
	}
	
	public WordTupple(List<Word> tElements) {
		super(tElements);
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	@Override
	public String toString(boolean formatted) {
		StringBuilder sb = new StringBuilder();
		if(formatted) {
			sb.append("[ ");
			elements.forEach(t -> sb.append("'" + t.toString() + "',"));
			sb.delete(sb.length()-1, sb.length()).append(" ]");
		}
		else {
			elements.forEach(t -> sb.append(t.toString()));
		}
		return sb.toString();
	}

}
