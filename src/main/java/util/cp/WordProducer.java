package util.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.cp.CollectorStats;
import mathlib.cp.CollectorStatsMap;
import mathlib.cp.IProducer;
import mathlib.cp.MarkovChain;
import mathlib.cp.OccurrenceProbability;
import util.text.Word;

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
 *  
 *  
 * If you wanted to use the same seed, for example " KA" for womens names, specify -recycle number
 * to be > number of words to produce (-num) times #repeats (-repeat):
 * 
 * -file "build\resources\main\reference\femaleFirstNames.txt" -num 50 -order 3  -list -seed " KA" -recycle 50
 *  
 * @author don_bacon
 *
 */
public class WordProducer implements IProducer<MarkovChain<Character, Word>, Word > { 
	
	protected static final Logger log = LogManager.getLogger(WordProducer.class);
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
	
	public static WordProducer getWordProducer(int order, MarkovChain<Character, Word> cstatsMap, Word seed ) {
		WordProducer producer = new WordProducer(order, cstatsMap);
		producer.setSeed(seed);
		producer.setOriginalSeed(seed);
		return producer;
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
				log.debug("adding: '" + word + "'");
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
			log.debug("next char: '" + nextChar + "'");
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
			log.error("No CollectorStats value for '" + nextSeed + "' returning TERMINAL");
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
		String filename = null;
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
				filename = args[++i];
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
			else {
				text = args[i];
			}
		}

		// Run the CharacterCollector first
		collector = CharacterCollector.getCharacterCollector(order, filename, ignoreCase);
		if(filename == null) {
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
			WordProducer producer = WordProducer.getWordProducer(order, markovChain, seed);
			producer.setSortedResult(sort);
			producer.setNumberOfWords(num);
			producer.setRecycleSeedNumber(recycleSeedNumber);
			producer.setStatisticalPick(statistical);
			producer.setMinimumWordLength(minLength);
			Set<Word> words = producer.produce();
			Collection<Word> wordCollection = showOrderGenerated ?  producer.getWordListChain() : words;
			
			for(Word word : wordCollection) {
				System.out.println(word.toString().trim());
			}
		}
	}

}
