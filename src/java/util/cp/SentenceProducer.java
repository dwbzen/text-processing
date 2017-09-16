package util.cp;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.text.Book;
import util.text.Book.TYPE;
import util.text.Sentence;
import util.text.Word;

public class SentenceProducer  implements IProducer<MarkovChain<Word, Sentence>, Sentence > {
	
	protected static final Logger log = LogManager.getLogger(SentenceProducer.class);
	private int numberToGenerate;		// #words to generate
	private Sentence seed = null;
	private Sentence originalSeed = null;
	private Sentence nextSeed = null;
	private int keylen;
	private MarkovChain<Word, Sentence> markovChain = null;
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private boolean sortedResult = false;
	private boolean ignoreCase = true;
	private boolean reuseSeed = false;
	private boolean statisticalPick = true;
	private int recycleSeedNumber = 1;	
	private int recycleSeedCount = 1;	// pick a new seed every recycleSeedNumber iterations
	private int minimumLength = 2;	// doesn't save Sentences with fewer words than this
	private int count = 0;

	public static SentenceProducer getSentenceProducer(int keylen, MarkovChain<Word, Sentence> cstatsMap, Sentence seed ) {
		SentenceProducer producer = new SentenceProducer(keylen, cstatsMap);
		producer.setSeed(seed);
		producer.setOriginalSeed(seed);
		return producer;
	}
	
	protected SentenceProducer(int keylen, MarkovChain<Word, Sentence> cstatsMap ) {
		this.keylen = keylen;
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
	
	public boolean isReuseSeed() {
		return reuseSeed;
	}

	public void setReuseSeed(boolean reuseSeed) {
		this.reuseSeed = reuseSeed;
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
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		
		Sentence seed = null;
		int num = 10;
		String filename = null;
		String text = null;
		boolean ignoreCase = false;
		boolean sort = false;
		boolean reuse = false;
		boolean statistical = true;
		int recycleSeedNumber = 10;
		boolean debug = false;
		int keylen = 2;
		String textType = null;
		WordCollector collector = null;
		int repeats = 1;
		int minLength = 2;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				filename = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-debug")) {
				debug = true;
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
			else if(args[i].equalsIgnoreCase("-reuse")) {
				reuse = true;
			}
			else if(args[i].equalsIgnoreCase("-type")) {
				textType = args[++i];
			}
			else {
				text = args[i];
			}
		}

		Book.TYPE type = (textType != null && textType.equalsIgnoreCase("verse")) ? TYPE.VERSE : TYPE.PROSE;
		// Run the CharacterCollector first
		collector = WordCollector.getWordCollector(keylen, filename, ignoreCase, type);
		if(filename == null) {
			collector.setText(text);
		}
		collector.collect();
		MarkovChain<Word, Sentence> markovChain = collector.getMarkovChain();
		if(debug) {
			markovChain.display();
			markovChain.displaySummaryMap();
		}
		if(seed == null) {
			seed = markovChain.pickSeed();
		}
		
		// run the producer on the results
		for(int nr=1; nr<=repeats; nr++) {
			SentenceProducer producer = SentenceProducer.getSentenceProducer(keylen, markovChain, seed);
			producer.setSortedResult(sort);
			producer.setNumberToGenerate(num);
			producer.setReuseSeed(reuse);
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
