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

import util.text.Word;

/**
 * Produces made-up Words based on the CollectorStats output from a CharacterCollector.
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
	private int keylen;
	private MarkovChain<Character, Word> markovChain = null;
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private boolean sortedResult = false;
	private boolean ignoreCase = true;
	private boolean reuseSeed = false;
	private boolean statisticalPick = true;
	private int recycleSeedNumber = 1;	
	private int recycleSeedCount = 1;	// pick a new seed every recycleSeedNumber iterations
	private int minimumLength = 4;	// doesn't save Words with fewer characters than this
	private int count = 0;
	private Collection<Word> wordListChain = new ArrayList<Word>();	// in order generated
	
	public static WordProducer getWordProducer(int keylen, MarkovChain<Character, Word> cstatsMap, Word seed ) {
		WordProducer producer = new WordProducer(keylen, cstatsMap);
		producer.setSeed(seed);
		producer.setOriginalSeed(seed);
		return producer;
	}
	
	protected WordProducer(int keylen, MarkovChain<Character, Word> cstatsMap ) {
		this.keylen = keylen;
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
			if(reuseSeed) {
				if(++recycleSeedCount <= recycleSeedNumber) {
					nextSeed = seed;
				}
				else {
					seed =  markovChain.pickSeed();
					nextSeed = seed;
					recycleSeedCount = 0;
				}
			}
			else {
				seed = markovChain.pickSeed();	// need a new seed for next iteration
				nextSeed = seed;
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
		 * This would indicate some kind of internal error so throw a RuntimeException
		 */
		if(cstats == null) {
			return Word.TERMINAL;
			//throw new RuntimeException("getNextCharacter(): cstats null for nextSeed: " + nextSeed);
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

	public int getKeylen() {
		return keylen;
	}

	public void setKeylen(int keylen) {
		this.keylen = keylen;
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

	public boolean isReuseSeed() {
		return reuseSeed;
	}

	public void setReuseSeed(boolean reuseSeed) {
		this.reuseSeed = reuseSeed;
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
		boolean reuse = false;
		boolean statistical = true;
		int recycleSeedNumber = 10;
		int keylen = 2;
		int repeats = 1;	// number of times to run WordProducer
		int minLength = 4;
		boolean showOrderGenerated = false;
		CharacterCollector collector = null;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				filename = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-ignoreCase")) {
				ignoreCase = true;
			}
			else if(args[i].equalsIgnoreCase("-keylen")) {
				keylen = Integer.parseInt(args[++i]);
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
			}
			else if(args[i].equalsIgnoreCase("-sort")) {
				sort = true;
			}
			else if(args[i].startsWith("-statis")) {
				statistical = args[++i].equalsIgnoreCase("Y");
			}
			else if(args[i].equalsIgnoreCase("-reuse")) {
				reuse = true;
			}
			else if(args[i].startsWith("-list")) {
				showOrderGenerated = true;
			}
			else {
				text = args[i];
			}
		}

		// Run the CharacterCollector first
		collector = CharacterCollector.getCharacterCollector(keylen, filename, ignoreCase);
		if(filename == null) {
			collector.setText(text);
		}
		collector.collect();
		MarkovChain<Character, Word> markovChain = collector.getMarkovChain();
		//cstatsMap.display();
		//cstatsMap.displaySummaryMap();
		if(seed == null) {
			seed = markovChain.pickSeed();
		}
		
		// Run the WordProducer on the results
		for(int nr=1; nr<=repeats; nr++) {
			WordProducer producer = WordProducer.getWordProducer(keylen, markovChain, seed);
			producer.setSortedResult(sort);
			producer.setNumberOfWords(num);
			producer.setReuseSeed(reuse);
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
