package org.dwbzen.text.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.pos.Dictionary;
import org.dwbzen.text.pos.PartsOfSpeech;
import org.dwbzen.text.pos.WordPos;


/**
 * Converts legacy=formatted pos files to new and improved JSON format.<br>
 * The new format includes multi-char parts of speech and introduction of tags.<br>
 * Examples -  <br>
 * legacy: "bloom	NitV`op`#01"<br>
 * JSON: { "word":"bloom", "pos":["N", "i", "t", "V", "op", "01"] }<br>
 * Legacy files use a tab (\t) character to separate the word from the parts of speech.
 * 
 * @author don_bacon
 *
 */
public class LegacyPosConverter {

	private static final Logger logger = LogManager.getLogger(PartsOfSpeech.class);
	
	private Dictionary dictionary = null;
	
	protected LegacyPosConverter() {}
	
	public LegacyPosConverter(String aName) {
		dictionary = new Dictionary(aName);
	}
	
	public void addPosFile(String filename) {
		loadWords(filename);
		dictionary.getSourceFiles().add(filename);
	}
	
	/**
	 * Writes the JSON Dictionary to std out.
	 */
	public void convert() {
		System.out.println(dictionary.toJson(true));
	}
	
	protected boolean loadWords(String aPosFile) {
		boolean okay = true;
		try {
			if(aPosFile == null) {
				readFileLines(new InputStreamReader(System.in));
			}
			else if(aPosFile.contains(":")) {	// absolute path, as in "C:/data/text/"
					readFileLines(new FileReader(aPosFile));
			}
			else {	// relative to POS_DIR=/reference/pos/<aposfile>
				InputStream is = this.getClass().getResourceAsStream(aPosFile);
				if(is != null) {
					try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {
						stream.forEach(s -> analyzeAndSaveWord(s));
					}
				}
				else {
					logger.error("Unable to open " + aPosFile);
					okay = false;
				}
			}
		}
		catch(FileNotFoundException e) {
			logger.error("File not found: " + aPosFile);
			okay = false;
		}
		catch(IOException e) {
			logger.error("Unable to read: " + aPosFile);
			okay = false;
		}
		return okay;
	}

	private int readFileLines(Reader reader)  throws IOException {
		int fileWords = 0;
		BufferedReader posFileReader = new BufferedReader(reader);
		String line = null;
		while((line = posFileReader.readLine()) != null) {
			analyzeAndSaveWord(line);
		}
		posFileReader.close();
		return fileWords;
	}
	
	private void analyzeAndSaveWord(String line) {
		int tab = line.indexOf('\t');
		if(tab <= 0) return;
		String word = line.substring(0,tab);
		String pos = line.substring(tab+1);
		if(word.length() <= 1) return;
		List<String> posList = PosUtil.parseInstance(pos, false);
		WordPos wordPos = new WordPos(word);
		wordPos.addAllPos(posList);
		dictionary.addWordPos(wordPos);
	}
	
	/**
	 * Usage LegacyPosConverter -name <dictionary Name> <file list>
	 * 
	 * @param args a list of files to convert comma-delimited.
	 */
	public static void main(String[] args) {
		String name = null;
		List<String> files = new ArrayList<>();
		LegacyPosConverter converter = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-name")) {
				name = args[++i];
			}
			else { 
				files.add(args[i]);
			}
		}
		if(name != null && files.size()>0) {
			converter = new LegacyPosConverter(name);
			for(String filename : files) {
				converter.addPosFile(filename);
			}
			converter.convert();
		}
		else {
			System.err.println("Missing name or no files specified");
		}
	}

}
