package org.dwbzen.text.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.element.Sentence;
import org.dwbzen.text.element.Word;
import org.dwbzen.text.util.TextFileReader;
import org.dwbzen.text.util.Util;
import org.dwbzen.common.cp.CollectorStats;
import org.dwbzen.common.cp.MarkovChain;
import org.dwbzen.common.cp.OutputStyle;


public class CharacterCollectorRunner {
	protected static final Logger log = LogManager.getLogger(CharacterCollector.class);
	static boolean displayMarkovChain = false;
	static boolean sorted = false;	// applies to MarkovChain
	static boolean displaySummaryMap = false;
	static boolean displayInvertedSummary = false;
	static OutputStyle outputStyle = OutputStyle.TEXT;
	public final static String[] CONFIG_FILES = {"/config.properties"};
	
	public static class CharacterCollectorBuilder {
		static TextFileReader reader = null;

		/**
		 * Factory method.
		 * @param order length of the key in #of characters, usually 2 or 3
		 * @param inputFile full path to the input file. Use null for STDIN
		 * @param ignorecaseflag set to true to ignore case. This converts all input to lower case.
		 * @return CharacterCollector instance
		 */
		public static CharacterCollector build(int order, String[] inputFiles, boolean ignorecaseflag) throws IOException {
			CharacterCollector collector = new CharacterCollector(order, inputFiles, ignorecaseflag);
			collector.setMarkovChain(new MarkovChain<Character, Word, Sentence>(order));
			for(String inputFile : inputFiles) {
				String inputFilename = Util.getInputFilename(inputFile);
				reader = TextFileReader.getInstance(inputFilename);
				reader.setMinimumLength(order);
				collector.setText( (ignorecaseflag? 
							reader.getFileText().toLowerCase() :
							reader.getFileText()) );
			}
			return collector;
		}
		
		public static CharacterCollector build(int order, String[] inputFiles, boolean ignorecaseflag, String pos) throws IOException {
			CharacterCollector collector = new CharacterCollector(order, inputFiles, ignorecaseflag, pos);
			collector.setMarkovChain(new MarkovChain<Character, Word, Sentence>(order));
			for(String inputFile : inputFiles) {
				String inputFilename = Util.getInputFilename(inputFile);
				reader = TextFileReader.getInstance(inputFilename);
				reader.setMinimumLength(order);
				List<String> fileText = reader.getFileLines();
				for(String s : fileText) {
					String[] splits = s.split("\t");
					if(splits.length > 1) {
						if(Util.containsAny(splits[1], pos)) {
							collector.setText( (ignorecaseflag ? splits[0].toLowerCase() : splits[0]) + " ");
						}
					}
					else {	// no POS specified
						collector.setText( (ignorecaseflag ? splits[0].toLowerCase() : splits[0]) + " ");
					}
				}
			}
			return collector;
		}
		
	}

	public static void main(String...args) throws IOException {
		String[] filenames = {};
		String text = null;
		boolean ignoreCase = false;
		boolean trace = false;
		boolean showSupplierCounts = false;
		String orderstring = null;
		List<Integer> orderList = new ArrayList<Integer>();
		
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-file")) {
				filenames = args[++i].split(",");
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				orderstring = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = true;
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
					else if(f.equalsIgnoreCase("suppliers")) { showSupplierCounts = true;}
				}
			}
			else if(args[i].equalsIgnoreCase("-sorted")) {
				sorted = args[++i].equalsIgnoreCase("true") ? true : false;
			}
			else {
				text = args[i];
			}
		}
		if(orderstring == null) {
			orderList.add(2);	// default order is 2 if not specified
		}
		else {
			for(String order : orderstring.split(",")) {
				orderList.add(Integer.parseInt(order));
			}
		}
		Map<Integer, MarkovChain<Character, Word, Sentence>>  markovChains = new TreeMap<Integer, MarkovChain<Character, Word, Sentence>>();
		CollectorStats.trace = trace;
		for(Integer order : orderList) {
			CharacterCollector collector = CharacterCollectorBuilder.build(order, filenames, ignoreCase);
			collector.setTrace(trace);
			if(filenames.length == 0) {
				text = ignoreCase ? text.toLowerCase() : text;
				collector.setText(text);
			}
			collector.collect();
			MarkovChain<Character, Word, Sentence> markovChain = collector.getMarkovChain();
			markovChains.put(order, markovChain);
		}

		MarkovChain<Character, Word, Sentence> markovChain = combineMultichains(markovChains);
		if(displayMarkovChain) {
			if(sorted) {
				String s = markovChain.getSortedDisplayText(outputStyle, showSupplierCounts);
				System.out.println(s);
			}
			else {
				System.out.println(  markovChain.getMarkovChainDisplayText(outputStyle, showSupplierCounts)); 
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
	
	private static MarkovChain<Character, Word, Sentence>combineMultichains(Map<Integer, MarkovChain<Character, Word, Sentence>> markovChains) {
		MarkovChain<Character, Word, Sentence> markovChain = null;
		for(Integer ord : markovChains.keySet()) {
			if(markovChain == null) {
				markovChain = markovChains.get(ord);
			}
			else {
				markovChain.add(markovChains.get(ord));
			}
		}
		return markovChain;
	}
}
