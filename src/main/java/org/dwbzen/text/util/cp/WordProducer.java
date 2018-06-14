package org.dwbzen.text.util.cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.model.Word;

import mathlib.cp.CollectorStats;
import mathlib.cp.CollectorStatsMap;
import mathlib.cp.IProducer;
import mathlib.cp.MarkovChain;
import mathlib.cp.OccurrenceProbability;

/**
 * Produces made-up Words based on the MarkovChain result from a CharacterCollector.
 * Command Line Arguments:
 * 	-seed <text>		Initial seed to use
 *  -order	n			Length of the seed, used when picking a seed from the Markov Chain.
 *  					This is set from -seed if one is provided
 *  -file <filename>	Text file containing source Words, used to create the MarkovChain for generation.
 *  					If not provided, source is taken from the command line
 *  -num n				Number of Words to produce
 *  -min n				Minimum word length, default is 4 characters
 *  -recycle n			How often to pick a new seed, default is after each produced word
 *  -ignoreCase			Ignores case (converts input to lower)
 *  -repeat n			#times to run the producer - each run produces <num> Words
 *  -sort				Sort the output.
 *  -list				Display produce Words in order produced
 *  -trace				Follow the action on seed picking. Sets trace mode on CharacterCollector
 *  -format				post-processing: TC = title case, UC = upper case, LC = lower case
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
 */
public class WordProducer implements IProducer<MarkovChain<Character, Word>, Word > { 
	
	protected static final Logger logger = LogManager.getLogger(WordProducer.class);
	static String postProcessing = "NC";	// no conversion

	private int numberToGenerate;		// #words to generate
	private Word seed = null;
	private Word originalSeed = null;
	private Word nextSeed = null;
	private int order;
	private MarkovChain<Character, Word> markovChain = null;
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private boolean sortedResult = false;
	private boolean ignoreCase = true;
	private boolean statisticalPick = true;
	private int recycleSeedNumber = 1;	
	private int recycleSeedCount = 0;	// pick a new seed every recycleSeedNumber iterations
	private int minimumLength = 4;		// doesn't save Words with fewer characters than this
	private int count = 0;
	private Collection<Word> wordListChain = new ArrayList<Word>();	// in order generated
	
	public static WordProducer instance(int order, MarkovChain<Character, Word> cstatsMap, Word seed ) {
		WordProducer producer = new WordProducer(order, cstatsMap);
		producer.setSeed(seed);
		producer.setOriginalSeed(seed);
		return producer;
	}
	
	/**
	 * /reference/drugNames.txt" -num 50  -order 3 -list
	 * @param order
	 * @param seed
	 * @param resource
	 * @return
	 */
	public static WordProducer instance(int order, Word seed, String resource) {
		WordProducer producer = null;

		return producer;
	}
	
	protected boolean loadResource(String resource) {
		boolean result = false;
		InputStream is = this.getClass().getResourceAsStream(resource);
		List<Word> wordList = new ArrayList<>();
		if(is != null) {
			try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {
				stream.forEach(s -> wordList.add(new Word(s)));
				result = true;
			}
		}
		else {
			logger.error("Unable to open " + resource);
		}
		return result;
	}
	
	protected WordProducer(int order, MarkovChain<Character, Word> cstatsMap ) {
		this.order = order;
		this.markovChain = cstatsMap;
	}
	
	@Override
	public Set<Word> produce() {
		Set<Word> generatedWords = sortedResult ? new TreeSet<Word>() : new HashSet<Word>();
		nextSeed = seed;
		for(count=0; count<numberToGenerate; count++) {
			Word word = apply(markovChain);
			if(word != null && word.toString().length() >= minimumLength) {
				logger.debug("adding: '" + word + "'");
				generatedWords.add(word);
				wordListChain.add(word);
			}
			if(++recycleSeedCount < recycleSeedNumber) {
				// reuse the current seed
				nextSeed = seed;
			}
			else {
				seed = markovChain.pickSeed();
				nextSeed = seed;
				recycleSeedCount = 0;
			}
		}
		return generatedWords;
	}
	
