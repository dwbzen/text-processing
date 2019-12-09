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

	public static Map<String, String> partsOfSpeechLegacy = new HashMap<String, String>();
	// Maps single-character legacy part of speech to PartOfSpeech
	public static Map<String, PartOfSpeech> legacyPosMapping = new TreeMap<String, PartOfSpeech>();
	
	@JsonProperty	private List<PartOfSpeech> partsOfSpeech = new ArrayList<>();
	
	
	/**
	 * @return fully-populated PartsOfSpeech instance
	 *
	 */
	public static PartsOfSpeech newInstance()  {
		PartsOfSpeech partsOfSpeech = new PartsOfSpeech();
		for(PartOfSpeech pos : legacyPosMapping.values()) {
			partsOfSpeech.addPartOfSpeech(pos);
		}
		return partsOfSpeech;
	}
	
	public PartsOfSpeech()  {
	}

	public List<PartOfSpeech> getPartsOfSpeech() {
		return partsOfSpeech;
	}

	public boolean addPartOfSpeech(PartOfSpeech pos) {
		return partsOfSpeech.add(pos);
	}
	
	
	  @Override 
	  public String toJson(boolean pretty) { 
		  StringBuilder sb = new
		  StringBuilder("{\n"); sb.append(" \"partsOfSpeech\" : [\n"); for(PartOfSpeech
		  pos : legacyPosMapping.values()) { sb.append(pos.toJson(pretty));
		  sb.append(",\n"); } sb.deleteCharAt(sb.lastIndexOf(","));
		  sb.append("  ]\n}"); return sb.toString(); 
	  }

	/*
	 * PartOfSpeech that have no standard legacy code, by convention new additions are assigned
	 * a code from unused in legacy in alpha order (except O and o)
	 * Unused in legacy: E, J, K, O, Q, R, T, U, W, Y, a, e, f, g, j, k, m, n, q, s, u, w, y
	 */
	static  {
		partsOfSpeechLegacy.put("!", "Interjection");
		partsOfSpeechLegacy.put("A", "Adjective");
		partsOfSpeechLegacy.put("B", "Body part");
		partsOfSpeechLegacy.put("C", "Conjunction");
		partsOfSpeechLegacy.put("D", "Definite Article");
		partsOfSpeechLegacy.put("E", "Number name");
		partsOfSpeechLegacy.put("F", "Female first name");
		partsOfSpeechLegacy.put("G", "Gerund");
		partsOfSpeechLegacy.put("H", "Place names");
		partsOfSpeechLegacy.put("I", "Indefinite Article");
		partsOfSpeechLegacy.put("J", "Number adjective");
		partsOfSpeechLegacy.put("K", "Ordinal number");
		partsOfSpeechLegacy.put("L", "Proper Noun");
		partsOfSpeechLegacy.put("M", "Male first name");
		partsOfSpeechLegacy.put("N", "Noun");
		partsOfSpeechLegacy.put("P", "Preposition");
		partsOfSpeechLegacy.put("Q", "City");
		partsOfSpeechLegacy.put("R", "Country");
		partsOfSpeechLegacy.put("S", "Surname");
		partsOfSpeechLegacy.put("T", "Food/Beveage");
		partsOfSpeechLegacy.put("V", "Verb (participle)");
		partsOfSpeechLegacy.put("W", "Digit19");
		partsOfSpeechLegacy.put("X", "Present tense verb");
		partsOfSpeechLegacy.put("Z", "Derrived noun");
		partsOfSpeechLegacy.put("b", "Male body part");
		partsOfSpeechLegacy.put("c", "Color");
		partsOfSpeechLegacy.put("d", "Female body part");
		partsOfSpeechLegacy.put("h", "Noun Phrase");
		partsOfSpeechLegacy.put("i", "Verb (intransitive)");
		partsOfSpeechLegacy.put("l", "Improper Noun");
		partsOfSpeechLegacy.put("o", "Nominative");
		partsOfSpeechLegacy.put("p", "Plural");
		partsOfSpeechLegacy.put("r", "Pronoun");
		partsOfSpeechLegacy.put("t", "Verb (transitive)");
		partsOfSpeechLegacy.put("v", "Adverb");
		partsOfSpeechLegacy.put("w", "Digit09");
		partsOfSpeechLegacy.put("x", "Past tense verb");
		partsOfSpeechLegacy.put("z", "Derrived plural noun");

		// PosCategory category, String code, String name, String description, String parent
		legacyPosMapping.put("!", new PartOfSpeech("interjection", PosCategory.interjection, "!", "Interjection"));
		legacyPosMapping.put("A", new PartOfSpeech("adjective", PosCategory.adjective, "A",  "modifies a noun or pronoun"));
		legacyPosMapping.put("B", new PartOfSpeech("bodyPart", PosCategory.noun, "B",  "body part", "noun"));
		legacyPosMapping.put("C", new PartOfSpeech("conjunction", PosCategory.conjunction, "C", "joins two phrases"));
		legacyPosMapping.put("D", new PartOfSpeech("definiteArticle", PosCategory.determiner, "D", "determiners are the new articles"));
		legacyPosMapping.put("E", new PartOfSpeech("numberName", PosCategory.noun, "E", "cardinal number or word representing a number", "noun"));
		legacyPosMapping.put("F", new PartOfSpeech("femaleName", PosCategory.noun, "F",  "Female Name", "noun"));
		legacyPosMapping.put("G", new PartOfSpeech("gerund", PosCategory.noun, "G", "Gerund", "noun"));
		legacyPosMapping.put("H", new PartOfSpeech("placeName", PosCategory.noun, "H",  "Place Name", "noun"));
		legacyPosMapping.put("I", new PartOfSpeech("indefiniteArticle", PosCategory.determiner, "I", "determiners are the new articles"));
		legacyPosMapping.put("J", new PartOfSpeech("number", PosCategory.adjective, "J", "cardinal number as an adjective", "adjective"));
		legacyPosMapping.put("K", new PartOfSpeech("ordinal", PosCategory.adjective, "K", "ordinal number or word", "adjective"));
		legacyPosMapping.put("L", new PartOfSpeech("properNoun", PosCategory.noun, "L",  "Proper Noun", "noun"));
		legacyPosMapping.put("M", new PartOfSpeech("maleName", PosCategory.noun, "M",  "Male Name", "noun"));
		legacyPosMapping.put("N", new PartOfSpeech("noun", PosCategory.noun, "N", "a word or lexical item denoting any abstract or concrete entity"));
		legacyPosMapping.put("P", new PartOfSpeech("preposition", PosCategory.preposition, "P", "Preposition"));
		legacyPosMapping.put("Q", new PartOfSpeech("cityName", PosCategory.noun, "Q",  "City name", "placeName"));
		legacyPosMapping.put("R", new PartOfSpeech("countryName", PosCategory.noun, "R",  "Country name", "placeName"));
		legacyPosMapping.put("S", new PartOfSpeech("surname", PosCategory.noun, "S",  "Last Name", "noun"));
		legacyPosMapping.put("T", new PartOfSpeech("food", PosCategory.noun, "T",  "Food/beverage name", "noun"));
		legacyPosMapping.put("V", new PartOfSpeech("participleVerb", PosCategory.verb, "V", "Verb (participle)", "verb"));
		legacyPosMapping.put("W", new PartOfSpeech("digit19", PosCategory.noun, "W", "Random digit 1 to 9", "noun"));
		legacyPosMapping.put("X", new PartOfSpeech("presentTenseVerb", PosCategory.verb, "X", "Verb (present tense)", "verb"));
		legacyPosMapping.put("Z", new PartOfSpeech("derrivedNoun", PosCategory.noun, "Z",  "Made-up from a legit noun", "noun"));
		legacyPosMapping.put("b", new PartOfSpeech("maleBodyPart", PosCategory.noun, "b",  "male body part", "bodyPart"));
		legacyPosMapping.put("c", new PartOfSpeech("color", PosCategory.adjective, "c",  "adjective representing a color", "adjective"));
		legacyPosMapping.put("d", new PartOfSpeech("femaleBodyPart", PosCategory.noun, "d",  "female body part", "bodyPart"));
		legacyPosMapping.put("h", new PartOfSpeech("nounPhrase", PosCategory.noun, "h", "Noun Phrase", "noun"));
		legacyPosMapping.put("i", new PartOfSpeech("intransitiveVerb", PosCategory.verb, "i", "Verb (intransitive)", "verb"));
		legacyPosMapping.put("l", new PartOfSpeech("improperNoun", PosCategory.noun, "l", "Improper Noun", "noun"));
		legacyPosMapping.put("o", new PartOfSpeech("nominative", PosCategory.noun, "o", "Nominative", "noun"));
		legacyPosMapping.put("p", new PartOfSpeech("plural", PosCategory.noun, "p", "Plural Noun", "noun"));
		legacyPosMapping.put("r", new PartOfSpeech("pronoun", PosCategory.pronoun, "r", "Pronoun"));
		legacyPosMapping.put("t", new PartOfSpeech("transitiveVerb", PosCategory.verb, "t", "Verb (transitive)", "verb"));
		legacyPosMapping.put("v", new PartOfSpeech("adverb", PosCategory.adverb, "v", "a modifier of an adjective, verb, or another adverb"));
		legacyPosMapping.put("w", new PartOfSpeech("digit09", PosCategory.noun, "w", "Random digit 0 to 9", "noun"));
		legacyPosMapping.put("x", new PartOfSpeech("pastTenseVerb", PosCategory.verb, "x", "Verb (past tense)", "verb"));
		legacyPosMapping.put("z", new PartOfSpeech("derrivedPluralNoun", PosCategory.noun, "z",  "Made-up from a legit plural noun", "noun"));
	}

	/**
	 * Produces and writes to stdout the JSON representation of mapped PartsOfSpeech.
	 * @param strings
	 */
	public static void main(String...strings) {
		PartsOfSpeech partsOfSpeech = PartsOfSpeech.newInstance();
		System.out.println(partsOfSpeech.toJson(true));
		PartOfSpeech pos = legacyPosMapping.get("F");
		
		System.out.println("\nCheck deserialization");
		String posJson = pos.toJson();
		System.out.println(posJson);
		PartOfSpeech pos2 = null;
		try {
			pos2 = IJson.mapper.readValue(posJson, PartOfSpeech.class);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		System.out.println(pos2.toJson());
	}
}
