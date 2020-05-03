package org.dwbzen.text.pos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dwbzen.text.util.PosUtil;

/**
*
* @see org.dwbzen.text.pos.Dictionary
* @see org.dwbzen.text.pos.PartsOfSpeech
*/
public class PartsOfSpeechManager extends AbstractPartsOfSpeechManager {

	/**
	 * Command line arguments:
	 * -include flag1,flag2...	TODO: types of words to include (load) that are normally ignored
	 * 							uc  = words in all UPPER CASE
	 * 							num = words entirely numeric chars
	 * 							pn  = proper nouns, phrases
	 * 							comp= compound words (phrases)
	 * -filter pos1,pos2..		TODO: parts of speech to ignore (see above list)
	 * -show pos1,pos2..		show words having these parts of speech
	 * 
	 * 
	 */
	public static void main(String[] args) {
		PartsOfSpeechManager partsOfSpeechManager = null;
		List<String> posToIgnore = new ArrayList<>();
		List<String> wordsToInclude = new ArrayList<>();
		List<String> customFiles = new ArrayList<>();
		String[] posToShow = {};
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-filter")) {
				String[] sa = args[++i].split(",");
				for(int ind=0;ind<sa.length;ind++) { posToIgnore.add(sa[ind]); }
			}
			else if(args[i].equalsIgnoreCase("-include")) {
				String[] sa = args[++i].split(",");
				for(int ind=0;ind<sa.length;ind++) { wordsToInclude.add(sa[ind]); }
			}
			else if(args[i].equalsIgnoreCase("-show")) {
				posToShow = args[++i].split(",");
			}
			else {
				customFiles.add(args[i]);
			}
		}
		try {
			if(customFiles.size() == 0) {
				partsOfSpeechManager = PartsOfSpeechManager.newInstance();
			}
			else {
				partsOfSpeechManager = PartsOfSpeechManager.newInstance(customFiles);
			}
			
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		System.out.println(partsOfSpeechManager.getStats());
		if(posToShow.length > 0) {
			Map<String, List<String>> map = partsOfSpeechManager.getWordsForPos(posToShow);
			map.keySet().forEach(k -> {
				System.out.println(PartsOfSpeech.PartsOfSpeechMap.get(k));
				System.out.println(map.get(k));
			});
		}
	}
	
	/**
	 * Creates a new PartsOfSpeech that takes input from the configured parts of speech files
	 * Part Of Speech Database, July 23, 2000
	 * Compiled by Kevin Atkinson <kevina@users.sourceforge.net>
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public static PartsOfSpeechManager newInstance()throws IOException {
		PartsOfSpeechManager pos = new PartsOfSpeechManager();
		return pos;
	}
	
	public static PartsOfSpeechManager newInstance(List<String> files)throws IOException {
		PartsOfSpeechManager pos = new PartsOfSpeechManager(files);
		return pos;
	}
	
	protected PartsOfSpeechManager() throws IOException {
		configure();	// set configuration and load wordMap
	}
	
	protected PartsOfSpeechManager(List<String> files) throws IOException {
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
	 * Loads words from a parts of speech file, for example "3eslpos" or "C;\data\text\myPos" <br>
	 * File extension is not included but is provided by the concrete PartsOfSpeechManager as ".json" or ".txt"
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
