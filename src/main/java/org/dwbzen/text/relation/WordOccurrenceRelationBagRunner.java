package org.dwbzen.text.relation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.element.Book;
import org.dwbzen.text.element.Sentence;
import org.dwbzen.text.element.Word;
import org.dwbzen.text.element.Book.ContentType;
import org.dwbzen.text.util.Configuration;
import org.dwbzen.text.util.DataSourceDescription;
import org.dwbzen.text.util.DataSourceType;
import org.dwbzen.text.util.TextConfigurator;
import org.dwbzen.text.util.TextFileDataSource;
import org.dwbzen.text.util.PosUtil;
import org.dwbzen.common.math.SourceOccurrenceProbability;
import org.dwbzen.common.math.Tupple;
import org.dwbzen.common.cp.OutputStyle;

public class WordOccurrenceRelationBagRunner {
	protected static final Logger log = LogManager.getLogger(WordOccurrenceRelationBag.class);
	static boolean displayMarkovChain = false;
	static boolean sorted = false;	// applies to MarkovChain
	static boolean displaySummaryMap = false;
	static boolean displayInvertedSummary = false;

	static OutputStyle outputStyle = OutputStyle.TEXT;
	static boolean trace = false;
	
	static TextConfigurator textConfigurator = null;
	static Configuration configuration = null;
	
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
		
		public static WordOccurrenceRelationBag build(int degree, boolean ignorecaseflag, boolean supressSources, boolean supressIds, ContentType type, Optional<String> theschema, String... args) {
			String sourceText = null;
			String inputFile = null;
			DataSourceDescription dataSourceDescription = null;
			TextFileDataSource dataSource = null;
			String schemaName = null;
			
			for(int i=0; i<args.length; i++) {
				if(args[i].startsWith("file:")) {
					inputFile = args[i].substring(args[i].indexOf(':') + 1);
				}
				else {
					dataSourceDescription = new DataSourceDescription(DataSourceType.Text);
					sourceText = args[i];
				}
			}
			if(inputFile != null) {
				String inputFilename = PosUtil.getInputFilename(inputFile);
				if(theschema.isPresent()) {
					dataSourceDescription = new DataSourceDescription(inputFilename, DataSourceType.JsonText);
					schemaName = theschema.get();
					dataSourceDescription.getProperties().setProperty("eol", "\n");
				}
				else {
					schemaName = "text";		
					dataSourceDescription = new DataSourceDescription(inputFilename, DataSourceType.TextFile);
					dataSourceDescription.getProperties().setProperty("eol", (type.equals(ContentType.VERSE)) ? "\n" : "");
				}
				dataSourceDescription.setSchema(schemaName);
				try {
					dataSource = new TextFileDataSource(dataSourceDescription);
					sourceText = dataSource.getData();
				} catch(Exception e) {
					System.err.println(e.getMessage());
					log.error(e.getMessage());
					System.exit(1);
				}
			}
			WordOccurrenceRelationBag occurrenceRelationBag = new WordOccurrenceRelationBag(degree, ignorecaseflag, schemaName, type, dataSource);
			occurrenceRelationBag.setSupressSourceOutput(supressSources);
			occurrenceRelationBag.setSupressIdOutput(supressIds);
			occurrenceRelationBag.setText(sourceText);
			return occurrenceRelationBag;
		}

	}
	 
		public static void main(String...args) {
			String inputFile = null;
			List<OutputStyle> outputStyles = new ArrayList<>();
			String text = null;
			boolean sorted = false;
			String orderstring = null;
			boolean reverseSorted = false;
			boolean ignoreCaseFlag = false;
			boolean showSources = false;
			boolean showIds = true;
			String schema = "none";
			String textType = "technical";
			List<Integer> orderList = new ArrayList<Integer>();
			
			for(int i=0; i<args.length; i++) {
				if(args[i].startsWith("-file")) {
					inputFile = args[++i];
				}
				else if(args[i].equalsIgnoreCase("-schema")) {
					schema = args[++i];
				}
				else if(args[i].equalsIgnoreCase("-ignoreCase")) {
					ignoreCaseFlag = true;
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
				else if(args[i].startsWith("-sources")) {	// Show sources in output defaults to false
					showSources =  args[++i].equalsIgnoreCase("true") ? true : false;
				}
				else if(args[i].startsWith("-ids")) {	// Show ids in output defaults to true
					showIds = args[++i].equalsIgnoreCase("true") ? true : false;;
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
			Book.ContentType type = ContentType.PROSE;
			if(textType.equalsIgnoreCase("verse")) {
				type = ContentType.VERSE;
			}
			else if(textType.equalsIgnoreCase("technical")) {
				type = ContentType.TECHNICAL;
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
			int degree = orderList.get(0);
			String[] builderArgs = new String[1];
			builderArgs[0] = (inputFile != null) ? "file:" + inputFile : text;
			Optional<String> optionalSchema = Optional.ofNullable(schema);

			/*
			 * This creates and configures a WordOccurrenceRelationBag instance
			 * which includes loading and formatting the data
			 * and adding WordOccurrenceRelations for each Sentence in the data (Book)
			 */
			WordOccurrenceRelationBag occurrenceRelationBag = WordOccurrenceRelationBagBuilder.build(degree, ignoreCaseFlag, !showSources, !showIds, type, optionalSchema, builderArgs);
			WordOccurrenceRelationBag  targetoccurrenceRelationBag = occurrenceRelationBag;
			/*
			 * Closes the WordOccurrenceRelationBag and recomputes the probabilities and ranges.
			 */
			occurrenceRelationBag.close();
			
			Map<Tupple<Word>, SourceOccurrenceProbability<Word, Sentence>> sortedMap = null;
			if(sorted) {
				// check first
				if(trace) {
					for(Entry<Tupple<Word>, SourceOccurrenceProbability<Word, Sentence>> entry : occurrenceRelationBag.getSourceOccurrenceProbabilityMap().entrySet()) {
						Tupple<Word> key = entry.getKey();
						SourceOccurrenceProbability<Word, Sentence> value = entry.getValue();
						System.out.println(key.toString(true) + " " + value.getOccurrenceProbability().getOccurrence());
					}
				}
				sortedMap = occurrenceRelationBag.sortByValue(reverseSorted);
				targetoccurrenceRelationBag = new WordOccurrenceRelationBag(occurrenceRelationBag, sortedMap);
			}
			/*
			 * Display the results according to specified style(s)
			 */
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
}