	@Override
	public Word apply(MarkovChain<Character, Word> cstatsMap) {
		Word generatedWord = new Word(nextSeed);
		Character nextChar = null;
		do {
			nextChar = getNextCharacter();
			logger.debug("next char: '" + nextChar + "'");
			if(!nextChar.equals(Word.TERMINAL)) {
				generatedWord.append(nextChar);
			}
		} while(!nextChar.equals(Word.TERMINAL));		// determines when to stop adding Characters
		return generatedWord;
	}
	
	protected Character getNextCharacter() {
		Character nextChar = null;
		CollectorStats<Character, Word> cstats = markovChain.get(nextSeed);
		/*
		 * it's impossible that nextSeed does not occur in collectorStatsMap
		 * This would indicate some kind of internal error so return a TERMINAL character and log an error
		 */
		if(cstats == null) {
			logger.error("No CollectorStats value for '" + nextSeed + "' returning TERMINAL");
			return Word.TERMINAL;
		}

		int occur = cstats.getTotalOccurrance();
		int keysize = cstats.size();
		int spick = random.nextInt(1, occur+1);
		int npick = random.nextInt(0, keysize);
		int i = 0;
		Map<Character, OccurrenceProbability> occurrenceProbabilityMap = cstats.getOccurrenceProbabilityMap();
		for(Character nc : occurrenceProbabilityMap.keySet()) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(nc);
			int[] range = op.getRange();
			if(statisticalPick) {
				if(spick >= range[CollectorStats.LOW] && spick <= range[CollectorStats.HIGH]) {
					// found the next character
					nextChar = nc;
					break;
				}
			}
			else if(npick == i++) {
					// found the next character
					nextChar = nc;
					break;
				}
			}
		// set the nextSeed which is an instance variable
		nextSeed = new Word(nextSeed.getWordString().substring(1) + nextChar);
		return nextChar;
	}
	
	public int getNumberOfWords() {
		return numberToGenerate;
	}

	public void setNumberOfWords(int numberOfWords) {
		this.numberToGenerate = numberOfWords;
	}

	public Word getSeed() {
		return seed;
	}

	public void setSeed(Word seed) {
		this.seed = seed;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isSortedResult() {
		return sortedResult;
	}

	public void setSortedResult(boolean sortedResult) {
		this.sortedResult = sortedResult;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public CollectorStatsMap<Character, Word> getCollectorStatsMap() {
		return markovChain;
	}

	public Word getNextSeed() {
		return nextSeed;
	}

	public void setNextSeed(Word nextSeed) {
		this.nextSeed = nextSeed;
	}

	public int getMinimumWordLength() {
		return minimumLength;
	}

	public void setMinimumWordLength(int minimumWordLength) {
		this.minimumLength = minimumWordLength;
	}

	public boolean isStatisticalPick() {
		return statisticalPick;
	}

	public void setStatisticalPick(boolean statisticalPick) {
		this.statisticalPick = statisticalPick;
	}

	public int getRecycleSeedNumber() {
		return recycleSeedNumber;
	}

	public void setRecycleSeedNumber(int recycleSeedNumber) {
		this.recycleSeedNumber = recycleSeedNumber;
	}
	
	public Word getOriginalSeed() {
		return originalSeed;
	}

	public Collection<Word> getWordListChain() {
		return this.wordListChain;
	}
	
	protected void setOriginalSeed(Word originalSeed) {
		this.originalSeed = originalSeed;
	}

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
			else if(args[i].equalsIgnoreCase("-format")) {
				// format processing
				postProcessing = args[++i];
			}
			else {
				text = args[i];
			}
		}

		// Run the CharacterCollector first
		collector = CharacterCollector.instance(order, filenames, ignoreCase);
		if(filenames.length == 0) {
			collector.setText(text);
		}
		collector.setTrace(trace);
		collector.collect();
		MarkovChain<Character, Word> markovChain = collector.getMarkovChain();
		if(seed == null) {
			seed = markovChain.pickSeed();
		}
		if(trace) {
			markovChain.display();
			markovChain.displaySummaryMap();
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

}
