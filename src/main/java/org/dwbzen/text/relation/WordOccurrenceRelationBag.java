package org.dwbzen.text.relation;


import java.util.ArrayList;
import java.util.List;

import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.cp.OutputStyle;
import mathlib.relation.OccurrenceRelationBag;

public class WordOccurrenceRelationBag extends OccurrenceRelationBag<Word, Sentence, Book> {
	private static final long serialVersionUID = 1L;
	static OutputStyle outputStyle = OutputStyle.TEXT;
	static boolean trace = false;
	
	public WordOccurrenceRelationBag(int degree) {
		super(degree);
	}

	public static void main(String...args) {
		String[] filenames = {};
		List<OutputStyle> outputStyles = new ArrayList<>();
		String text = null;
		boolean sorted = false;
		String orderstring = null;
		boolean ignoreCase = false;
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
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = true;
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
		
		WordOccurrenceRelationBag occurrenceRelationBag = new WordOccurrenceRelationBag(order);
		WordOccurrenceRelationBag  targetoccurrenceRelationBag = occurrenceRelationBag;
		
	}
}
