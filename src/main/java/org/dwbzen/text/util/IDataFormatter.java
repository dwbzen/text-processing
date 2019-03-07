package org.dwbzen.text.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dwbzen.text.util.model.Sentence;

/**
 * Takes text data in one form and transforms it to a standard format for a Collector
 * IDataFormatters can be chained together as needed.
 * 
 * @author DBacon
 *
 */
public interface IDataFormatter<T extends Serializable>  {
	default T format(T rawData) {
		return rawData;
	}
	
	default Sentence formatAsSentence(T rawData) {
		return new Sentence(rawData.toString());
	}
	
	String getKey();
	String getId();
	
	default List<T> formatAsList(T rawData) {
		List<T> list = new ArrayList<T>();
		list.add(format(rawData));
		return list;
	}
}
