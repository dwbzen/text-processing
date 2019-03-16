package org.dwbzen.text.util.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.*;

public class Chapter implements Supplier<Optional<Sentence>>, IJson, INameable {

	private static final long serialVersionUID = -5391415347032021541L;
	@JsonProperty("title")	private String name = INameable.DEFAULT_NAME;
	@JsonProperty			private List<Sentence> sentences = new ArrayList<>();	// References to Sentences in this Chapter
	
	@JsonIgnore		private int currentIndex = -1;
	

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<Sentence> get() {
		// gets the next sentence
		currentIndex++;
		Optional<Sentence> sentence = (currentIndex > sentences.size()-1) ? Optional.empty() : Optional.of(sentences.get(currentIndex));
		return sentence;
	}
	
	public boolean add(Sentence e) {
		return sentences.add(e);
	}

}
