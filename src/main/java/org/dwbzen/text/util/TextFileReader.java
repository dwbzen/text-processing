package org.dwbzen.text.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Text file reader. Can be used to get the entire file contents
 * or to iterate by line.
 * 
 * @author don_bacon
 *
 */
public class TextFileReader {
	private List<String> lines = new ArrayList<String>();
	private Character delimiter = null;
	private String endOfLine = null;
	private String fileName = null;
	private int minimumLength = 1;
	private int maximumLength = 20;
	private BufferedReader inputFileReader = null;
	private Reader reader = null;
	
	static Character delim = ' ';
	static String eolString = "";
	
	protected TextFileReader(String inputFile, Character cdelim, String eol) throws FileNotFoundException {
		setDelimiter(cdelim);
		setEndOfLine(eol);
		fileName = inputFile;
		try {
			openFile();
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + inputFile);
			throw(e);
		}
	}
	
	public static TextFileReader getInstance(String inputFile, Character cdelim, String eol) throws FileNotFoundException {
		return new TextFileReader(inputFile, cdelim, eol);
	}
	
	public static TextFileReader getInstance(String inputFile, String eol) throws FileNotFoundException {
		return new TextFileReader(inputFile, delim, eol);
	}
	public static TextFileReader getInstance(String inputFile) throws FileNotFoundException {
		return new TextFileReader(inputFile, delim, eolString);
	}

	/**
	 * Gets the contents of a text file as a single String.
	 * Appends the set delim Character and eolString (if not null) after trimming the line.
	 * Skips lines having "#" as the first character
	 * @param inputFile the file to read. If null, reads from STDIN.
	 * @return String text of the entire file, lines separated by configured delimiter (normally SPACE)
	 * @throws FileNotFoundException 
	 * 
	 */
	public String getFileText() throws FileNotFoundException, IOException {
		readFileLines();
		StringBuilder sb = new StringBuilder();
		for(String line : lines) {
			if(!line.startsWith("#")) {
				sb.append(line);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Gets the contents of a file as a List<String>  where each List element is a physical line in the file.
	 * Appends the set delim Character and eolString (if not null) after trimming the line.
	 * @param inputFile
	 * @return
	 * @throws IOException
	 */
	public List<String> getFileLines() throws IOException {
		if(lines.isEmpty()) {
			readFileLines();
		}
		return lines;
	}
	
	/**
	 * Gets the next line from the previously opened file.
	 * This does not check for minimum length, it just gets the next line.
	 * It does not append the delimiter character at the end (typically a space),
	 * but will append the endOfLine string if there is one.
	 * The line is trimmed of extra spaces.
	 * @return String the next line or null if no more lines
	 */
	public String getNextLine() {
		String line = null;
		StringBuilder sb = null;
		try {
			if((line = inputFileReader.readLine()) != null) {
				sb = new StringBuilder(line.trim());
				if(endOfLine != null && endOfLine.length()>0) {
					sb.append(endOfLine);
				}
			}
			else {
				inputFileReader.close();
			}
		} catch (IOException e) {
			System.err.println("Could not getNextLine from file");
		}
		return (sb != null) ? sb.toString() : null;
	}
	
	private void readFileLines() throws IOException {
		StringBuilder sb = null;
		String line;
		while((line = inputFileReader.readLine()) != null) {
			if(line.length() >= minimumLength) { 
				sb = new StringBuilder(line.trim());
				sb.append(delimiter);
				if(endOfLine != null) {
					sb.append(endOfLine);
				}
				lines.add(sb.toString()); 
			}
		}
		inputFileReader.close();
	}
	
	private void openFile() throws FileNotFoundException {
		reader = (fileName == null || fileName.equalsIgnoreCase("System.in") || fileName.equalsIgnoreCase("stdin")) 
					?  new InputStreamReader(System.in) : new FileReader(fileName);
		inputFileReader = new BufferedReader(reader);
	}
	
	public static void main(String... args) throws IOException {
		String filename = args[0];
		TextFileReader reader = TextFileReader.getInstance(filename);
		String text = reader.getFileText();
		System.out.println(text);
		System.out.println("length: " + text.length());
		
		TextFileReader reader2 = TextFileReader.getInstance(filename, "___\n");
		text = reader2.getFileText();
		System.out.println(text);
		System.out.println("length: " + text.length());

		List<String> lines = reader.getFileLines();
		System.out.println("#lines: " + reader2.size());
		int lnum = 1;
		for(String line : lines) {
			System.out.println(lnum + ". " + line);
			lnum++;
		}
		TextFileReader reader3 = TextFileReader.getInstance(filename, "\n");
		String line;
		lnum = 1;
		while((line = reader3.getNextLine()) != null) {
			System.out.println(lnum + ". " + line);
			lnum++;
		}
		
	}

	public List<String> getLines() {
		return lines;
	}

	public Character getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(Character delimiter) {
		this.delimiter = delimiter;
	}

	public String getEndOfLine() {
		return endOfLine;
	}

	public void setEndOfLine(String endOfLine) {
		this.endOfLine = endOfLine;
	}

	public String getFileName() {
		return fileName;
	}

	public int getMinimumLength() {
		return minimumLength;
	}

	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}

	public int getMaximumLength() {
		return maximumLength;
	}

	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

	public int size() {
		return lines.size();
	}
}
