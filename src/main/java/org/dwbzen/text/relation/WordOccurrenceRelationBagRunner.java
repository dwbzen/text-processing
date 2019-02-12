package org.dwbzen.text.relation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.TextFileReader;
import org.dwbzen.text.util.Util;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.SourceOccurrenceProbability;
import mathlib.Tupple;
import mathlib.cp.OutputStyle;

public class WordOccurrenceRelationBagRunner {
	protected static final Logger log = LogManager.getLogger(WordOccurrenceRelationBag.class);
	static boolean displayMarkovChain = false;
	static boolean sorted = false;	// applies to MarkovChain
	static boolean displaySummaryMap = false;
	static boolean displayInvertedSummary = false;

	static OutputStyle outputStyle = OutputStyle.TEXT;
	static boolean trace = false;
	
	/**
	 * Builds a ordOccurrenceRelationBag. Uses default Book TYPE of PROSE
	 * @param order length of the key in #of Words, usually 2 or 3
	 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
	 * @param Type VERSE, TECHNICAL or PROSE
	 * @param Optional<String> optional schema
	 * @param args - file:filename just a text string.
	 * @return WordOccurrenceRelationBag instance
	 * 
	 * If the schema is specified, it maps to a configured data formatter class.
	 * For example the "ticket" schema: dataFormatterClass.ticket=org.dwbzen.text.util.TicketDataFormatter
	 * 
	 */	
	public static class WordOccurrenceRelationBagBuilder {

	}
	 
	 
		public static void main(String...args) {
			String[] filenames = {};
			List<OutputStyle> outputStyles = new ArrayList<>();
			String text = null;
			boolean sorted = false;
			String orderstring = null;
			boolean reverseSorted = false;
			boolean ignoreCase = false;
			boolean supressSources = false;
			Sentence sentence = null;
			String schema = "none";
			String textType = "technical";
			List<Integer> orderList = new ArrayList<Integer>();
			
			for(int i=0; i<args.length; i++) {
				if(args[i].startsWith("-file")) {
					// comma-separated list of files
					filenames = args[++i].split(",");
				}
				else if(args[i].equalsIgnoreCase("-schema")) {
					schema = args[++i];
				}
				else if(args[i].equalsIgnoreCase("-ignoreCase")) {
					ignoreCase = true;
				}
				else if(args[i].equalsIgnoreCase("-order")) {
					orderstring = args[++i];
				}
				else if(args[i].equalsIgnoreCase("-type")) {
					textType = args[++i];
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
			Optional<String> optionalSchema = Optional.ofNullable(schema);
			// TODO - use the builder instead
			WordOccurrenceRelationBag occurrenceRelationBag = new WordOccurrenceRelationBag(order);
			
			occurrenceRelationBag.setSupressSourceOutput(supressSources);
			WordOccurrenceRelationBag  targetoccurrenceRelationBag = occurrenceRelationBag;
			if(text != null && text.length() > 0) {
				sentence = new Sentence(text);
				addSentence(sentence, occurrenceRelationBag, order, ignoreCase);
			}
			if(filenames.length > 0) {
				//TODO use optional schema
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
			Map<Tupple<Word>, SourceOccurrenceProbability<Word, Sentence>> sortedMap = null;
			/*
			 * Display the results according to specified style(s)
			 */
			if(sorted) {
				sortedMap = occurrenceRelationBag.sortByValue(reverseSorted);
				targetoccurrenceRelationBag = new WordOccurrenceRelationBag(occurrenceRelationBag, sortedMap);
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
		
		private static void displaySummaryTextOutput(WordOccurrenceRelationBag orb, OutputStyle style, PrintStream out) {
			out.println("totalOccurrences: " + orb.getTotalOccurrences());
			for(SourceOccurrenceProbability<Word, Sentence> sop : 	orb.getSourceOccurrenceProbabilityMap().values()) {
				out.println(sop.getKey().toString(true) + ": " + sop.getOccurrenceProbability().getOccurrence());
			}
		}
		
		private static void addSentence(Sentence sentence, WordOccurrenceRelationBag occurrenceRelationBag, int degree, boolean ignoreCase) {
			WordOccurranceRelation occurrenceRelation = new WordOccurranceRelation(sentence, degree, ignoreCase);
			if(trace) {
				Set<Tupple<Word>> partitions = occurrenceRelation.getPartitions();
				System.out.println(sentence + "\n" + partitions);
			}
			occurrenceRelationBag.addOccurrenceRelation(occurrenceRelation);
		}

}
