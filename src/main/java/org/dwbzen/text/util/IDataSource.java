package org.dwbzen.text.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface IDataSource<T extends Serializable> {
	
	T getData();
	
	T getNext();
	
	DataSourceDescription getDataSourceDescription();
	
	/**
	 * The default returns the result of getData() as a List<T> with 1 element.
	 * Overload as needed. For example, tokenize a text string and return as List<String>
	 * @return
	 */
	default List<T> getDataList() {
		List<T> list = new ArrayList<T>();
		list.add(getData());
		return list;
	}
	
	default DataSourceType getDataSourceType() {
		return DataSourceType.Unknown;
	}
}
