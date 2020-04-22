package org.dwbzen.text.pos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.Configuration;

/**
	Applies to legacy pos files only.<br>
	The format of each legacy POS entry is 
	<word><tab><POS tag(s)><unix newline><br>
	
	Where the POS tag is one or more of the following:</p>
	<code>
	
	N	Noun
	L   Proper noun (not tagged) - N and first letter upper case
	l   Improper noun (not tagged) - N, p and first letter lower case
	H	Place name (proper noun)
	M   Male person first name
	F   Female person first name
	S   Surname (last names)
	p	Plural Noun
	h	Noun Phrase
	V	Verb (usu participle)
	t	Verb (transitive)
	i	Verb (intransitive)
	A	Adjective
	v	Adverb
	C	Conjunction
	P	Preposition
	!	Interjection
	r	Pronoun
	D	Definite Article
	I	Indefinite Article
	o	Nominative
	c	Color
	w	Random digit 0 to 9
	W   Random digit 1 to 9
	
	Derived Forms
	-------------
	G   gerund (V or t + ing)
	Z   Adjectival verb (V or t + "er", as in taker, putter etc.)
	z   derived possessive noun (N or p + "er" )
	X	present tense verb (V, t)
	x	past tense verb (V, t)
	
	B	body part 
	b	body part - male    (optional tagged) added automatically for B
	d	body part - female  (optional tagged) added automatically for B
	</code>
	
	Unused in legacy: E, J, K, O, Q, R, T, U, Y, a, e, f, g, j, k, m, n, q, s, u, y
*
* @see  org.dwbzen.text.pos.Dictionary
*/
public class PartsOfSpeechManager extends AbstractPartsOfSpeechManager {

    private static final Logger logger = LogManager.getLogger(PartsOfSpeech.class);


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
		List<String> customFiles = new ArrayList();
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
	
	public Map<String, List<String>> getWordsForPos(String[] poss) {
		Map<String, List<String>> posWordMap = new TreeMap<>();
		for(String key : poss) {
			posWordMap.put(key, wordMap.get(key));
		}
		
		return posWordMap;
	}
	
	public String lookup(String word) {
		return posMap.get(word);
	}
	
	public List<String> getPosFiles() {
		return this.posFiles;
	}

	public List<String> getPosFileNames() {
		return posFiles;
	}
	
	public String getPosDir() {
		return posDir;
	}

	public void setPosDir(String posDir) {
		this.posDir = posDir;
	}

	public String getPosFile() {
		return posFile;
	}

	public void setPosFile(String posFile) {
		this.posFile = posFile;
	}


	@Override
	public IPartsOfSpeechManager getInstance()  {
		IPartsOfSpeechManager mgr = null;
		try {
			mgr = PartsOfSpeechManager.newInstance();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return mgr;
	}

	@Override
	public IPartsOfSpeechManager getInstance(List<String> posFiles) {
		IPartsOfSpeechManager mgr = null;
		try {
			mgr = PartsOfSpeechManager.newInstance(posFiles);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return mgr;
	}

	@Override
	public String getPosFileType() {
		return "txt";
	}
	
}
