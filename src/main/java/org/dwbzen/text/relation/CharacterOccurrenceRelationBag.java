package org.dwbzen.text.relation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dwbzen.text.util.TextFileReader;
import org.dwbzen.text.util.Util;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.SourceOccurrenceProbability;
import mathlib.Tupple;
import mathlib.cp.OutputStyle;
import mathlib.relation.OccurrenceRelationBag;

public class CharacterOccurrenceRelationBag extends OccurrenceRelationBag<Character, Word, Sentence> {

	private static final long serialVersionUID = 8408003287349121596L;
	static OutputStyle outputStyle = OutputStyle.TEXT;
	static boolean trace = false;
	public static final String indent = "      ";
	public final static String[] CONFIG_FILES = {"/config.properties"};
	private boolean supressSourceOutput = false;
	
	public CharacterOccurrenceRelationBag(int degree) {
		super(degree);
	}
	
	/**
	 * Copy constructor (shallow)
	 * @param CharacterOccurrenceRelationBag otherBag
	 * @param Map<Tupple<Character>, SourceOccurrenceProbability<Character,Word>> optionalMap
	 */
	public CharacterOccurrenceRelationBag(CharacterOccurrenceRelationBag otherBag, 
										  Map<Tupple<Character>, SourceOccurrenceProbability<Character,Word>> optionalMap) {
		super(otherBag.getDegree());
		setSupressSourceOutput(otherBag.isSupressSourceOutput());
		setTotalOccurrences(otherBag.getTotalOccurrences());
		setMetricFunction(new TuppleWordDistanceMetric());
		if(optionalMap != null) {
			setSourceOccurrenceProbabilityMap(optionalMap);
		}
		recomputeProbabilities();
	}
	
