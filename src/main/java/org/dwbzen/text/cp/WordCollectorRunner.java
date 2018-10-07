package org.dwbzen.text.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Book.TYPE;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.cp.CollectorStats;
import mathlib.cp.MarkovChain;
import mathlib.cp.OccurrenceProbability;

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
	static boolean displayMarkovChain = false;
	static boolean sorted = false;	// applies to MarkovChain
	static boolean displaySummaryMap = false;
	static boolean displayInvertedSummary = false;
	static boolean displayJson = false;
	static boolean prettyJson = false;
	static boolean textOutput = false;
	static boolean csvOutput = false;
	
	public static void main(String...args) throws IOException {
		String inputFile = null;
		String text = null;
		String textType = "prose";
		boolean ignoreCase = false;
		String orderstring = null;
		List<Integer> orderList = new ArrayList<Integer>();
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
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
					if(f.equalsIgnoreCase("json")) { displayJson = true; }
					else if(f.startsWith("pretty")) { prettyJson = true; displayJson = true; }
					else if(f.equalsIgnoreCase("text")) { textOutput = true; }
					else if(f.equalsIgnoreCase("csv")) { csvOutput = true; }
				}
			}
			else if(args[i].equalsIgnoreCase("-sorted")) {
				sorted = true;
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
		for(Integer order : orderList) {
			WordCollector collector = WordCollector.getWordCollector(order, ignoreCase, type, collectorArg);
			collector.collect();
			MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
			markovChains.put(order, markovChain);
		}
		if(orderList.size() == 1) {
			Integer ord = orderList.get(0);
			MarkovChain<Word, Sentence> markovChain = markovChains.get(ord);
			if(displayMarkovChain) {
				if(sorted) {
					Map<?,?> sortedChain = markovChain.sortByValue();
					for(Object obj : sortedChain.keySet()) {
						Sentence sentence = (Sentence)obj;
						@SuppressWarnings("unchecked")
						CollectorStats<Word, Sentence> cstats = (CollectorStats<Word, Sentence>)sortedChain.get(sentence);
						@SuppressWarnings("unchecked")
						Map<Word,OccurrenceProbability> sortedStats = (Map<Word,OccurrenceProbability>)cstats.sortByValue();
						System.out.println(sentence + "\t" + cstats.getTotalOccurrance());
						for(Word key : sortedStats.keySet()) {
							OccurrenceProbability op = sortedStats.get(key);
							System.out.println("\t" + key + "\t" + op.getOccurrence() + "\t" + op.getProbability());
						}
					}
				}
				else {
					System.out.println( displayJson ? markovChain.toJson() :  markovChain.getMarkovChainDisplayText()); 
				}
			}
			if(displaySummaryMap) { 
				System.out.println(markovChain.getSummaryMapText()); 
			}
			if(displayInvertedSummary) { 
				System.out.println(markovChain.getInvertedSummaryMapText(displayJson, prettyJson));
			}
		}
		else {
			displayMultichains(markovChains);
		}

	}

	private static void displayMultichains(Map<Integer, MarkovChain<Word, Sentence>> markovChains) {
		for(Integer ord : markovChains.keySet()) {
			MarkovChain<Word, Sentence> markovChain = markovChains.get(ord);
			if(displayMarkovChain) { 
				System.out.println( displayJson ? markovChain.toJson() :  markovChain.getMarkovChainDisplayText()); 
			}
			if(displaySummaryMap) { 
				System.out.println(markovChain.getSummaryMapText()); 
			}
			if(displayInvertedSummary) { 
				System.out.println(markovChain.getInvertedSummaryMapText(displayJson, prettyJson));
			}
		}
	}
	
}
