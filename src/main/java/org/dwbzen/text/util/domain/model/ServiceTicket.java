package org.dwbzen.text.util.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;

public class ServiceTicket implements IJson {

	private static final long serialVersionUID = -4515187462233738568L;
	
	@JsonProperty	String id = null;
	@JsonProperty	String summary = null;
	
	public ServiceTicket() {
		
	}
	
	public ServiceTicket(String theid, String asummary) {
		id = theid;
		summary = asummary;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
}
