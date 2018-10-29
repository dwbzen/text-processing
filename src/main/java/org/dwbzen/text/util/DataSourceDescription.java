package org.dwbzen.text.util;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;
import mathlib.util.INameable;

public class DataSourceDescription implements INameable, IJson {

	private static final long serialVersionUID = 1595143609157156758L;
	
	@JsonProperty	private String name = DEFAULT_NAME;		// name of the DataSource not the input
	@JsonProperty	private DataSourceType	dataSourceType = DataSourceType.Unknown;
	@JsonProperty	private Configuration configuration = Configuration.getInstance();
	@JsonProperty	private String schema = null;
	@JsonProperty	private String extension = null;	// content type - .json etc.
	@JsonProperty	private String inputFileName = null;
	
	public DataSourceDescription(DataSourceType aType) {
		dataSourceType = aType;
		extension = "txt";
	}
	
	public DataSourceDescription(String inputFile, DataSourceType aType) {
		inputFileName = inputFile;
		int index = name.lastIndexOf('.');
		extension = index >=0 ? name.substring(index + 1) : "txt";
		dataSourceType = aType;
		configuration.getProperties().setProperty("filename", inputFile);
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
	public DataSourceDescription setDataSourceType(DataSourceType dataSourceType) {
		this.dataSourceType = dataSourceType;
		return this;
	}
	
	public String getExtension() {
		return extension;
	}

	public DataSourceDescription setExtension(String extension) {
		this.extension = extension;
		return this;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public DataSourceDescription setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
		return this;
	}

	public Properties getProperties() {
		return configuration.getProperties();
	}
	
	public String getSchema() {
		return schema;
	}
	
	public DataSourceDescription setSchema(String schemaName) {
		schema = schemaName;
		return this;
	}
}
