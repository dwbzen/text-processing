package collector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;

/**
 * @deprecated use CharacterCollector, WordProducer in util.cp package
 * 
 * Analyzes text string(s) and collects substring statistics that
 * can be used by a generation (Producer) class.
 * For a given string (inline or from a file/stream), this examines all the substrings
 * of a given length 2 to n (n <= 5), and records the #instances of each character that
 * follows that substring. Substrings formed left to right advancing  1 character each iteration.
 * For example, given the contents of the file maleFirstNames.txt (in resources)
 * and a substring length of 2, a subset of the collection stats includes:
 * 
 * Substring/      Next      Number of
 * Total#		Character	Occurrences		Range	Probability
   ----------	---------	-----------		-----	-----------------
  'ag'		2
					'o'				1			1,1		0.5
					'u'				1			2,2		0.5
   'ah'		13
			    	' '				9			1,9		0.6923076923076923
			    	'a'				2			10,11	0.15384615384615385
			    	'm'				2			12,13	0.15384615384615385
   'ai'		15
			    	' '				1			1,1		0.06666666666666667
			   		'a'				2			2,3		0.13333333333333333
			   		'g'				3			4,6		0.2
			   		'l'				1			7,7		0.06666666666666667
			   		'm'				1			8,8		0.06666666666666667
			   		'n'				5			9,13	0.3333333333333333
			   		'r'				2			14,15	0.13333333333333333
 * For each substring, the total number of occurrences is the number of times
 * that string appears in the sample set. Number of occurrences
 * is the number of times that substring is followed by that Next Character.
 * The range is a cumulative sum of the number of occurrences expressed
 * as low/high values to make random selection easier in Producer classes.
 * 
 * Collection options include case sensitivity (default is no)
 * and individual character maps such as "\n" (new line) to <sp> (space)
 * although this option is not implemented yet.
 * Input source can be an inline text string, file contents, or stdin.
 * 
 * The Collector output data structure is Map<String, CollectorStats<String>>
 * where the Map key is the substrings (in the sample set)
 * 
 * Example usage (export runable JAR file):
 * java -jar StringCollector.jar -file C:\\data\\text\\allFirstNames.txt -keylen 2 -ignoreCase -noprint -summary
 * java -jar StringCollector.jar keylen 2 -ignoreCase -noprint -summary < allFirstNames.txt
 * 
 * @author Don_Bacon
 *
 */
public class StringCollector implements ICollector<String> {
	protected static final org.apache.logging.log4j.Logger log = LogManager.getLogger(StringCollector.class);

	private File file = null;
	private String text = null;
	private boolean ignoreCase = true; 
	private boolean skipOnBlank = false;	// if true skips processing for a subset that starts with a blank
	private int keylen; 
	private Map<String, CollectorStats<String>> collectorStatsMap = new TreeMap<String, CollectorStats<String>>();
	private Map<String, Integer> summaryMap = new  TreeMap<String, Integer>();
	
	public StringCollector(int keylen, File file, String text, boolean ignorecaseflag) throws FileNotFoundException {
		this.file = file;
		this.text = text;
		this.ignoreCase = ignorecaseflag;
		this.keylen = keylen;
		setText(getAllText());
	}
	
	private String getAllText() throws FileNotFoundException {
		StringBuffer sb = new StringBuffer();
		BufferedReader reader = null;
		try {
			if(file != null) {
				reader = new BufferedReader(new FileReader(file));
			}
			else {
				reader = new BufferedReader(new InputStreamReader(System.in));
			}
			String line = null;
			while((line = reader.readLine()) != null) {
				if(ignoreCase) {
					sb.append(line.toLowerCase().trim());
				}
				else {
					sb.append(line);
				}
				sb.append(" ");
			}
			reader.close();
		} catch(Exception ex) {
			System.err.println(ex.getMessage());
			throw new FileNotFoundException(ex.getMessage());
		}
		if(this.text != null) {
			sb.append(this.text);
		}
		return sb.toString();
	}

