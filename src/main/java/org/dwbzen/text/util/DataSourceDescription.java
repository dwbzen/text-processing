package org.dwbzen.text.util;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;
import mathlib.util.INameable;

public class DataSourceDescription implements INameable, IJson {

	private static final long serialVersionUID = 1595143609157156758L;
	
	@JsonProperty	private String name = DEFAULT_NAME;
	@JsonProperty	private DataSourceType	dataSourceType = DataSourceType.Unknown;
	@JsonProperty	private Configuration configuration = Configuration.getInstance();
	
	public DataSourceDescription(DataSourceType aType) {
		dataSourceType = aType;
	}
	
	public DataSourceDescription(String aName, DataSourceType aType) {
		name = aName;
		dataSourceType = aType;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DataSourceType getDataSourceType() {
		return dataSourceType;
	}
	public void setDataSourceType(DataSourceType dataSourceType) {
		this.dataSourceType = dataSourceType;
	}
	
	public Properties getProperties() {
		return configuration.getProperties();
	}
}