	public static void main(String...args) {
		String[] filenames = {};
		List<OutputStyle> outputStyles = new ArrayList<>();
		String text = null;
		boolean sorted = false;
		boolean reverseSorted = false;
		String orderstring = null;
		boolean ignoreCase = false;
		boolean supressSources = false;
		Sentence sentence = null;
		List<Integer> orderList = new ArrayList<Integer>();
		
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-file")) {
				// comma-separated list of files
				filenames = args[++i].split(",");
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				orderstring = args[++i];
			}
			else if(args[i].startsWith("-sort")) {
				sorted = args[++i].equalsIgnoreCase("true") ? true : false;
				if(args[i].startsWith("rev")) {
					reverseSorted = true;
					sorted = true;
				}
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = true;
			}
			else if(args[i].startsWith("-nos")) {	// Suppress sources in output
				supressSources = true;
			}			
			else if(args[i].equalsIgnoreCase("-output")) {
				String[] outputFormats = args[++i].split(",");
				// text, csv, json, pretty
				for(String f : outputFormats) {
					if(f.equalsIgnoreCase("json")) { outputStyles.add(OutputStyle.JSON); }
					else if(f.startsWith("pretty")) { outputStyles.add(OutputStyle.PRETTY_JSON); }
					else if(f.equalsIgnoreCase("text")) { outputStyles.add(OutputStyle.TEXT); }
					else if(f.equalsIgnoreCase("csv")) { outputStyles.add(OutputStyle.CSV); }
					else if(f.equalsIgnoreCase("summary")) { outputStyles.add(OutputStyle.TEXT_SUMMARY); }
				}
			}
			else {
				text = args[i];
			}
		}
		if(outputStyles.size() == 0) {
			// default output style if not set
			outputStyles.add(OutputStyle.PRETTY_JSON);
		}
		if(orderstring == null) {
			orderList.add(2);	// default order is 2 if not specified
		}
		else {
			for(String order : orderstring.split(",")) {
				orderList.add(Integer.parseInt(order));
			}
		}
		int order = orderList.get(0);
		CharacterOccurrenceRelationBag occurrenceRelationBag = new CharacterOccurrenceRelationBag(order);
		occurrenceRelationBag.setSupressSourceOutput(supressSources);
		occurrenceRelationBag.setMetricFunction(new TuppleWordDistanceMetric());
		CharacterOccurrenceRelationBag targetoccurrenceRelationBag = occurrenceRelationBag;
		
		if(text != null && text.length() > 0) {
			sentence = new Sentence(text);
			addSentence(sentence, occurrenceRelationBag, order, ignoreCase);
		}
		if(filenames.length > 0) {
			try {
				for(String inputFile : filenames) {
					String inputFilename = Util.getInputFilename(inputFile);
					TextFileReader reader = TextFileReader.getInstance(inputFilename);
					reader.setMinimumLength(order);
					String fileText = ignoreCase ? reader.getFileText().toLowerCase() : reader.getFileText();
					sentence = new Sentence(fileText);
					addSentence(sentence, occurrenceRelationBag, order, ignoreCase);
				}
			}
			catch(Exception ex) { System.err.println("Something went wrong " +  ex.toString()); }
		}
		occurrenceRelationBag.close();
		Map<Tupple<Character>, SourceOccurrenceProbability<Character,Word>> sortedMap = null;
		/*
		 * Display the results according to specified style(s)
		 */
		if(sorted) {
			sortedMap = occurrenceRelationBag.sortByValue(reverseSorted);
			targetoccurrenceRelationBag = new CharacterOccurrenceRelationBag(occurrenceRelationBag, sortedMap);
		}
		for(OutputStyle style : outputStyles) {
			switch(style) {
			case TEXT:
			case TEXT_SUMMARY: displaySummaryTextOutput(targetoccurrenceRelationBag, style, System.out);
				break;
			case JSON: 
				System.out.println(targetoccurrenceRelationBag.toJson());
				break;
			case CSV:
			case PRETTY_JSON:
				System.out.println(targetoccurrenceRelationBag.toJson(true));
				break;
			}
		}
	}

	public boolean isSupressSourceOutput() {
		return supressSourceOutput;
	}

	public void setSupressSourceOutput(boolean supressSourceOutput) {
		this.supressSourceOutput = supressSourceOutput;
	}

	@Override
	public String toJson(boolean pretty) {
		if(!pretty) {
			return super.toJson();
		}
		StringBuilder sb = new StringBuilder("{\n  \"totalOccurrences\" : " + getTotalOccurrences() + "\n");
		sb.append("  \"degree\" : " + this.getDegree() + "\n");
		sb.append("  \"sourceOccurrenceProbabilityMap\" : {\n");
		for(SourceOccurrenceProbability<Character, Word> sop : 	sourceOccurrenceProbabilityMap.values()) {
			sb.append("    \"" + sop.getKey().toString(false) + "\" : {\n");
			sb.append(sop.getOccurrenceProbability().toJson(indent));
			String avgDistance = indent + "\"averageDistance\" : " + sop.getAverageDistanceText();
			sb.append(",\n" + avgDistance );
			if(!isSupressSourceOutput()) {
				sb.append(",\n");
				sb.append(indent);				
				sb.append("\"sources\" : [");
				int nsources = sop.getSources().size();
				int i = 0;
				for(Word word : sop.getSources()) {
					sb.append("[" + word.quoteString(word.toString()) + "]");
					if(++i < nsources) {sb.append(","); }
				}
				sb.append("]\n");
				sb.append("    },\n");
			}
			else {
				sb.append(",\n");
			}
		}
		sb.deleteCharAt(sb.length()-2);
		sb.append("}\n");
		return sb.toString();
	}

	private static void displaySummaryTextOutput(CharacterOccurrenceRelationBag orb, OutputStyle style, PrintStream out) {
		out.println("totalOccurrences: " + orb.getTotalOccurrences());
		for(SourceOccurrenceProbability<Character, Word> sop : 	orb.sourceOccurrenceProbabilityMap.values()) {
			out.println(sop.getKey().toString(false) + ": " + sop.getOccurrenceProbability().getOccurrence());
		}
	}

	private static void addSentence(Sentence sentence, CharacterOccurrenceRelationBag occurrenceRelationBag, int order, boolean ignoreCase) {
		for(Word word:sentence) {
			CharacterOccurrenceRelation occurrenceRelation = new CharacterOccurrenceRelation(word, order, ignoreCase);
			if(trace) {
				Set<Tupple<Character>> partitions = occurrenceRelation.getPartitions();
				System.out.println(word + "\n" + partitions);
			}
			occurrenceRelationBag.addOccurrenceRelation(occurrenceRelation);
		}
	}
}
