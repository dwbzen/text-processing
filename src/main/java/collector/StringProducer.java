package collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @deprecated use CharacterCollector, WordProducer in util.cp package
 * 
 * Produces Strings from random selections from CollectorStats<String> instance.
 * If a seed string is not specified, one is chosen (a Map key) at random from the 
 * StringCollector results -  Map<String, CollectorStats<String>> getCollectorStatsMap()
 * 
 * Example usage (export runable JAR file):
 * java -jar StringProducer.jar -file C:\\data\\text\\allFirstNames.txt -keylen 2 -ignoreCase -num 20
 * java -jar StringProducer.jar -keylen 2 -ignoreCase -num 50 < allFirstNames.txt
 *
 * @author Don_Bacon
 *
 */
public class StringProducer implements IProducer<String> {
	protected static final Logger log = LogManager.getLogger(StringProducer.class);

	private String seed;
	private String nextSeed = "";
	private int numberOfWords;		// #words to generate
	private File file = null;
	private String text = null;
	private boolean ignoreCase = true;
	private int keylen; 
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private boolean sortedResult = false;
	
	public StringProducer(String aSeed) {
		this.seed = aSeed;
	}
	
	@Override
	public Set<String> produce(Map<String, CollectorStats<String>> collectorStatsMap) {
		Set<String> generatedText = sortedResult ? new TreeSet<String>() : new HashSet<String>();
		int n = 0;		// #generated so far
		nextSeed = seed;
		StringBuffer sb = new StringBuffer(seed);
		do {
			String nextChar = getNextChar(collectorStatsMap);
			sb.append(nextChar);
			if(nextChar.equals(" ")) {	// end of this word
				n++;
				generatedText.add(sb.toString().trim());
				sb = new StringBuffer();
			}
		} while(n < numberOfWords);
		
		return generatedText;
	}
	
	private String getNextChar(Map<String, CollectorStats<String>> collectorStatsMap) {
		String  nextChar = "";
		CollectorStats<String> cstats = collectorStatsMap.get(nextSeed);
		log.debug("nextSeed: " + nextSeed + ", cstats: " + ((cstats==null)? "null" : "not null"));
		int occur = cstats.getTotalOccurrance();
		int pick = random.nextInt(1, occur+1);
		Map<String, OccurrenceProbability> occurrenceProbabilityMap = cstats.getOccurrenceProbabilityMap();
		for(String nc : occurrenceProbabilityMap.keySet()) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(nc);
			int[] range = op.getRange();
			if(pick >= range[CollectorStats.LOW] && pick <= range[CollectorStats.HIGH]) {
				// found the next character
				nextChar = nc;
				// set the nextSeed which is an instance variable
				nextSeed = nextSeed.substring(1) + nc;
				break;
			}
		}
		return nextChar;
	}
	
	/**
	 * Produces Strings using the results of StringCollector.
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		
		String seed = null;
		int num = 10;
		
		String filename = null;
		String text = null;
		File file = null;
		boolean ignoreCase = false;
		boolean sort = false;
		int keylen = 2;
		ICollector<String> collector;

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
			else if(args[i].equalsIgnoreCase("-seed")){
				seed = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-sort")) {
				sort = true;
			}
			else {
				text = args[i];
			}
		}
		if(filename != null) {
			file = new File(filename);
		}
		try {
			collector = new StringCollector(keylen, file, text, ignoreCase);
			collector.collect();
			Map<String, CollectorStats<String>> collectorStatsMap = collector.getCollectorStatsMap();
			
			if(seed == null || seed.length() < 2) {
				seed = pickSeed(collectorStatsMap);
				log.debug("pick seed: " + seed);
			}
			StringProducer producer = new StringProducer(seed);
			producer.setNumberOfWords(num);
			producer.setSortedResult(sort);
			Set<String> wordList = producer.produce(collectorStatsMap);
			for(String s:wordList) {
				System.out.println(s);
			}
		} catch(FileNotFoundException ex) {
			System.err.println("Invalid file specified for Collector");
			return;
		}
	}
	
	/**
	 * Pick a likely seed from the collector stats Map keys
	 * @param collectorStatsMap Map<String, CollectorStats<String>> 
	 * @return String candidate seed
	 */
	public static String pickSeed(Map<String, CollectorStats<String>> collectorStatsMap) {
		String aSeed = null;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		List<String> keys = new ArrayList<String>();
		for(String key : collectorStatsMap.keySet()) {
			if(key.startsWith(" ") || key.endsWith(" ")) {
				continue;
			}
			keys.add(key);
		}
		int totalAll = keys.size();
		int randomIndex = random.nextInt(0, totalAll);
		aSeed = keys.get(randomIndex);
		return aSeed;
	}

	@Override
	public Class<String> getProducerSubsetClass() {
		return String.class;
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public int getNumberOfWords() {
		return numberOfWords;
	}

	public void setNumberOfWords(int numberOfWords) {
		this.numberOfWords = numberOfWords;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
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
	
}
