package collector;

import java.util.Map;
import java.util.Set;

/**
 *  @deprecated use classes in util.cp package
 * @author don_bacon
 *
 * @param <T>
 */
public interface IProducer<T> {
	
	Class<T> getProducerSubsetClass();
	Set<String> produce(Map<String, CollectorStats<String>> collectorStatsMap);
	String getSeed();
	void setSeed(String seed);
}
