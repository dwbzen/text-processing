package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dwbzen.common.util.IJson;
import org.dwbzen.text.pos.PosCategory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Parts of speech as a JSON structure. This will replace the single-character codes currently
* used in POS files.
* @author don_bacon
*/
public class PartsOfSpeech implements IJson {

	private static final long serialVersionUID = 1L;
	public static Map<String, String> partsOfSpeechLegacy = new HashMap<String, String>();
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
	 


	static  {
		partsOfSpeechLegacy.put("N", "Noun");
		// PosCategory category, String code, String name, String description, String parent
		legacyPosMapping.put("N", new PartOfSpeech( "noun", PosCategory.noun, "N", "a word or lexical item denoting any abstract or concrete entity"));
		
		partsOfSpeechLegacy.put("L", "Proper Noun");
		legacyPosMapping.put("L", new PartOfSpeech("properNoun", PosCategory.noun, "L",  "Proper Noun", "noun"));
		
		partsOfSpeechLegacy.put("M", "Male first name");
		legacyPosMapping.put("M", new PartOfSpeech("maleName", PosCategory.noun, "M",  "Male Name", "noun"));
		
		partsOfSpeechLegacy.put("H", "Place names");
		legacyPosMapping.put("H", new PartOfSpeech("placeName", PosCategory.noun, "H",  "Place Name", "noun"));

		partsOfSpeechLegacy.put("F", "Female first name");
		legacyPosMapping.put("F", new PartOfSpeech("femaleName", PosCategory.noun, "F",  "Female Name", "noun"));
		
		partsOfSpeechLegacy.put("S", "Surname");
		legacyPosMapping.put("S", new PartOfSpeech("surname", PosCategory.noun, "S",  "Last Name", "noun"));

		partsOfSpeechLegacy.put("l", "Improper Noun");
		partsOfSpeechLegacy.put("p", "Plural");
		partsOfSpeechLegacy.put("h", "Noun Phrase");
		partsOfSpeechLegacy.put("V", "Verb (participle)");
		partsOfSpeechLegacy.put("t", "Verb (transitive)");
		partsOfSpeechLegacy.put("i", "Verb (intransitive)");
		
		partsOfSpeechLegacy.put("A", "Adjective");
		legacyPosMapping.put("A", new PartOfSpeech("adjective", PosCategory.adjective, "A",  "modifies a noun or pronoun"));
		
		partsOfSpeechLegacy.put("c", "Color");
		legacyPosMapping.put("c", new PartOfSpeech("color", PosCategory.adjective, "c",  "adjective representing a color", "adjective"));
		
		partsOfSpeechLegacy.put("v", "Adverb");
		legacyPosMapping.put("v", new PartOfSpeech("adverb", PosCategory.adverb, "v", "a modifier of an adjective, verb, or another adverb"));
		
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
		legacyPosMapping.put("B", new PartOfSpeech("bodyPart", PosCategory.noun, "B",  "body part", "noun"));

		partsOfSpeechLegacy.put("b", "Male body part");
		legacyPosMapping.put("b", new PartOfSpeech("maleBodyPart", PosCategory.noun, "b",  "male body part", "bodyPart"));

		partsOfSpeechLegacy.put("d", "Female body part");
		legacyPosMapping.put("d", new PartOfSpeech("femaleBodyPart", PosCategory.noun, "d",  "female body part", "bodyPart"));
		
		/*
		 * PartOfSpeech that have no standard legacy code, by convention they are assigned
		 * a numeric code starting at "1"
		 */
		legacyPosMapping.put("1", new PartOfSpeech("numberName", PosCategory.noun, "1", "cardinal number or word representing a number", "noun"));
		legacyPosMapping.put("2", new PartOfSpeech("number", PosCategory.adjective, "2", "cardinal number as an adjective", "adjective"));
		legacyPosMapping.put("3", new PartOfSpeech("ordinal", PosCategory.adjective, "3", "ordinal number or word", "adjective"));
		legacyPosMapping.put("4", new PartOfSpeech("cityName", PosCategory.noun, "4",  "City name", "placeName"));
		legacyPosMapping.put("5", new PartOfSpeech("countryName", PosCategory.noun, "5",  "Country name", "placeName"));
		
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
