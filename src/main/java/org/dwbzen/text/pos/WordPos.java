package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WordPos implements IJson {

	@JsonProperty String word = null;
	@JsonProperty List<String> pos = new ArrayList<>();
	
	public WordPos() { }
	
	public WordPos(String aWord) {
		word = aWord;
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public List<String> getPos() {
		return pos;
	}
	
	public void setPos(List<String> pos) {
		this.pos = pos;
	}

	public void addPos(String aPos) {
		pos.add(aPos);
	}
	
	public void addAllPos(List<String> posList) {
		pos.addAll(posList);
	}

	public static void main(String[] args) {
		WordPos wp = new WordPos("Amazon");
		String[] poss = {"N", "H", "01"};
		wp.addAllPos(Arrays.asList(poss));
		String posString = wp.toJson();
		System.out.println(posString);
		try {
			WordPos wordPos = new ObjectMapper().readValue(posString, WordPos.class);
			System.out.println(wordPos.toJson(true));
		} catch (JsonProcessingException e) {
			System.err.println("exception: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
}
