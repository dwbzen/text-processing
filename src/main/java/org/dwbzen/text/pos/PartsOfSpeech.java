package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Parts of speech as a JSON structure. This will replace the single-character codes currently
* used in POS files.
* @author don_bacon
*/
public class PartsOfSpeech implements IJson {

	private static final long serialVersionUID = 1L;
	private static Map<String, PartOfSpeech> legacyPosMapping = new TreeMap<String, PartOfSpeech>();
	
	@JsonProperty	private List<PartOfSpeech> partsOfSpeech = new ArrayList<>();
	
	
	/**
	 * @return PartsOfSpeech instance
	 *
	 */
	public static PartsOfSpeech newInstance()  {
		PartsOfSpeech pos = new PartsOfSpeech();
		return pos;
	}
	
	public PartsOfSpeech()  {

	}

	public static Map<String, String> partsOfSpeechLegacy = new HashMap<String, String>();
	static  {
		partsOfSpeechLegacy.put("N", "Noun");
		partsOfSpeechLegacy.put("L", "Proper Noun");
		partsOfSpeechLegacy.put("M", "Male first name");
		partsOfSpeechLegacy.put("H", "Place names");
		partsOfSpeechLegacy.put("F", "Female first name");
		partsOfSpeechLegacy.put("S", "Surname");
		partsOfSpeechLegacy.put("l", "Improper Noun");
		partsOfSpeechLegacy.put("p", "Plural");
		partsOfSpeechLegacy.put("h", "Noun Phrase");
		partsOfSpeechLegacy.put("V", "Verb (participle)");
		partsOfSpeechLegacy.put("t", "Verb (transitive)");
		partsOfSpeechLegacy.put("i", "Verb (intransitive)");
		partsOfSpeechLegacy.put("A", "Adjective");
		partsOfSpeechLegacy.put("v", "Adverb");
		partsOfSpeechLegacy.put("C", "Conjunction");
		partsOfSpeechLegacy.put("P", "Preposition");
		partsOfSpeechLegacy.put("!", "Interjection");
		partsOfSpeechLegacy.put("r", "Pronoun");
		partsOfSpeechLegacy.put("D", "Definite Article");
		partsOfSpeechLegacy.put("I", "Indefinite Article");
		partsOfSpeechLegacy.put("o", "Nominative");
		partsOfSpeechLegacy.put("G", "Gerund");
		partsOfSpeechLegacy.put("Z", "Derrived noun");
		partsOfSpeechLegacy.put("z", "Derrived plural noun");
		partsOfSpeechLegacy.put("X", "Present tense verb");
		partsOfSpeechLegacy.put("x", "Past tense verb");
		partsOfSpeechLegacy.put("B", "Body part");
		partsOfSpeechLegacy.put("b", "Male body part");
		partsOfSpeechLegacy.put("d", "Female body part");
		partsOfSpeechLegacy.put("c", "Color");
		
	}

}