	/**
	 * Collect stats for each subset of length keylen of provided text
	 * Ignore subsets that begin with a space.
	 * Creates the summaryMap when complete.
	 */
	@Override
	public void collect() {
		for(int i=0; i<text.length(); i++) {
			if(i+keylen < text.length()) {
				String subset = text.substring(i, i+keylen);
				if(skipOnBlank && subset.startsWith(" ")) {
					continue;
				}
				String nextChar = String.valueOf(text.charAt(i+keylen));
				if(collectorStatsMap.containsKey(subset)) {
					CollectorStats<String> collectorStats = collectorStatsMap.get(subset);
					collectorStats.addOccurrence(nextChar);
				}
				else {
					List<String> ls = new ArrayList<String>();
					for(int j=0; j<subset.length(); j++) {
						ls.add(subset.substring(j, j+1));
					}
					CollectorStats<String> collectorStats = new CollectorStats<String>(keylen);
					collectorStats.setSubset(ls);
					collectorStats.addOccurrence(nextChar);
					collectorStatsMap.put(subset, collectorStats);
				}
			}
		}
		createSummaryMap();
		return;
	}
	
	/**
	 * This is valid only after collect() is invoked
	 * otherwise the Map will be empty.
	 */
	protected Map<String, Integer> createSummaryMap() {
		for(String key : collectorStatsMap.keySet()) {
			CollectorStats<String> cstats = collectorStatsMap.get(key);
			summaryMap.put(key, cstats.getTotalOccurrance());
		}
		return summaryMap;
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

	public File getFile() {
		return file;
	}

	@Override
	public int getKeylen() {
		return keylen;
	}

	@Override
	public void setKeylen(int keylen) {
		this.keylen = keylen;
	}

	@Override
	public Map<String, CollectorStats<String>> getCollectorStatsMap() {
		return collectorStatsMap;
	}

	public Map<String, Integer> getSummaryMap() {
		return summaryMap;
	}

	/**
	 * Test instance where T :: String
	 * Usage: StringCollector <text> -keylen n [options]
	 * Where: 
	 * 	<text> is a String to analyze & collect stats for
	 * 2 <= n <= 5  - keylen for CollectorStats
	 * 	[options] :
	 * 		-map char1a=char1b[;char2a=char2b;...]
	 * 		-ignoreCase <T | F>  default is to convert all to LC
	 * 		-file <path to text file>
	 * 
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String... args) throws FileNotFoundException {
		
		String filename = null;
		String text = null;
		File file = null;
		boolean ignoreCase = false;
		int keylen = 2;
		boolean print = false;
		boolean summary = false;
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
			else if(args[i].equalsIgnoreCase("-print")) {
				print = true;
			}
			else if(args[i].equalsIgnoreCase("-noprint")) {
				print = false;
			}
			else if(args[i].equalsIgnoreCase("-summary")) {
				summary = true;
			}
			else {
				text = args[i];
			}
		}
		if(filename != null) {
			file = new File(filename);
		}
		StringCollector stringCollector = new StringCollector(keylen, file, text, ignoreCase);
		stringCollector.collect();
		Map<String, CollectorStats<String>> collectorStatsMap = stringCollector.getCollectorStatsMap();
		Map<String, Integer> summaryMap = stringCollector.getSummaryMap();
		
		if(print) {
			displayCollectorStats(collectorStatsMap);
		}
		if(summary) {
			displayCollectorStatsSummary(summaryMap);
		}
	}

	private static void displayCollectorStatsSummary(Map<String, Integer> summarymap) {
		for(String key : summarymap.keySet()) {
			Integer count = summarymap.get(key);
			System.out.println("'" + key + "'\t" + count);
		}
	}

	public static void displayCollectorStats(Map<String, CollectorStats<String>> collectorStatsMap) {
		for(String key : collectorStatsMap.keySet()) {
			CollectorStats<String> cstats = collectorStatsMap.get(key);
			System.out.println("'" + key + "'\t" + cstats.getTotalOccurrance());
			System.out.print(cstats.toString());
		}
	}
	
	@Override
	public Class<String> getCollectorSubsetClass() {
		return String.class;
	}

}
