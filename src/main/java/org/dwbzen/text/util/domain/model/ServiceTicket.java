package org.dwbzen.text.util.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import mathlib.util.IJson;

public class ServiceTicket implements IJson {

	private static final long serialVersionUID = -4515187462233738568L;
	
	@JsonProperty	String id = null;
	@JsonProperty	String summary = null;
	@JsonIgnore		private String resourceString = null;	// comma-separated list of userIDs or 'na'
	private List<String> resourceList = new ArrayList<String>();
	
	
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

	public String getResourceString() {
		return resourceString;
	}

	@JsonSetter("resources")
	public void setResources(String resources) {
		resourceString = resources;
		String[] resArray = resources.split(",");
		for(String s : resArray) {
			resourceList.add(s);
		}
	}
	
	@JsonGetter("resources")
	public List<String> getResourceList() {
		return resourceList;
	}
	
	public String getResources() {
		return resourceString;
	}
	
}
