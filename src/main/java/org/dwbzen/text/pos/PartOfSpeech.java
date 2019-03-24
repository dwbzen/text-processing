package org.dwbzen.text.pos;

import org.dwbzen.common.util.IJson;
import org.dwbzen.common.util.INameable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates an English part of speech such a a noun, verb, etc.
 * including derived forms.</br>
 * <dl>
 * <dt>name</dt><dd>the unique name or key of this POS</dd>
 * <dt>category</dt><dd>one of the 9 parts of speech of English grammar.</dd>
 * <dt>partOfSpeechCode</dt><dd>legacy single-character code</dd>
 * <dt>parent</dt><dd>the name of the base POS if this inherits from (is a subtype of) that POS, or blank if not.</br>
 * For example a Proper Noun is a subtype of Noun.</dd>
 * <dt>qualifies</dt><dd>qualifies (is an instance of) this POS, for example color is an adjective instance but not a type of adjective.</dd>
 * </dl>
 * A given POS can be child of and/or qualify a parent POS. For example place name qualifies proper noun which is a child of noun.</br>
 * The distinction between parent and qualifies is subjective and informational only.
 * 
 * @author don_bacon
 * @see https://en.wikipedia.org/wiki/Part_of_speech
 */
public class PartOfSpeech implements IJson, INameable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty	private String name = null;
	@JsonProperty	private PosCategory category = null;
	@JsonProperty	private String code = null;
	@JsonProperty	private String description = null;
	@JsonProperty	private String parent = null;
	@JsonProperty	private String qualifies = null;
	
	public PartOfSpeech(String name, PosCategory category, String code, String description, String parent, String qualifies) {
		this.category = category;
		this.code = code;
		this.name = name;
		this.description = description;
		this.parent = parent;
		this.qualifies = qualifies;
	}
	public PartOfSpeech(String name, PosCategory category, String code, String description, String parent) {
		this(name, category, code, description, parent, null);
	}
	public PartOfSpeech(String name, PosCategory category, String code, String description) {
		this(name, category, code, description, null, null);
	}
	public PartOfSpeech() {
		
	}
	
	public PosCategory getCategory() {
		return category;
	}
	public void setCategory(PosCategory category) {
		this.category = category;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getQualifies() {
		return qualifies;
	}
	public void setQualifies(String qualifies) {
		this.qualifies = qualifies;
	}
	@Override
	public void setName(String name) {
		this.name = name;		
	}
	
	
	  @Override 
	  public String toJson(boolean pretty) { 
		  String prefix = pretty ?  "    " : ""; String eol = pretty ? ",\n" : ","; 
		  StringBuilder sb = new  StringBuilder("  "); 
		  sb.append("{\"name\":").append("\"" + name +  "\"").append(eol); 
		  sb.append(prefix).append("\"category\":").append("\"" + category + "\"").append(eol);
		  sb.append(prefix).append("\"code\":").append("\"" + code + "\"").append(eol);
		  sb.append(prefix).append("\"description\":").append("\"" + description + "\"").append(eol); 
		  if(parent != null) {
			  sb.append(prefix).append("\"parent\":").append("\"" + parent +  "\"").append(eol); 
		  } 
		  if(qualifies != null) {
			  sb.append(prefix).append("\"qualifies\":").append("\"" + qualifies + "\"").append(eol); 
		  } 
		  sb.deleteCharAt(sb.lastIndexOf(",")); 
		  sb.append(pretty ? "  }" : "}"); 
		  return sb.toString(); 
	  }
	 

	@Override
	public String getName() {
		return name;
	}
}
