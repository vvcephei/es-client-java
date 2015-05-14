package org.elasticsearch.action.search.helpers;

import com.bazaarvoice.elasticsearch.client.core.util.aggs.AggregationManifest;
import com.bazaarvoice.elasticsearch.client.core.util.aggs.AggregationsManifest;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.bucket.children.ChildrenHelper;
import org.elasticsearch.search.aggregations.bucket.filter.FilterHelper;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersHelper;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoHashGridHelper;
import org.elasticsearch.search.aggregations.bucket.global.GlobalHelper;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramHelper;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramHelper;
import org.elasticsearch.search.aggregations.bucket.missing.MissingHelper;
import org.elasticsearch.search.aggregations.bucket.nested.NestedHelper;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedHelper;
import org.elasticsearch.search.aggregations.bucket.range.RangeHelper;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeHelper;
import org.elasticsearch.search.aggregations.bucket.range.geodistance.GeoDistanceHelper;
import org.elasticsearch.search.aggregations.bucket.range.ipv4.IPv4RangeBucketHelper;
import org.elasticsearch.search.aggregations.bucket.range.ipv4.IPv4RangeHelper;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsHelper;
import org.elasticsearch.search.aggregations.bucket.terms.TermsHelper;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityHelper;
import org.elasticsearch.search.aggregations.metrics.avg.AvgHelper;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsHelper;
import org.elasticsearch.search.aggregations.metrics.max.MaxHelper;
import org.elasticsearch.search.aggregations.metrics.min.MinHelper;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanksHelper;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesHelper;
import org.elasticsearch.search.aggregations.metrics.scripted.ScriptedMetricHelper;
import org.elasticsearch.search.aggregations.metrics.stats.StatsHelper;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStatsHelper;
import org.elasticsearch.search.aggregations.metrics.sum.SumHelper;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsHelper;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountHelper;

import java.util.Date;
import java.util.Map;

import static com.bazaarvoice.elasticsearch.client.core.util.MapFunctions.nodeMapValue;
import static org.elasticsearch.common.base.Preconditions.checkArgument;
import static org.elasticsearch.common.base.Preconditions.checkState;

public class InternalAggregationsHelper {
    public static InternalAggregations fromXContent(final Map<String, Object> map, final AggregationsManifest aggregationsManifest) {
        if (!(map.containsKey("aggregations") || map.containsKey("aggs"))) {
            return new InternalAggregations(ImmutableList.<InternalAggregation>of());
        } else {
            final Map<String, Object> aggregationsMap;
            if (map.containsKey("aggregations")) {
                aggregationsMap = nodeMapValue(map.get("aggregations"), String.class, Object.class);
            } else {
                aggregationsMap = nodeMapValue(map.get("aggs"), String.class, Object.class);
            }

            return fromXContentUnwrapped(aggregationsMap, aggregationsManifest);
        }
    }

    public static InternalAggregations fromXContentUnwrapped(final Map<String, Object> aggregationsMap, final AggregationsManifest aggregationsManifest) {
        if (aggregationsManifest == null) {
            checkArgument(aggregationsMap.isEmpty());
            return new InternalAggregations(ImmutableList.<InternalAggregation>of());
        }
        final ImmutableList.Builder<InternalAggregation> builder = ImmutableList.builder();
        for (Map.Entry<String, AggregationManifest> entry : aggregationsManifest.getManifest().entrySet()) {
            final String name = entry.getKey();
            final String type = entry.getValue().getType();
            checkState(aggregationsMap.containsKey(name));
            final Map<String, Object> subAggregationMap = nodeMapValue(aggregationsMap.get(name), String.class, Object.class);
            final AggregationsManifest subAggregationsManifest = entry.getValue().getSubAggregationsManifest();
            if (type.equals("terms")) {
                builder.add(TermsHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("value_count")) {
                builder.add(ValueCountHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("avg")) {
                builder.add(AvgHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("min")) {
                builder.add(MinHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("max")) {
                builder.add(MaxHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("sum")) {
                builder.add(SumHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("stats")) {
                builder.add(StatsHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("extended_stats")) {
                builder.add(ExtendedStatsHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("percentiles")) {
                builder.add(PercentilesHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("percentile_ranks")) {
                builder.add(PercentileRanksHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("cardinality")) {
                builder.add(CardinalityHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("geo_bounds")) {
                builder.add(GeoBoundsHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("top_hits")) {
                builder.add(TopHitsHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("scripted_metric")) {
                builder.add(ScriptedMetricHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("global")) {
                builder.add(GlobalHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("filter")) {
                builder.add(FilterHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("filters")) {
                builder.add(FiltersHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("missing")) {
                builder.add(MissingHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("nested")) {
                builder.add(NestedHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("reverse_nested")) {
                builder.add(ReverseNestedHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("children")) {
                builder.add(ChildrenHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("significant_terms")) {
                builder.add(SignificantTermsHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("range")) {
                builder.add(RangeHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("date_range")) {
                builder.add(DateRangeHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("ip_range")) {
                builder.add(IPv4RangeHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("histogram")) {
                builder.add(HistogramHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("date_histogram")) {
                builder.add(DateHistogramHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("geo_distance")) {
                builder.add(GeoDistanceHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else if (type.equals("geohash_grid")) {
                builder.add(GeoHashGridHelper.fromXContent(name, subAggregationMap, subAggregationsManifest));
            } else {
                throw new IllegalStateException("Unrecognized type: " + type);
            }
        }
        return new InternalAggregations(builder.build());
    }
}
