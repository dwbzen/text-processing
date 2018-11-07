package org.dwbzen.text.cp;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Set;

import org.dwbzen.text.cp.WordCollectorRunner.WordCollectorBuilder;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;
import org.dwbzen.text.util.model.Book.TYPE;

import mathlib.cp.MarkovChain;

public class SentenceProducerRunner {

	/**
	 * Produces Strings using the results of StringCollector.
	 * TODO: implement "pretty" printing
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		
		Sentence seed = null;
		int num = 10;
		String inputFile = null;
		String text = null;
		boolean ignoreCase = false;
		boolean sort = false;
		boolean statistical = true;
		int recycleSeedNumber = 1;		// how often to pick a new seed.
		boolean trace = false;
		int order = 2;
		String textType = null;
		WordCollector collector = null;
		int repeats = 1;
		int minLength = 3;
		int maxLength = 10;
		boolean pretty = false;
		String schema = null;
		boolean enableDisplay = true;	// display results as they are produced
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-schema")) {
				schema = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = true;
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				order = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-num")) {
				num = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-minLength")) {
				minLength = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-maxLength")) {
				maxLength = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-repeat")) {
				repeats = Integer.parseInt(args[++i]);
			}
			else if(args[i].startsWith("-recycle")) {
				recycleSeedNumber = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-seed")){
				seed = new Sentence(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-sort")) {
				sort = true;
			}
			else if(args[i].startsWith("-statis")) {
				statistical = args[++i].equalsIgnoreCase("Y");
			}
			else if(args[i].equalsIgnoreCase("-type")) {
				textType = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-pretty")) {
				pretty = true;
			}
			else {
				text = args[i];
			}
		}

		Book.TYPE type = (textType != null && textType.equalsIgnoreCase("verse")) ? TYPE.VERSE : TYPE.PROSE;
		// Run the WordCollector first
		String[] collectorArg = new String[1];
		collectorArg[0] = (inputFile != null) ? "file:" + inputFile : text;
		Optional<String> optionalSchema = Optional.ofNullable(schema);
		collector = WordCollectorBuilder.build(order, ignoreCase, type, optionalSchema, collectorArg);
		collector.collect();
		MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
		if(trace) {
			System.out.print(markovChain.getMarkovChainDisplayText());
			System.out.print(markovChain.getSummaryMapText());
		}
		if(seed == null) {
			seed = markovChain.pickSeed();
		}
		
		// run the producer on the results
		for(int nr=1; nr<=repeats; nr++) {
			SentenceProducer producer = SentenceProducer.getSentenceProducer(order, markovChain, seed);
			producer.setSortedResult(sort);
			producer.setNumberToGenerate(num);
			producer.setRecycleSeedNumber(recycleSeedNumber);
			producer.setStatisticalPick(statistical);
			producer.setMinimumLength(minLength);
			producer.setMaximumLength(maxLength);
			Set<Sentence> sentences = producer.produce(enableDisplay);
			if(!enableDisplay) {
				PrintStream stream = System.out;
				sentences.stream().forEach(s -> stream.println(s));
			}
		}
	}

}
