package org.dwbzen.text.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.DataSourceDescription;
import org.dwbzen.text.util.DataSourceType;
import org.dwbzen.text.util.TextFileDataSource;
import org.dwbzen.text.util.exception.InvalidDataSourceException;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Book.TYPE;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;
import mathlib.cp.*;

import mathlib.cp.MarkovChain;

/**
 * WordCollectorRunner -display <displayFormats> -json <prettyFlag> 
 * <pre>
 * where displayFormats :: markov[chain] | summary[map] | inverted[map]
 *       pretyFlag :: true | yes | false | no
 * </pre>
 * @author don_bacon
 *
 */
public class WordCollectorRunner {
	protected static final Logger log = LogManager.getLogger(WordCollector.class);
	static boolean displayMarkovChain = false;
	static boolean sorted = false;	// applies to MarkovChain
	static boolean displaySummaryMap = false;
	static boolean displayInvertedSummary = false;

	static OutputStyle outputStyle = OutputStyle.TEXT;
	
	public static class WordCollectorBuilder {
		/**
		 * Builds a WordCollector. Uses default Book TYPE of PROSE
		 * @param order length of the key in #of Words, usually 2 or 3
		 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
		 * @param Type VERSE, TECHNICAL or PROSE
		 * @param Optional<String> optional schema
		 * @param args - file:filename just a text string.
		 * @return WordCollector instance
		 * If the schema is specified, it maps to a configured implementation class.
		 */
		public static WordCollector build(int order, boolean ignorecaseflag, TYPE type, Optional<String> theschema, String... args)  {
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
				dataSourceDescription = new DataSourceDescription(inputFile, DataSourceType.TextFile);
				dataSourceDescription.getProperties().setProperty("eol", (type.equals(TYPE.VERSE)) ? "\n" : "");
				schemaName = theschema.isPresent() ? theschema.get() : "text";
				dataSourceDescription.setSchema(schemaName);
				dataSource = new TextFileDataSource(dataSourceDescription);
				try {
					sourceText = dataSource.getData();
				} catch(InvalidDataSourceException e) {
					System.err.println(e.getMessage());
					log.error(e.getMessage());
					System.exit(1);
				}
			}
			WordCollector collector = new WordCollector(order, ignorecaseflag, schemaName);
			collector.setText(sourceText);	// also filters unwanted words
			Book book = new Book(collector.getText());
			book.setType(type);
			collector.setBook(book);
			collector.setMarkovChain(new MarkovChain<Word, Sentence>(order));
			return collector;
		}

	}
	
	public static void main(String...args) throws IOException {
		String inputFile = null;
		String text = null;
		String textType = "prose";
		boolean ignoreCase = false;
		String orderstring = null;
		List<Integer> orderList = new ArrayList<Integer>();
		String schema = "none";
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-schema")) {
				schema = args[++i];
			}
			else if(args[i].startsWith("-display")) {
				String[] formats = args[++i].split(",");
				for(String f : formats) {
					if(f.startsWith("markov")) { displayMarkovChain = true; }
					else if(f.startsWith("summary")) { displaySummaryMap = true; }
					else if(f.startsWith("inverted")) { displayInvertedSummary = true; }
				}
			}
			else if(args[i].equalsIgnoreCase("-output")) {
				String[] outputFormats = args[++i].split(",");
				// text, csv, json, pretty
				for(String f : outputFormats) {
					if(f.equalsIgnoreCase("json")) { outputStyle = OutputStyle.JSON; }
					else if(f.startsWith("pretty")) { outputStyle = OutputStyle.PRETTY_JSON; }
					else if(f.equalsIgnoreCase("text")) { outputStyle = OutputStyle.TEXT; }
					else if(f.equalsIgnoreCase("csv")) { outputStyle = OutputStyle.CSV; }
				}
			}
			else if(args[i].equalsIgnoreCase("-sorted")) {
				sorted = args[++i].equalsIgnoreCase("true") ? true : false;
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}			
			else if(args[i].equalsIgnoreCase("-order")) {	// can be a list
				orderstring = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-type")) {
				textType = args[++i];
			}
			else {
				text = args[i];
			}
		}
		Book.TYPE type = TYPE.PROSE;
		if(textType.equalsIgnoreCase("verse")) {
			type = TYPE.VERSE;
		}
		else if(textType.equalsIgnoreCase("technical")) {
			type = TYPE.TECHNICAL;
		}
		if(orderstring == null) {
			orderList.add(2);	// default order is 2 if not specified
		}
		else {
			for(String order : orderstring.split(",")) {
				orderList.add(Integer.parseInt(order));
			}
		}
		Map<Integer, MarkovChain<Word,Sentence>> markovChains = new TreeMap<Integer, MarkovChain<Word,Sentence>>();
		String[] collectorArg = new String[1];
		collectorArg[0] = (inputFile != null) ? "file:" + inputFile : text;
		Optional<String> optionalSchema = Optional.ofNullable(schema);
		for(Integer order : orderList) {
			WordCollector collector = WordCollectorBuilder.build(order, ignoreCase, type, optionalSchema, collectorArg);
			collector.collect();
			MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
			markovChains.put(order, markovChain);
		}
		if(orderList.size() == 1) {
			Integer ord = orderList.get(0);
			MarkovChain<Word, Sentence> markovChain = markovChains.get(ord);
			if(displayMarkovChain) {
				if(sorted) {
					String s = markovChain.getSortedDisplayText(outputStyle);
					System.out.println(s);
				}
				else {
					System.out.println( outputStyle==OutputStyle.JSON ? markovChain.toJson(true) :  markovChain.getMarkovChainDisplayText()); 
				}
			}
			if(displaySummaryMap) { 
				System.out.println(markovChain.getSummaryMapText()); 
			}
			if(displayInvertedSummary) { 
				boolean displayJson = outputStyle==OutputStyle.JSON || outputStyle==OutputStyle.PRETTY_JSON;
				System.out.println(markovChain.getInvertedSummaryMapText(displayJson , outputStyle==OutputStyle.PRETTY_JSON));
			}
		}
		else {
			displayMultichains(markovChains, outputStyle);
		}

	}
	
	private static void displayMultichains(Map<Integer, MarkovChain<Word, Sentence>> markovChains, OutputStyle outputStyle) {
		for(Integer ord : markovChains.keySet()) {
			MarkovChain<Word, Sentence> markovChain = markovChains.get(ord);
			boolean displayJson = outputStyle==OutputStyle.JSON || outputStyle==OutputStyle.PRETTY_JSON;
			if(displayMarkovChain) { 
				System.out.println( displayJson ? markovChain.toJson() :  markovChain.getMarkovChainDisplayText()); 
			}
			if(displaySummaryMap) { 
				System.out.println(markovChain.getSummaryMapText()); 
			}
			if(displayInvertedSummary) { 
				System.out.println(markovChain.getInvertedSummaryMapText(displayJson,  outputStyle==OutputStyle.PRETTY_JSON));
			}
		}
	}
	
}
