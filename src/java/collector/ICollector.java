package collector;

import java.util.Map;

/**
 * @deprecated use classes in util.cp package
 * @author don_bacon
 *
 * @param <T>
 */
public interface ICollector<T> {

	void collect();
	int getKeylen();
	void setKeylen(int keylen);
	Class<T> getCollectorSubsetClass();
	Map<T, CollectorStats<T>> getCollectorStatsMap();
}
