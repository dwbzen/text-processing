package org.dwbzen.text.pos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;

import org.dwbzen.text.util.PosUtil;

/**
*
* @see org.dwbzen.text.pos.Dictionary
* @see org.dwbzen.text.pos.PartsOfSpeech
*/
public class LegacyPartsOfSpeechManager extends PartsOfSpeechManager {

	/**
	 * Creates a new PartsOfSpeech that takes input from the configured parts of speech files
	 * Part Of Speech Database, July 23, 2000
	 * Compiled by Kevin Atkinson <kevina@users.sourceforge.net>
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public static LegacyPartsOfSpeechManager newInstance()throws IOException {
		LegacyPartsOfSpeechManager pos = new LegacyPartsOfSpeechManager();
		return pos;
	}
	
	public static LegacyPartsOfSpeechManager newInstance(List<String> files)throws IOException {
		LegacyPartsOfSpeechManager pos = new LegacyPartsOfSpeechManager(files);
		return pos;
	}
	
	protected LegacyPartsOfSpeechManager() throws IOException {
		configure();	// set configuration and load wordMap
	}
	
	protected LegacyPartsOfSpeechManager(List<String> files) throws IOException {
		customPosFiles.addAll(files);
		configure();	// set configuration and load wordMap
	}
	
	/**
	 * Load all the configured pos file names to use. Names don't include extensions, ".txt" in this case.
	 */
	@Override
	public void configure()  {
		super.configure();
    }

	/**
	 * Loads words from a legacy parts of a speech file, for example "3eslpos" <br>
	 * File extension is added as ".txt"
	 * 
	 * @param posFileName
	 */
	@Override
	public int loadWords(String posFileName) {
		int fileWords = 0;
		String posFile = (posFileName == null) ? null : posFileName + getPosFileExtension();
		try {
			if(posFile == null) {
				readFileLines(new InputStreamReader(System.in));
			}
			else if(posFile.contains(":")) {	// like C:/data/text/
					readFileLines(new FileReader(posFile));
			}
			else {
				InputStream is = this.getClass().getResourceAsStream(posFile);
				if(is != null) {
					try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {
						stream.forEach(s -> analyzeAndSaveWord(s));
					}
				}
				else {
					logger.error("Unable to open " + posFile);
				}
			}
		}
		catch(FileNotFoundException e) {
			logger.error("File not found: " + posFile);
		}
		catch(IOException e) {
			logger.error("Unable to read: " + posFile);
		}
		
		logger.debug(fileWords + " words analyzed/saved " + posFile);
		return fileWords;
	}
	
	/**
	 * Analyzes and saves a word from legacy POS files.
	 */
	@Override
	public int analyzeAndSaveWord(String line) {
		int fileWords = 0;
		int tab = line.indexOf('\t');
		if(tab <= 0) return 0;
		String word = line.substring(0,tab);
		String pos = line.substring(tab+1);
		if(word.length() <= 1) return 0;
		List<String> posList = PosUtil.parseInstance(pos, false);
		for(String aPos : posList) {
			// a word can have multiple parts of speech
			boolean isproper = word.matches(PROPER_WORD);
			if(aPos.equals("N")) {
				if(isproper) {
					saveWord(word, "L");
				}
				else {
					saveWord(word, "l");
				}
			}
			else if(aPos.equals("B")) {
				saveWord(word, "d");
				saveWord(word, "b");
			}

			saveWord(word, aPos);
			fileWords++;
		}
		return fileWords;
	}
	
	@Override
	public String getPosFileType() {
		return "txt";
	}
	
}
