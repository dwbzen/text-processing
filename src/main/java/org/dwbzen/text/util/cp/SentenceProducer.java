package org.dwbzen.text.util.cp;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.model.Book;
import org.dwbzen.text.util.model.Book.TYPE;
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

	public void setSeed(Sentence seed) {
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

	public MarkovChain<Word, Sentence> getMarkovChain() {
		return markovChain;
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
	
	public Sentence getOriginalSeed() {
		return originalSeed;
	}

	protected void setOriginalSeed(Sentence originalSeed) {
		this.originalSeed = originalSeed;
	}
	
	public Sentence getNextSeed() {
		return nextSeed;
	}

	public void setNextSeed(Sentence nextSeed) {
		this.nextSeed = nextSeed;
	}

	public int getNumberToGenerate() {
		return numberToGenerate;
	}

	public void setNumberToGenerate(int numberToGenerate) {
		this.numberToGenerate = numberToGenerate;
	}

	public int getRecycleSeedCount() {
		return recycleSeedCount;
	}

	public void setRecycleSeedCount(int recycleSeedCount) {
		this.recycleSeedCount = recycleSeedCount;
	}

	public int getMinimumLength() {
		return minimumLength;
	}

	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}

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
		boolean pretty = false;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
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
		collector = WordCollector.getWordCollector(order, ignoreCase, type, collectorArg);
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
			Set<Sentence> sentences = producer.produce();
			for(Sentence sentence : sentences) {
				System.out.println(sentence.toString());
			}
		}
	}
	
}
