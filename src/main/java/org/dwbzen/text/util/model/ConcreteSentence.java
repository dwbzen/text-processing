package org.dwbzen.text.util.model;

import org.dwbzen.text.util.INameable;

import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;

public class ConcreteSentence implements INameable, IJson {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty		private String name = null;
	@JsonProperty		private String id = null;
	@JsonProperty		private String text = null;
			
	public ConcreteSentence(String text) {
		this.text = text;
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
