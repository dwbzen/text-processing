package org.dwbzen.text.util;

import java.util.List;

import org.dwbzen.common.math.Tupple;
import org.dwbzen.text.element.Word;

public class WordTupple extends Tupple<Word> {
	
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
