package org.dwbzen.text.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.dwbzen.text.util.exception.InvalidDataSourceException;

/**
 * The DatSourceDescription for a TextFileDataSource must include the properties:<br>
 * filename: full path OR "System.in" or "stdin" to read from stdin (DataSourceType set to Text). Cannot be missing.
 * </p>
 * Optional properties:<br>
 * cacheContent:true (the default) or false<br>
 * wordDelimiter: specify if other than a space<br>
 * eol: specify if other than an empty String ""<br>
 * 
 * @author DBacon
 *
 */
public class TextFileDataSource implements IDataSource<String>,IDataFormatter<String> {

	private DataSourceDescription dataSourceDescription;
	private Properties dataSourceProperties = null;
	private DataSourceType dataSourceType = null;
	private String filename = null;
	private Character delimiter = null;
	private String endOfLine = null;
	private TextFileReader reader = null;
	private String fileText = null;
	private List<String> lines = null;;
	
	public TextFileDataSource(DataSourceDescription description) {
		dataSourceDescription = description;
		if(dataSourceDescription.getProperties() == null 
			|| !dataSourceDescription.getProperties().containsKey("filename")) {
			throw new InvalidDataSourceException("Missing filename");
		}
		else if(dataSourceDescription.getProperties().getProperty("filename").length() < 3) {
			throw new InvalidDataSourceException("Invalid filename");
		}
		dataSourceProperties = dataSourceDescription.getProperties();
		filename = dataSourceProperties.getProperty("filename");
		dataSourceType = filename.equalsIgnoreCase("System.in") || filename.equalsIgnoreCase("stdin")
				? DataSourceType.Text :  DataSourceType.TextFile;
		delimiter = dataSourceProperties.getProperty("wordDelimiter", " ").charAt(0);
		endOfLine = dataSourceProperties.getProperty("eol","");
		reader = TextFileReader.getInstance(filename, delimiter, endOfLine);
	}
	
	@Override
	public String getData() throws InvalidDataSourceException {
		try {
			fileText = format(reader.getFileText());
		}
		catch(FileNotFoundException e1) {
			throw new InvalidDataSourceException("File not found: " + filename);
		}
		catch(IOException e2) {
			throw new InvalidDataSourceException("IOException. Here's the details: " + e2.getMessage());
		}
		return fileText;
	}
	
	@Override
	public  List<String> getDataList() throws InvalidDataSourceException {
		try {
			lines = reader.getFileLines();
		}
		catch(FileNotFoundException e1) {
			throw new InvalidDataSourceException("File not found: " + filename);
		}
		catch(IOException e2) {
			throw new InvalidDataSourceException("IOException. Here's the details: " + e2.getMessage());
		}
		return lines;
	}

	@Override
	public DataSourceDescription getDataSourceDescription() {
		return dataSourceDescription;
	}
	
	@Override
	 public DataSourceType getDataSourceType() {
		return dataSourceType;
	}

	public String getFileText() {
		return fileText;
	}

	public void setFileText(String fileText) {
		this.fileText = fileText;
	}

	public String getFilename() {
		return filename;
	}

	public List<String> getLines() {
		return lines;
	}

	@Override
	public String format(String rawData) {
		return rawData;
	}

}
