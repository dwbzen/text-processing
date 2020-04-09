package org.dwbzen.text.pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A PatternWord is a "compiled" version of a text pattern word template such as N{2,5}</br>
 * An instance is an actual pattern created at random from the template</br>
 * For example, "NN", "NNN", "NNNN", and "NNNNN" are all instances of N{2,5}</br>
 * Inline (not-interpreted) text can also be included in patterns, For example. "[F|M](and)[F|M]"
 * @author don_bacon
 *
 */
public class PatternWord implements IJson {

	@JsonIgnore			private int rangeLow = 1;
	@JsonIgnore			private int rangeHigh = 1;
	@JsonIgnore			private List<String> choices = new ArrayList<String>();		// could be only 1 or size=0 if raw text
	@JsonIgnore			private char variable = 0;		// '1', '2', etc. for $1, ... $9

	// inline, non-pattern text
	@JsonIgnore			private List<String> textChoices = new ArrayList<String>();
	@JsonIgnore			Random rand = new Random();
	@JsonProperty("instance")	private String instance = "";
	/**
	 * This delimits pattern in-line text instances
	 */
	public static String TEXT_DELIMETER = "/";

	public PatternWord(int low, int high, List<String> choicesList) {
		rangeLow = low;
		rangeHigh = high;
		if(choicesList != null && choicesList.size()>0) {
			choices.addAll(choicesList);
		}
	}
	
	public PatternWord(int low, int high, String choicesString) {
		rangeLow = low;
		rangeHigh = high;
		choices.add(choicesString);
	}
	public PatternWord(String choicesString) {
		choices.add(choicesString);
	}
	public PatternWord(List<String> choicesList) {
		this(1, 1, choicesList);
	}
	public PatternWord(char c) {
		choices.add(String.valueOf(c));
	}
	public PatternWord(int low, int high, List<String> choicesList, char var) {
		this(low, high, choicesList);
		variable = var;
	}
	public PatternWord(int low, int high, String choices, char var) {
		this(low, high, choices);
		variable = var;
	}
	/**
	 * If choices == null && inlineText != null, range applies to inlineText
	 * If choices != null && inlineText == null, range applies to choices
	 * If both are != null, range applies to both
	 * @param low
	 * @param high
	 * @param choices
	 * @param inlineText
	 */
	public PatternWord(int low, int high, List<String> choicesList, List<String> inlineTextList) {
		this(low, high, choicesList);
		if(inlineTextList != null && inlineTextList.size()>0) {
			textChoices.addAll(inlineTextList);
		}
	}
	public PatternWord(int low, int high, List<String> choicesList, String inlineText) {
		this(low, high, choicesList);
		if(inlineText != null && inlineText.length()>0) {
			textChoices.add(inlineText);
		}
	}
	public PatternWord(int low, int high, String choicesString, String inlineText) {
		choices.add(choicesString);
		rangeLow = low;
		rangeHigh = high;
		if(inlineText != null && inlineText.length()>0) {
			textChoices.add(inlineText);
		}
	}

	/**
	 * Creates an instance of this PatternWord
	 * In-line text instances are returned as the text string
	 * delimited by TEXT_DELIMETER (usually "/")
	 * For example, (and) becomes "/and/"
	 * Metacharacters $ and = are re-inserted in the toString()
	 * @return String instance and sets pattern to this instance
	 */
	public String getInstance() {
		int repeat = rangeLow + rand.nextInt(rangeHigh-rangeLow+1);
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<repeat; i++) {
			if(textChoices.size() > 0) {
				sb.append(TEXT_DELIMETER +
						((textChoices.size() > 1) ?
								textChoices.get(rand.nextInt(textChoices.size())) :
									textChoices.get(0)) +
						TEXT_DELIMETER);
			}
			if(choices.size() > 0) {	// choices will never be null
				if(variable > 0){
					sb.append(TEXT_DELIMETER + "=" + variable + TEXT_DELIMETER);
				}
				if(choices.size() > 1) {
					sb.append(String.valueOf(choices.get(rand.nextInt(choices.size()))));
				}
				else if(choices.get(0) != null){
					sb.append(choices.get(0));
				}
			}
			else if(variable > 0){
				// ($1) instance is /$1/
				sb.append(TEXT_DELIMETER + "$" + variable + TEXT_DELIMETER);
			}
		}
		instance = sb.toString();
		return instance;
	}

	/** 
	 * This reconstructs the original pattern from the PatternWord
	 *
	 * ex. "[a|b|c]"   or   N{1,4}
	 *
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(choices != null) {
			if(variable > 0) {
				sb.append("$" + variable + "=");
			}
			if(choices.size() > 1) {
				sb.append('[');
				for(int i=0; i<choices.size(); i++) {
					sb.append(choices.get(i));
					if(i != choices.size()-1) {
						sb.append(" | ");
					}
				}
				sb.append(']');
			}
			else if(choices.size() > 0 && choices.get(0) != null){
				sb.append(choices);
			}
		}
		else if(variable > 0) {
			sb.append("($" + variable + ")");
		}
		if(textChoices.size()>0) {
			if(textChoices.size() == 1)
				sb.append("(" + textChoices.get(0) + ")");
			else {
				sb.append('[');
				for(int i=0; i<textChoices.size(); i++) {
					sb.append("(" + textChoices.get(i) + ")");
					if(i != textChoices.size()-1) {
						sb.append("|");
					}
				}
				sb.append(']');
			}
		}
		if(rangeLow == 0 && rangeHigh ==1) {
			sb.append('?');
		}
		else if(rangeLow != 0 || rangeLow != 1) {
			if(rangeLow == rangeHigh) {
				if(rangeLow != 1)
					sb.append("{" + rangeLow + "}");
			}
			else {
				sb.append("{" + rangeLow + "," + rangeHigh + "}");
			}
		}
		return sb.toString();
	}
	public int getRangeLow() {
		return rangeLow;
	}
	public void setRangeLow(int rangeLow) {
		this.rangeLow = rangeLow;
	}
	public int getRangeHigh() {
		return rangeHigh;
	}
	public void setRangeHigh(int rangeHigh) {
		this.rangeHigh = rangeHigh;
	}
	public List<String> getChoices() {
		return choices;
	}

	public Random getRand() {
		return rand;
	}
	public void setRand(Random rand) {
		this.rand = rand;
	}
	public List<String> getTextChoices() {
		return textChoices;
	}
	public void setTextChoices(List<String> choices) {
		textChoices = new ArrayList<String>(choices);
	}
	public void addTextChoice(String text) {
		textChoices.add(text);
	}
	public char getVariable() {
		return variable;
	}
	public void setVariable(char variable) {
		this.variable = variable;
	}
}
