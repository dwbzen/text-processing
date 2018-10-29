package org.dwbzen.text.cp;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.model.Sentence;
import org.dwbzen.text.util.model.Word;

import mathlib.cp.CollectorStats;
import mathlib.cp.IProducer;
import mathlib.cp.MarkovChain;
import mathlib.cp.OccurrenceProbability;

/**
 * Produces Sentences based on the MarkovChain result from a WordCollector.
 * Command Line Arguments:
 * 	-seed <text>		Initial seed to use
 *  -order	n			Length of the seed, used when picking a seed from the Markov Chain.
 *  					This is set from -seed if one is provided
 *  -file <filename>	Text file containing source Words, used to create the MarkovChain for generation.
 *  					If not provided, source is taken from the command line
 *  -num n				Number of Sentences to produce
 *  -minLength n		Minimum Sentence length, default is 2 Words
 *  -recycle n			How often to pick a new seed, default is after each produced Sentence
 *  -ignoreCase			Ignores case (converts input to lower)
 *  -repeat n			#times to run the producer - each run produces <num> Sentences
 *  -sort				sort the Sentences on output
 *  -type				verse | prose | technical  - default is prose
 *  -trace				Follow the action on seed picking. Sets trace mode on CharacterCollector
 *  
 *   @author don_bacon
 */
public class SentenceProducer  implements IProducer<MarkovChain<Word, Sentence>, Sentence > {
	
	protected static final Logger log = LogManager.getLogger(SentenceProducer.class);
	private int numberToGenerate;		// #words to generate
	private Sentence seed = null;
	private Sentence originalSeed = null;
	private Sentence nextSeed = null;
	private int order;
	private MarkovChain<Word, Sentence> markovChain = null;
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private boolean sortedResult = false;
	private boolean ignoreCase = true;
	private boolean statisticalPick = true;
	private int recycleSeedNumber = 1;	
	private int recycleSeedCount = 0;	// pick a new seed every recycleSeedNumber iterations
	private int minimumLength = 2;	// doesn't save Sentences with fewer words than this
	private int count = 0;

	public static SentenceProducer getSentenceProducer(int order, MarkovChain<Word, Sentence> cstatsMap, Sentence seed ) {
		SentenceProducer producer = new SentenceProducer(order, cstatsMap);
		producer.setSeed(seed);
		producer.setOriginalSeed(seed);
		return producer;
	}
	
	protected SentenceProducer(int order, MarkovChain<Word, Sentence> cstatsMap ) {
		this.order = order;
		this.markovChain = cstatsMap;
	}

	@Override
	public Set<Sentence> produce() {
		Set<Sentence> generatedSentences = sortedResult ? new TreeSet<Sentence>() : new LinkedHashSet<Sentence>(numberToGenerate);
		nextSeed = seed;
		for(count=0; count<numberToGenerate; count++) {
			Sentence sentence = apply(markovChain);
			if(sentence != null && sentence.size() >= minimumLength) {
				log.debug("adding: '" + sentence + "'");
				generatedSentences.add(sentence);
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
		return generatedSentences;
	}

	@Override
	public Sentence apply(MarkovChain<Word, Sentence> cstatsMap) {
		Sentence generatedSentence = new Sentence(nextSeed);
		Word nextWord = null;
		do {
			nextWord = getNextWord();
			log.debug("next word: '" + nextWord + "'");
			if(!nextWord.equals(Sentence.TERMINAL)) {
				generatedSentence.add(nextWord);
			}
		}while(!nextWord.equals(Sentence.TERMINAL));  	// determine when to stop adding words.
		return generatedSentence;
	}
	
	protected Word getNextWord() {
		Word nextWord = null;
		CollectorStats<Word, Sentence> cstats = markovChain.get(nextSeed);
		/*
		 * it's impossible that nextSeed does not occur in collectorStatsMap
		 * This would indicate some kind of internal error so throw a RuntimeException
		 */
		if(cstats == null) {
			throw new RuntimeException("getNextCharacter(): cstats null for nextSeed: " + nextSeed);
		}
		int occur = cstats.getTotalOccurrance();
		int keysize = cstats.size();
		int spick = random.nextInt(1, occur+1);
		int npick = random.nextInt(0, keysize);
		int i = 0;
		Map<Word, OccurrenceProbability> occurrenceProbabilityMap = cstats.getOccurrenceProbabilityMap();
		for(Word nc :  occurrenceProbabilityMap.keySet()) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(nc);
			int[] range = op.getRange();
			if(statisticalPick) {
				if(spick >= range[CollectorStats.LOW] && spick <= range[CollectorStats.HIGH]) {
					// found the next character
					nextWord = nc;
					break;
				}
			}
			else if(npick == i++) {
					// found the next character
					nextWord = nc;
					break;
			}
		}
		// set the nextSeed which is an instance variable
		nextSeed = new Sentence(nextSeed.subset(1), nextWord);
		log.debug("nextSeed now: {" + nextSeed + "}");
		return nextWord;
	}

	public Sentence getSeed() {
		return seed;
	}

	public SentenceProducer setSeed(Sentence seed) {
		this.seed = seed;
		return this;
	}

	public int getOrder() {
		return order;
	}

	public SentenceProducer setOrder(int order) {
		this.order = order;
		return this;
	}

	public boolean isSortedResult() {
		return sortedResult;
	}

	public SentenceProducer setSortedResult(boolean sortedResult) {
		this.sortedResult = sortedResult;
		return this;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public SentenceProducer setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		return this;
	}

	public MarkovChain<Word, Sentence> getMarkovChain() {
		return markovChain;
	}

	public int getMinimumWordLength() {
		return minimumLength;
	}

	public SentenceProducer setMinimumWordLength(int minimumWordLength) {
		this.minimumLength = minimumWordLength;
		return this;
	}

	public boolean isStatisticalPick() {
		return statisticalPick;
	}

	public SentenceProducer setStatisticalPick(boolean statisticalPick) {
		this.statisticalPick = statisticalPick;
		return this;
	}

	public int getRecycleSeedNumber() {
		return recycleSeedNumber;
	}

	public SentenceProducer setRecycleSeedNumber(int recycleSeedNumber) {
		this.recycleSeedNumber = recycleSeedNumber;
		return this;
	}
	
	public Sentence getOriginalSeed() {
		return originalSeed;
	}

	protected SentenceProducer setOriginalSeed(Sentence originalSeed) {
		this.originalSeed = originalSeed;
		return this;
	}
	
	public Sentence getNextSeed() {
		return nextSeed;
	}

	public SentenceProducer setNextSeed(Sentence nextSeed) {
		this.nextSeed = nextSeed;
		return this;
	}

	public int getNumberToGenerate() {
		return numberToGenerate;
	}

	public SentenceProducer setNumberToGenerate(int numberToGenerate) {
		this.numberToGenerate = numberToGenerate;
		return this;
	}

	public int getRecycleSeedCount() {
		return recycleSeedCount;
	}

	public SentenceProducer setRecycleSeedCount(int recycleSeedCount) {
		this.recycleSeedCount = recycleSeedCount;
		return this;
	}

	public int getMinimumLength() {
		return minimumLength;
	}

	public SentenceProducer setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
		return this;
	}

	
}
