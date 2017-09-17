package util.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A PatternWord is a "compiled" version of a text pattern word template such as N{2,5}
 * An instance is an actual pattern created at random from the template
 * For example, "NN", "NNN", "NNNN", and "NNNNN" are all instances of N{2,5}
 * Inline (not-interpreted) text can also be included in patterns
 * For example. "[F|M](and)[F|M]"
 * @author dbacon
 *
 */
public class PatternWord {
		private int rangeLow = 1;
		private int rangeHigh = 1;
		private String choices = null;	// could be only 1 or null if raw text
		private char variable = 0;		// '1', '2', etc. for $1, ... $9
		
		// inline, non-pattern text
		private List<String> textChoices = new ArrayList<String>();
		
		Random rand = new Random();
		/**
		 * This delimits pattern in-line text instances
		 */
		public static String TEXT_DELIMETER = "/";
		
		public PatternWord(int low, int high, String choices) {
			rangeLow = low;
			rangeHigh = high;
			this.choices = choices;
		}
		public PatternWord(String choices) {
			this(1, 1, choices);
		}
		public PatternWord(char c) {
			this(1,1, String.valueOf(c));
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
		public PatternWord(int low, int high, String choices, List<String> inlineTextList) {
			this(low, high, choices);
			if(inlineTextList != null && inlineTextList.size()>0) {
				textChoices.addAll(inlineTextList);
			}
		}
		public PatternWord(int low, int high, String choices, String inlineText) {
			this(low, high, choices);
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
		 * @return String instance
		 */
		public String createInstance() {
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
				if(choices != null) {
					if(variable > 0){
						sb.append(TEXT_DELIMETER + "=" + variable + TEXT_DELIMETER);
					}
					sb.append( (choices.length() > 1) ?
							String.valueOf(choices.charAt(rand.nextInt(choices.length()))) :
							choices);
				}
				else if(variable > 0){
					// ($1) instance is /$1/
					sb.append(TEXT_DELIMETER + "$" + variable + TEXT_DELIMETER);
				}
			}
			return sb.toString();
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
				if(choices.length() > 1) {
					sb.append('[');
					for(int i=0; i<choices.length(); i++) {
						sb.append(choices.charAt(i));
						if(i != choices.length()-1) {
							sb.append(" | ");
						}
					}
					sb.append(']');
				}
				else {
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
		public String getChoices() {
			return choices;
		}
		public void setChoices(String choices) {
			this.choices = choices;
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
