package org.dwbzen.text.pos;

import org.dwbzen.common.util.IJson;
import org.dwbzen.common.util.INameable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates an English part of speech such a a noun, verb, etc.
 * including derived forms.</br>
 * <dl>
 * <dt>category</dt>
 * <dd>one of the 9 parts of speech of English grammar.</dd>
 * <dt>partOfSpeechCode</dt>
 * <dd>legacy single-character code</dd>
 * <dt>name</dt><dd>the unique name or key of this POS</dd>
 * <dt>derivedFrom></dt><dd>the base POS if this is derived from another POS, or blank if not</dd>
 * <dt>qualifier</dt><dd>qualifies this POS, for example adjective:color</dd>
 * </dl>
 * 
 * @author don_bacon
 * @see https://en.wikipedia.org/wiki/Part_of_speech
 */
public class PartOfSpeech implements IJson, INameable {
	private static final long serialVersionUID = 1L;

	public static enum PosCategory {noun, verb, adjective, adverb, pronoun, preposition, conjunction, interjection, determiner};
	
	@JsonProperty("category")	private PosCategory category = null;
	@JsonProperty("partOfSpeechCode")	private String code = null;
	@JsonProperty	private String name = null;
	@JsonProperty	private String description = null;
	@JsonProperty	private String derivedFrom = null;
	@JsonProperty	private String qualifier = null;
	
	public PartOfSpeech(PosCategory category, String code, String name, String description, String derived, String qualifier) {
		this.category = category;
		this.code = code;
		this.name = name;
		this.description = description;
		this.derivedFrom = derived;
		this.qualifier = qualifier;
	}
	public PartOfSpeech(PosCategory category, String code, String name, String description) {
		this(category, code, name, description, "", "");
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
	public String getDerivedFrom() {
		return derivedFrom;
	}
	public void setDerivedFrom(String derivedFrom) {
		this.derivedFrom = derivedFrom;
	}
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	@Override
	public void setName(String name) {
		this.name = name;		
	}

	@Override
	public String getName() {
		return name;
	}
}
