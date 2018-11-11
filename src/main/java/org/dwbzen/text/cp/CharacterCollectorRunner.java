package org.dwbzen.text.cp;

import java.io.IOException;

import org.dwbzen.text.util.model.Word;

import mathlib.cp.MarkovChain;

public class CharacterCollectorRunner {

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
			else {
				text = args[i];
			}
		}
		CharacterCollector collector = CharacterCollector.instance(order, filenames, ignoreCase);
		if(filenames.length == 0) {
			text = ignoreCase ? text.toLowerCase() : text;
			collector.setText(text);
		}
		collector.setTrace(trace);
		collector.collect();
		MarkovChain<Character, Word> markovChain = collector.getMarkovChain();
		System.out.print(markovChain.getMarkovChainDisplayText()); 
		System.out.print(markovChain.getSummaryMapText());
		System.out.println(markovChain.toJson());
		
	}


}
