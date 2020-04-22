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

	public static Map<String, String> PartsOfSpeechMap = new HashMap<String, String>();
	// Maps single-character legacy part of speech to PartOfSpeech
	public static Map<String, PartOfSpeech> PartsOfSpeechDefinitionMap = new TreeMap<String, PartOfSpeech>();
	public static final int MAX_TAGS = 10;
	
	@JsonProperty	private List<PartOfSpeech> partsOfSpeech = new ArrayList<>();
	
	/**
	 * @return fully-populated PartsOfSpeech instance
	 *
	 */
	public static PartsOfSpeech newInstance()  {
		PartsOfSpeech partsOfSpeech = new PartsOfSpeech();
		for(PartOfSpeech pos : PartsOfSpeechDefinitionMap.values()) {
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
		  for(PartOfSpeech pos : PartsOfSpeechDefinitionMap.values()) { 
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
		PartsOfSpeechMap.put("!", "Interjection");
		PartsOfSpeechMap.put("A", "Adjective");
		PartsOfSpeechMap.put("B", "Body part");
		PartsOfSpeechMap.put("C", "Conjunction");
		PartsOfSpeechMap.put("D", "Definite Article");
		PartsOfSpeechMap.put("E", "Number name");
		PartsOfSpeechMap.put("F", "Female first name");
		PartsOfSpeechMap.put("G", "Gerund");
		PartsOfSpeechMap.put("H", "Place names");
		PartsOfSpeechMap.put("I", "Indefinite Article");
		PartsOfSpeechMap.put("J", "Number adjective");
		PartsOfSpeechMap.put("K", "Ordinal number");
		PartsOfSpeechMap.put("L", "Proper Noun");
		PartsOfSpeechMap.put("M", "Male first name");
		PartsOfSpeechMap.put("N", "Noun");
		PartsOfSpeechMap.put("P", "Preposition");
		PartsOfSpeechMap.put("Q", "City");
		PartsOfSpeechMap.put("R", "Country");
		PartsOfSpeechMap.put("S", "Surname");
		PartsOfSpeechMap.put("T", "Food/Beveage");
		PartsOfSpeechMap.put("V", "Verb (participle)");
		PartsOfSpeechMap.put("W", "Digit19");
		PartsOfSpeechMap.put("X", "Present tense verb");
		PartsOfSpeechMap.put("Z", "Derrived noun");
		PartsOfSpeechMap.put("b", "Male body part");
		PartsOfSpeechMap.put("c", "Color");
		PartsOfSpeechMap.put("d", "Female body part");
		PartsOfSpeechMap.put("h", "Noun Phrase");
		PartsOfSpeechMap.put("i", "Verb (intransitive)");
		PartsOfSpeechMap.put("l", "Improper Noun");
		PartsOfSpeechMap.put("o", "Nominative");
		PartsOfSpeechMap.put("p", "Plural");
		PartsOfSpeechMap.put("r", "Pronoun");
		PartsOfSpeechMap.put("t", "Verb (transitive)");
		PartsOfSpeechMap.put("v", "Adverb");
		PartsOfSpeechMap.put("w", "Digit09");
		PartsOfSpeechMap.put("x", "Past tense verb");
		PartsOfSpeechMap.put("z", "Derrived plural noun");
		
		// escaped PartsOfSpeech - n-character (n>1) code delimited by ` character: `sp` for example. Only the code is given.
		PartsOfSpeechMap.put("`sp`", "Subject phrase"); 		// as in "The revenge of the..."
		PartsOfSpeechMap.put("`op`", "Objective phrase");		// as in "... with a whip"
		PartsOfSpeechMap.put("`np`", "Noun phrase");			// equivalent to 'h' part of speech
		/*
		 *  Add tags #00 through #10. If U need more change the MAX_TAGS.
		 */
		for(int i=1; i<MAX_TAGS; i++) {
			String tag = (i<=9) ? "#0" + Integer.valueOf(i) : "#" + Integer.valueOf(i);
			PartsOfSpeechMap.put(tag, "Tag"+tag);
		}
		PartsOfSpeechDefinitionMap.put("!", new PartOfSpeech("interjection", PosCategory.interjection, "!", "Interjection"));
		PartsOfSpeechDefinitionMap.put("A", new PartOfSpeech("adjective", PosCategory.adjective, "A",  "modifies a noun or pronoun"));
		PartsOfSpeechDefinitionMap.put("B", new PartOfSpeech("bodyPart", PosCategory.noun, "B",  "body part", "noun"));
		PartsOfSpeechDefinitionMap.put("C", new PartOfSpeech("conjunction", PosCategory.conjunction, "C", "joins two phrases"));
		PartsOfSpeechDefinitionMap.put("D", new PartOfSpeech("definiteArticle", PosCategory.determiner, "D", "determiners are the new articles"));
		PartsOfSpeechDefinitionMap.put("E", new PartOfSpeech("numberName", PosCategory.noun, "E", "cardinal number or word representing a number", "noun"));
		PartsOfSpeechDefinitionMap.put("F", new PartOfSpeech("femaleName", PosCategory.noun, "F",  "Female Name", "noun"));
		PartsOfSpeechDefinitionMap.put("G", new PartOfSpeech("gerund", PosCategory.noun, "G", "Gerund", "noun"));
		PartsOfSpeechDefinitionMap.put("H", new PartOfSpeech("placeName", PosCategory.noun, "H",  "Place Name", "noun"));
		PartsOfSpeechDefinitionMap.put("I", new PartOfSpeech("indefiniteArticle", PosCategory.determiner, "I", "determiners are the new articles"));
		PartsOfSpeechDefinitionMap.put("J", new PartOfSpeech("number", PosCategory.adjective, "J", "cardinal number as an adjective", "adjective"));
		PartsOfSpeechDefinitionMap.put("K", new PartOfSpeech("ordinal", PosCategory.adjective, "K", "ordinal number or word", "adjective"));
		PartsOfSpeechDefinitionMap.put("L", new PartOfSpeech("properNoun", PosCategory.noun, "L",  "Proper Noun", "noun"));
		PartsOfSpeechDefinitionMap.put("M", new PartOfSpeech("maleName", PosCategory.noun, "M",  "Male Name", "noun"));
		PartsOfSpeechDefinitionMap.put("N", new PartOfSpeech("noun", PosCategory.noun, "N", "a word or lexical item denoting any abstract or concrete entity"));
		PartsOfSpeechDefinitionMap.put("P", new PartOfSpeech("preposition", PosCategory.preposition, "P", "Preposition"));
		PartsOfSpeechDefinitionMap.put("Q", new PartOfSpeech("cityName", PosCategory.noun, "Q",  "City name", "placeName"));
		PartsOfSpeechDefinitionMap.put("R", new PartOfSpeech("countryName", PosCategory.noun, "R",  "Country name", "placeName"));
		PartsOfSpeechDefinitionMap.put("S", new PartOfSpeech("surname", PosCategory.noun, "S",  "Last Name", "noun"));
		PartsOfSpeechDefinitionMap.put("T", new PartOfSpeech("food", PosCategory.noun, "T",  "Food/beverage name", "noun"));
		PartsOfSpeechDefinitionMap.put("V", new PartOfSpeech("participleVerb", PosCategory.verb, "V", "Verb (participle)", "verb"));
		PartsOfSpeechDefinitionMap.put("W", new PartOfSpeech("digit19", PosCategory.noun, "W", "Random digit 1 to 9", "noun"));
		PartsOfSpeechDefinitionMap.put("X", new PartOfSpeech("presentTenseVerb", PosCategory.verb, "X", "Verb (present tense)", "verb"));
		PartsOfSpeechDefinitionMap.put("Z", new PartOfSpeech("derrivedNoun", PosCategory.noun, "Z",  "Made-up from a legit noun", "noun"));
		PartsOfSpeechDefinitionMap.put("b", new PartOfSpeech("maleBodyPart", PosCategory.noun, "b",  "male body part", "bodyPart"));
		PartsOfSpeechDefinitionMap.put("c", new PartOfSpeech("color", PosCategory.adjective, "c",  "adjective representing a color", "adjective"));
		PartsOfSpeechDefinitionMap.put("d", new PartOfSpeech("femaleBodyPart", PosCategory.noun, "d",  "female body part", "bodyPart"));
		PartsOfSpeechDefinitionMap.put("h", new PartOfSpeech("nounPhrase", PosCategory.noun, "h", "Noun Phrase", "noun"));
		PartsOfSpeechDefinitionMap.put("i", new PartOfSpeech("intransitiveVerb", PosCategory.verb, "i", "Verb (intransitive)", "verb"));
		PartsOfSpeechDefinitionMap.put("l", new PartOfSpeech("improperNoun", PosCategory.noun, "l", "Improper Noun", "noun"));
		PartsOfSpeechDefinitionMap.put("o", new PartOfSpeech("nominative", PosCategory.noun, "o", "Nominative", "noun"));
		PartsOfSpeechDefinitionMap.put("p", new PartOfSpeech("plural", PosCategory.noun, "p", "Plural Noun", "noun"));
		PartsOfSpeechDefinitionMap.put("r", new PartOfSpeech("pronoun", PosCategory.pronoun, "r", "Pronoun"));
		PartsOfSpeechDefinitionMap.put("t", new PartOfSpeech("transitiveVerb", PosCategory.verb, "t", "Verb (transitive)", "verb"));
		PartsOfSpeechDefinitionMap.put("v", new PartOfSpeech("adverb", PosCategory.adverb, "v", "a modifier of an adjective, verb, or another adverb"));
		PartsOfSpeechDefinitionMap.put("w", new PartOfSpeech("digit09", PosCategory.noun, "w", "Random digit 0 to 9", "noun"));
		PartsOfSpeechDefinitionMap.put("x", new PartOfSpeech("pastTenseVerb", PosCategory.verb, "x", "Verb (past tense)", "verb"));
		PartsOfSpeechDefinitionMap.put("z", new PartOfSpeech("derrivedPluralNoun", PosCategory.noun, "z",  "Made-up from a legit plural noun", "noun"));
	
		// extended POS mapping does not include tags (#nn)
		PartsOfSpeechDefinitionMap.put("`sp`", new PartOfSpeech("subject phrase", PosCategory.noun, "sp", "A phrase that can start a sentence", "noun"));
		PartsOfSpeechDefinitionMap.put("`op`", new PartOfSpeech("object phrase", PosCategory.noun, "op", "An object phrase", "noun"));
		PartsOfSpeechDefinitionMap.put("`np`", new PartOfSpeech("noun phrase", PosCategory.noun, "np", "A general noun phrase", "noun"));		// equivalent to 'h' legacy
	}

	/**
	 * Produces and writes to stdout the JSON representation of mapped PartsOfSpeech.
	 * @param strings
	 */
	public static void main(String...strings) {
		PartsOfSpeech partsOfSpeech = PartsOfSpeech.newInstance();
		System.out.println(partsOfSpeech.toJson(true));
	}
}
