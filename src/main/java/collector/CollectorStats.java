package collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * @author Don_Bacon
 *
 * @param <T>	base datatype for subsets and occurrence
 * @see collector.StringCollector for sample usage
 * @deprecated use classes in util.cp package
 */
public class CollectorStats<T> {

	private int subsetLength;
	private List<T>	subset = new ArrayList<T>();	// List of length subsetLength
	private int totalOccurrance;	// total #times subset occurs
	private Map<T, OccurrenceProbability> occurrenceProbabilityMap = new TreeMap<T, OccurrenceProbability>();
	public static final int LOW = 0;
	public static final int HIGH = 1;
	
	public CollectorStats() {
	}
	
	public CollectorStats(int sublen) {
		this.subsetLength = sublen;
	}
	
	public CollectorStats(List<T> sub) {
		setSubset(sub);
	}

	public int getSubsetLength() {
		return subsetLength;
	}

	public void setSubsetLength(int subsetLength) {
		this.subsetLength = subsetLength;
	}
	
	public List<T> getSubset() {
		return subset;
	}

	public void setSubset(List<T> sub) {
		this.subset.addAll(sub);
		this.subsetLength = sub.size();
	}

	public Map<T, OccurrenceProbability> getOccurrenceProbabilityMap() {
		return occurrenceProbabilityMap;
	}
	
	public void addOccurrence(T toccur) {
		if(occurrenceProbabilityMap.containsKey(toccur)) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(toccur);
			op.setOccurrence(op.getOccurrence() + 1);
		}
		else {
			occurrenceProbabilityMap.put(toccur, new OccurrenceProbability(1, 1.0));
		}
		recomputeProbabilitie();
	}

	/**
	 * example: occur		range
	 * 			-----		-----
	 * 			  3			  1,3  (1,2,3 selects first entry)
	 * 			  5			  4,8  (4 - 8 selects the second)
	 * 			  1			  9,9  (9 selects the third)
	 * 			  2			10,11   etc.
	 */
	private void recomputeProbabilitie() {
		totalOccurrance = 0;
		Collection<OccurrenceProbability> opcollection = occurrenceProbabilityMap.values();
		for(OccurrenceProbability op : opcollection) {
			totalOccurrance+= op.getOccurrence();
		}
		Set<T> keyset = occurrenceProbabilityMap.keySet(); 
		int[] prevRange = null;
		for(T key : keyset) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(key);
			int occur = op.getOccurrence();
			if(totalOccurrance > 0) {
				op.setProbability(((double)op.getOccurrence()) / ((double)totalOccurrance));
			}
			if(prevRange == null) {
				op.setRange(LOW, 1);
				op.setRange(HIGH, occur);
			}
			else {
				op.setRange(LOW, prevRange[HIGH] + 1);
				op.setRange(HIGH, prevRange[HIGH] + occur);
			}
			prevRange = op.getRange();
		}
	}
	
	public int getTotalOccurrance() {
		return totalOccurrance;
	}

	public void setTotalOccurrance(int totalOccurrance) {
		this.totalOccurrance = totalOccurrance;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(T key : occurrenceProbabilityMap.keySet()) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(key);
			int[] rng = op.getRange();
			sb.append("   '" + key.toString() + "'\t" + op.getOccurrence() + 
					"\t" + rng[0] + "," + rng[1] +
					"\t" + op.getProbability());
			sb.append("\n");
		}
		return sb.toString();
	}

}

