package org.dwbzen.text.util.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;
import org.dwbzen.text.util.model.Book.TYPE;

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
	
	public static void main(String...args) throws IOException {
		String inputFile = null;
		String text = null;
		String textType = "prose";
		boolean displayMarkovChain = false;
		boolean displaySummaryMap = false;
		boolean displayInvertedSummary = false;
		boolean displayJson = false;
		boolean ignoreCase = false;
		boolean prettyJson = false;
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
			else if(args[i].equalsIgnoreCase("-json")) {
				displayJson = true;
				prettyJson = args[++i].equalsIgnoreCase("true") || args[++i].equalsIgnoreCase("yes") ? true : false;
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
		for(Integer order : orderList) {
			WordCollector collector = (inputFile != null) ?
					WordCollector.getWordCollector(order, inputFile, ignoreCase, type) :
					WordCollector.getWordCollector(order, text, type, ignoreCase);
			collector.collect();
			MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
			markovChains.put(order, markovChain);
		}
		if(orderList.size() == 1) {
			Integer ord = orderList.get(0);
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
		else {
			displayMultichains(markovChains);
		}

	}

	private static void displayMultichains(Map<Integer, MarkovChain<Word, Sentence>> markovChains) {
		
	}
	
}
