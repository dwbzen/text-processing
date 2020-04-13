package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Parts of speech as a JSON structure. This replaces the single-character codes currently used in legacy POS files.<br>
* Tags (#nn) are not included since they technically do not indicate a part of speech.<br>
* PosMapping structure:
*  <ul>
  	<li>PosCategory category</li>
	<li>String legacyCode</li>
	<li>String name</li>
	<li>String description</li>
	<li>String parentPos (optional)</li> 
  </ul> 
* 
* @author don_bacon
*/
public class PartsOfSpeech implements IJson {

	public static Map<String, String> partsOfSpeechLegacy = new HashMap<String, String>();
	// Maps single-character legacy part of speech to PartOfSpeech
	public static Map<String, PartOfSpeech> PosMapping = new TreeMap<String, PartOfSpeech>();
	
	@JsonProperty	private List<PartOfSpeech> partsOfSpeech = new ArrayList<>();
	
	/**
	 * @return fully-populated PartsOfSpeech instance
	 *
	 */
	public static PartsOfSpeech newInstance()  {
		PartsOfSpeech partsOfSpeech = new PartsOfSpeech();
		for(PartOfSpeech pos : PosMapping.values()) {
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
		  StringBuilder("{\n"); sb.append(" \"partsOfSpeech\" : [\n"); 
		  for(PartOfSpeech pos : PosMapping.values()) { 
			  sb.append(pos.toJson(pretty));
			  sb.append(",\n");
		  } 
		  sb.deleteCharAt(sb.lastIndexOf(","));
		  sb.append("  ]\n}"); 
		return sb.toString(); 
	  }

	/*
	 * PartOfSpeech that have no standard legacy code, by convention new additions are assigned
	 * a code from unused in legacy in alpha order (except O and o)
	 * Unused in legacy: E, J, K, O, Q, R, S, T, U, W, Y, a, e, f, g, j, k, m, n, q, s, u, w, y
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
		
		// escaped PartsOfSpeech - n-character (n>1) code delimited by ` character: `sp` for example. Only the code is given.
		partsOfSpeechLegacy.put("`sp`", "Subject phrase"); 		// as in "The revenge of the..."
		partsOfSpeechLegacy.put("`op`", "Objective phrase");		// as in "... with a whip"
		partsOfSpeechLegacy.put("`np`", "Noun phrase");			// equivalent to 'h' part of speech

		/*
		 *  
		 */
		PosMapping.put("!", new PartOfSpeech("interjection", PosCategory.interjection, "!", "Interjection"));
		PosMapping.put("A", new PartOfSpeech("adjective", PosCategory.adjective, "A",  "modifies a noun or pronoun"));
		PosMapping.put("B", new PartOfSpeech("bodyPart", PosCategory.noun, "B",  "body part", "noun"));
		PosMapping.put("C", new PartOfSpeech("conjunction", PosCategory.conjunction, "C", "joins two phrases"));
		PosMapping.put("D", new PartOfSpeech("definiteArticle", PosCategory.determiner, "D", "determiners are the new articles"));
		PosMapping.put("E", new PartOfSpeech("numberName", PosCategory.noun, "E", "cardinal number or word representing a number", "noun"));
		PosMapping.put("F", new PartOfSpeech("femaleName", PosCategory.noun, "F",  "Female Name", "noun"));
		PosMapping.put("G", new PartOfSpeech("gerund", PosCategory.noun, "G", "Gerund", "noun"));
		PosMapping.put("H", new PartOfSpeech("placeName", PosCategory.noun, "H",  "Place Name", "noun"));
		PosMapping.put("I", new PartOfSpeech("indefiniteArticle", PosCategory.determiner, "I", "determiners are the new articles"));
		PosMapping.put("J", new PartOfSpeech("number", PosCategory.adjective, "J", "cardinal number as an adjective", "adjective"));
		PosMapping.put("K", new PartOfSpeech("ordinal", PosCategory.adjective, "K", "ordinal number or word", "adjective"));
		PosMapping.put("L", new PartOfSpeech("properNoun", PosCategory.noun, "L",  "Proper Noun", "noun"));
		PosMapping.put("M", new PartOfSpeech("maleName", PosCategory.noun, "M",  "Male Name", "noun"));
		PosMapping.put("N", new PartOfSpeech("noun", PosCategory.noun, "N", "a word or lexical item denoting any abstract or concrete entity"));
		PosMapping.put("P", new PartOfSpeech("preposition", PosCategory.preposition, "P", "Preposition"));
		PosMapping.put("Q", new PartOfSpeech("cityName", PosCategory.noun, "Q",  "City name", "placeName"));
		PosMapping.put("R", new PartOfSpeech("countryName", PosCategory.noun, "R",  "Country name", "placeName"));
		PosMapping.put("S", new PartOfSpeech("surname", PosCategory.noun, "S",  "Last Name", "noun"));
		PosMapping.put("T", new PartOfSpeech("food", PosCategory.noun, "T",  "Food/beverage name", "noun"));
		PosMapping.put("V", new PartOfSpeech("participleVerb", PosCategory.verb, "V", "Verb (participle)", "verb"));
		PosMapping.put("W", new PartOfSpeech("digit19", PosCategory.noun, "W", "Random digit 1 to 9", "noun"));
		PosMapping.put("X", new PartOfSpeech("presentTenseVerb", PosCategory.verb, "X", "Verb (present tense)", "verb"));
		PosMapping.put("Z", new PartOfSpeech("derrivedNoun", PosCategory.noun, "Z",  "Made-up from a legit noun", "noun"));
		PosMapping.put("b", new PartOfSpeech("maleBodyPart", PosCategory.noun, "b",  "male body part", "bodyPart"));
		PosMapping.put("c", new PartOfSpeech("color", PosCategory.adjective, "c",  "adjective representing a color", "adjective"));
		PosMapping.put("d", new PartOfSpeech("femaleBodyPart", PosCategory.noun, "d",  "female body part", "bodyPart"));
		PosMapping.put("h", new PartOfSpeech("nounPhrase", PosCategory.noun, "h", "Noun Phrase", "noun"));
		PosMapping.put("i", new PartOfSpeech("intransitiveVerb", PosCategory.verb, "i", "Verb (intransitive)", "verb"));
		PosMapping.put("l", new PartOfSpeech("improperNoun", PosCategory.noun, "l", "Improper Noun", "noun"));
		PosMapping.put("o", new PartOfSpeech("nominative", PosCategory.noun, "o", "Nominative", "noun"));
		PosMapping.put("p", new PartOfSpeech("plural", PosCategory.noun, "p", "Plural Noun", "noun"));
		PosMapping.put("r", new PartOfSpeech("pronoun", PosCategory.pronoun, "r", "Pronoun"));
		PosMapping.put("t", new PartOfSpeech("transitiveVerb", PosCategory.verb, "t", "Verb (transitive)", "verb"));
		PosMapping.put("v", new PartOfSpeech("adverb", PosCategory.adverb, "v", "a modifier of an adjective, verb, or another adverb"));
		PosMapping.put("w", new PartOfSpeech("digit09", PosCategory.noun, "w", "Random digit 0 to 9", "noun"));
		PosMapping.put("x", new PartOfSpeech("pastTenseVerb", PosCategory.verb, "x", "Verb (past tense)", "verb"));
		PosMapping.put("z", new PartOfSpeech("derrivedPluralNoun", PosCategory.noun, "z",  "Made-up from a legit plural noun", "noun"));
	
		// extended POS mapping
		PosMapping.put("`sp`", new PartOfSpeech("subject phrase", PosCategory.noun, "sp", "A phrase that can start a sentence", "noun"));
		PosMapping.put("`op`", new PartOfSpeech("object phrase", PosCategory.noun, "op", "An object phrase", "noun"));
		PosMapping.put("`np`", new PartOfSpeech("noun phrase", PosCategory.noun, "np", "A general noun phrase", "noun"));		// equivalent to 'h' legacy
	}

	/**
	 * Produces and writes to stdout the JSON representation of mapped PartsOfSpeech.
	 * @param strings
	 */
	public static void main(String...strings) {
		PartsOfSpeech partsOfSpeech = PartsOfSpeech.newInstance();
		System.out.println(partsOfSpeech.toJson(true));
		PartOfSpeech pos = PosMapping.get("F");
		
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
