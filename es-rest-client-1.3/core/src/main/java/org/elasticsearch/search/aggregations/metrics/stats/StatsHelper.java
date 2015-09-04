package org.elasticsearch.search.aggregations.metrics.stats;

import com.bazaarvoice.elasticsearch.client.core.util.aggs.AggregationsManifest;
import org.elasticsearch.search.aggregations.InternalAggregation;

import java.util.Map;

import static org.elasticsearch.common.xcontent.support.XContentMapValues.nodeDoubleValue;
import static org.elasticsearch.common.xcontent.support.XContentMapValues.nodeLongValue;

public class StatsHelper {
    public static InternalAggregation fromXContent(final String name, final Map<String, Object> map, final AggregationsManifest manifest) {
        final long count = (map.get("count") == null ? 0L : nodeLongValue(map.get("count")));
        final double sum = (map.get("sum") == null ? 0 : nodeDoubleValue(map.get("sum")));
        final double min = (map.get("min") == null ? 0 : nodeDoubleValue(map.get("min")));
        final double max = (map.get("max") == null ? 0 : nodeDoubleValue(map.get("max")));
        return new InternalStats(name, count, sum, min, max);
    }
}
