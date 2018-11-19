package org.dwbzen.text.cp;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.TextFileReader;
import org.dwbzen.text.util.Util;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.cp.MarkovChain;
import mathlib.cp.OutputStyle;


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
		int order = 2;
		boolean ignoreCase = false;
		boolean trace = false;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-file")) {
				filenames = args[++i].split(",");
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				order = Integer.parseInt(args[++i]);
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
				}
			}
			else if(args[i].equalsIgnoreCase("-sorted")) {
				sorted = args[++i].equalsIgnoreCase("true") ? true : false;
			}
			else {
				text = args[i];
			}
		}
		CharacterCollector collector = CharacterCollectorBuilder.build(order, filenames, ignoreCase);
		if(filenames.length == 0) {
			text = ignoreCase ? text.toLowerCase() : text;
			collector.setText(text);
		}
		collector.setTrace(trace);
		collector.collect();
		MarkovChain<Character, Word, Sentence> markovChain = collector.getMarkovChain();
		
		if(displayMarkovChain) {
			if(sorted) {
				String s = markovChain.getSortedDisplayText(outputStyle);
				System.out.println(s);
			}
			else {
				System.out.println(  markovChain.getMarkovChainDisplayText(outputStyle)); 
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
}
