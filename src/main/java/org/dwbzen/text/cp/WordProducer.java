package org.dwbzen.text.cp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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


public class WordProducer implements IProducer<MarkovChain<Character, Word>, Word > { 
	
	protected static final Logger logger = LogManager.getLogger(WordProducer.class);

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
	private int maximumLength = 12;
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
	public Set<Word> produce(boolean enableDisplay) {
		Set<Word> generatedWords = sortedResult ? new TreeSet<Word>() : new HashSet<Word>();
		nextSeed = seed;
		while(count<numberToGenerate) {
			Word word = apply(markovChain);
			if(word != null && word.size() >= minimumLength) {
				logger.debug("adding: '" + word + "'");
				if(enableDisplay) {
					System.out.println(word);
				}
				generatedWords.add(word);
				wordListChain.add(word);
				count++;
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
		if(nextSeed.contains(Word.TERMINAL)) {
			return new Word();
		}
		Word generatedWord = new Word(nextSeed);
		Character nextChar = null;
		do {
			nextChar = getNextCharacter();
			logger.debug("next char: '" + nextChar + "'");
			if(! (nextChar.equals(Word.TERMINAL) || nextChar.equals(Word.NULL_VALUE))) {	// '¶'  '§'
				generatedWord.append(nextChar);
			}
		} while(!nextChar.equals(Word.TERMINAL) && generatedWord.size() < maximumLength);		// determines when to stop adding Characters
		return generatedWord;
	}
	
	protected Character getNextCharacter() {
		Character nextChar = null;
		CollectorStats<Character, Word> cstats = markovChain.get(nextSeed);
		/*
		 * it's impossible that nextSeed does not occur in the MarkovChain
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
		if(nextChar != Word.NULL_VALUE) {
			nextSeed = new Word(nextSeed.getWordString().substring(1) + nextChar);
		}
		else {
			nextSeed = markovChain.pickSeed();
		}
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

	public int getMaximumLength() {
		return maximumLength;
	}

	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

}
