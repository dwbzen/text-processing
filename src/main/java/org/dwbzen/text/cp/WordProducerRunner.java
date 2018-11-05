package org.dwbzen.text.cp;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.model.Word;

import mathlib.cp.CollectorStats;
import mathlib.cp.MarkovChain;
import mathlib.cp.OccurrenceProbability;
import mathlib.cp.OutputStyle;

/**
 * Produces made-up Words based on the MarkovChain result from a CharacterCollector.
 * Command Line Arguments:
 * 	-seed <text>		Initial seed to use
 *  -order	n			Length of the seed, used when picking a seed from the Markov Chain.
 *  					This is set from -seed if one is provided
 *  -file <file list>	Comma-delimited list of text file names containing source Words.
 *  					Used to create the MarkovChain for generation.
 *  					If not provided, source is taken from the command line
 *  -num n				Number of Words to produce
 *  -min n				Minimum word length, default is 4 characters
 *  -recycle n			How often to pick a new seed, default is after each produced word
 *  -ignoreCase			Ignores case (converts input to lower)
 *  -repeat n			#times to run the producer - each run produces <num> Words
 *  -sort  				Sort the output.
 *  -list				Display produce Words in order produced
 *  -trace				Follow the action on seed picking. Sets trace mode on CharacterCollector
 *  -format				post-processing: TC = title case, UC = upper case, LC = lower case
 *  -init				choose initial seed only (start of word)
 *  -pos				Specify parts of speech. Assumes file list includes a POS file.
 *  
 *  
 * If you wanted to use the same seed, for example " KA" for womens names, specify -recycle number
 * to be > number of words to produce (-num) times #repeats (-repeat):
 * 
 * -file "build\resources\main\reference\femaleFirstNames.txt" -num 50 -order 3  -list -recycle 2
 * 
 * -file "build/resources/main/reference/drugNames.txt" -num 50  -order 3 -list
 *  
 * @author don_bacon
 *
 */public class WordProducerRunner {
	protected static final Logger logger = LogManager.getLogger(WordProducer.class);
	static String postProcessing = "NC";	// no conversion
	
	
	/**
	 * Produces Words using the results of CharacterCollector.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		
		Word seed = null;
		int num = 10;
		String[] filenames = {};
		Optional<String> posOption = Optional.empty();
		String text = null;
		boolean ignoreCase = false;
		boolean sort = false;
		boolean statistical = true;
		int recycleSeedNumber = 1;		// how often to pick a new seed.
		int order = 2;
		int repeats = 1;	// number of times to run WordProducer
		int minLength = 4;
		boolean showOrderGenerated = false;
		CharacterCollector collector = null;
		boolean trace = false;
		boolean pickInitialSeed = false;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				filenames =  args[++i].split(",");
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
			else if(args[i].equalsIgnoreCase("-repeat")) {
				repeats = Integer.parseInt(args[++i]);
			}
			else if(args[i].startsWith("-min")) {
				minLength = Integer.parseInt(args[++i]);
			}
			else if(args[i].startsWith("-recycle")) {
				recycleSeedNumber = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-seed")){
				seed = new Word(args[++i]);
				order = seed.size();
			}
			else if(args[i].equalsIgnoreCase("-sort")) {
				sort = true;
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = true;
			}
			else if(args[i].startsWith("-statis")) {
				statistical = args[++i].equalsIgnoreCase("Y");
			}
			else if(args[i].startsWith("-list")) {
				showOrderGenerated = true;
			}
			else if(args[i].startsWith("-format")) {
				// format processing
				postProcessing = args[++i];
			}
			else if(args[i].startsWith("-init")) {
				// pick initial seeds - that start words
				pickInitialSeed = true;
			}
			else if(args[i].equalsIgnoreCase("-pos")) {
				// file is a part of speech file
				posOption = Optional.of(args[++i]);
			}
			else {
				text = args[i];
			}
		}

		// Run the CharacterCollector first
		collector = posOption.isPresent() ? 
			CharacterCollector.instance(order, filenames, ignoreCase, posOption.get()) :
			CharacterCollector.instance(order, filenames, ignoreCase);
		if(filenames.length == 0) {
			collector.setText(text);
		}
		collector.setTrace(trace);
		collector.collect();
		MarkovChain<Character, Word> markovChain = collector.getMarkovChain();
		markovChain.setPickInitialSeed(pickInitialSeed);
		if(seed == null) {
			seed = markovChain.pickSeed();
		}
		if(trace) {
			displaySortedMarkovChainText(markovChain, OutputStyle.TEXT);
			System.out.print(markovChain.getSummaryMapText());
		}
		// Run the WordProducer on the results
		for(int nr=1; nr<=repeats; nr++) {
			WordProducer producer = WordProducer.instance(order, markovChain, seed);
			producer.setSortedResult(sort);
			producer.setNumberOfWords(num);
			producer.setRecycleSeedNumber(recycleSeedNumber);
			producer.setStatisticalPick(statistical);
			producer.setMinimumWordLength(minLength);
			Set<Word> words = producer.produce();
			Collection<Word> wordCollection = showOrderGenerated ?  producer.getWordListChain() : words;
			
			PrintStream stream = System.out;
			wordCollection.stream().forEach(word -> outputWord(stream, word));

		}
	}
	
	public static void outputWord(PrintStream stream, Word word) {
		String wordString = null;
		if(postProcessing.equalsIgnoreCase("NC")) {
			wordString = word.toString().trim();
		}
		else if(postProcessing.equalsIgnoreCase("UC")) {
			wordString = word.toString().toUpperCase();
		}
		else if(postProcessing.equalsIgnoreCase("LC")) {
			wordString = word.toString().toLowerCase();
		}
		else if(postProcessing.equalsIgnoreCase("TC")) {
			wordString = word.toString().toLowerCase().trim();
			StringBuilder sb = new StringBuilder(wordString.substring(0, 1).toUpperCase());
			sb.append(wordString.substring(1));
			wordString = sb.toString();
		}
		else {
			wordString = word.toString();
		}
		stream.println(wordString);
	}
	
	public static void displaySortedMarkovChainText(MarkovChain<Character, Word> markovChain, OutputStyle outputStyle) {
		Map<?,?> sortedChain = markovChain.sortByValue();
		for(Object obj : sortedChain.keySet()) {
			Word word = (Word)obj;
			@SuppressWarnings("unchecked")
			CollectorStats<Character, Word> cstats = (CollectorStats<Character, Word>)sortedChain.get(word);
			Map<Character,OccurrenceProbability> sortedStats = (Map<Character,OccurrenceProbability>)cstats.sortByValue();
			System.out.println(word + "\t" + cstats.getTotalOccurrance());
			for(Character key : sortedStats.keySet()) {
				OccurrenceProbability op = sortedStats.get(key);
				System.out.println("\t" + key + "\t" + op.getOccurrence() + "\t" + op.getProbability());
			}
		}
	}

}
