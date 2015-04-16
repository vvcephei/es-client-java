package com.bazaarvoice.elasticsearch.client;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class SearchTest extends JerseyHttpClientTest {

    public static final String INDEX = "search-test-idx";
    public static final String TYPE = "search-test-type";
    public static final String ID = "search-test-id-1";

    private static boolean debug = false;

    @Test public void testSearch() {
        final IndexRequestBuilder indexRequestBuilder = restClient().prepareIndex(INDEX, TYPE, ID).setSource("field", "value").setRefresh(true);
        indexRequestBuilder.execute().actionGet();


        final String facetName = "myfacet";
        final String suggestionName = "mysugg";


        SearchRequestBuilder searchRequestBuilder = restClient().prepareSearch(INDEX);
        searchRequestBuilder.setQuery(QueryBuilders.termQuery("field", "value"));
        searchRequestBuilder.addFacet(FacetBuilders.termsFacet(facetName).field("field").size(10));
        searchRequestBuilder.addSuggestion(new TermSuggestionBuilder(suggestionName).text("valeu").field("field"));
        ListenableActionFuture<SearchResponse> execute2 = searchRequestBuilder.execute();
        SearchResponse searchResponse = execute2.actionGet();

        if (debug) System.out.println("took: " + Objects.toString(searchResponse.getTook()));
        if (debug) System.out.println("tookMillis: " + Objects.toString(searchResponse.getTookInMillis()));
        assertTrue(searchResponse.getTook().millis() > 0);
        if (debug) System.out.println("totalShards: " + Objects.toString(searchResponse.getTotalShards()));
        if (debug) System.out.println("successfulShards: " + Objects.toString(searchResponse.getSuccessfulShards()));
        if (debug) System.out.println("failedShards: " + Objects.toString(searchResponse.getFailedShards()));
        if (debug) System.out.println("shardFailures#: " + Objects.toString(searchResponse.getShardFailures().length));
        assertEquals(searchResponse.getTotalShards(), 5);
        assertEquals(searchResponse.getSuccessfulShards(), 5);
        assertEquals(searchResponse.getFailedShards(), 0);
        assertEquals(searchResponse.getShardFailures().length, 0);
        if (debug) System.out.println("scrollId: " + Objects.toString(searchResponse.getScrollId()));
        assertNull(searchResponse.getScrollId());
        if (debug) System.out.println("facets: " + Objects.toString(searchResponse.getFacets()));
        final TermsFacet myfacet = searchResponse.getFacets().facet("myfacet");
        if (debug) System.out.println("facet: name: " + Objects.toString(myfacet.getName()));
        assertEquals(myfacet.getName(), facetName);
        if (debug) System.out.println("facet: type: " + Objects.toString(myfacet.getType()));
        assertEquals(myfacet.getType(), "terms");
        if (debug) System.out.println("facet: total: " + Objects.toString(myfacet.getTotalCount()));
        assertEquals(myfacet.getTotalCount(), 1);
        if (debug) System.out.println("facet: missing: " + Objects.toString(myfacet.getMissingCount()));
        assertEquals(myfacet.getMissingCount(), 0);
        if (debug) System.out.println("facet: other: " + Objects.toString(myfacet.getOtherCount()));
        assertEquals(myfacet.getOtherCount(), 0);

        assertEquals(myfacet.getEntries().size(), 1);
        for (TermsFacet.Entry entry : myfacet.getEntries()) {
            if (debug) System.out.println("facet: entry: term: " + Objects.toString(entry.getTerm()));
            assertEquals(entry.getTerm().string(), "value");
            if (debug) System.out.println("facet: entry: count: " + Objects.toString(entry.getCount()));
            assertEquals(entry.getCount(), 1);
        }
        if (debug) System.out.println("aggs (not implemented): " + Objects.toString(searchResponse.getAggregations()));
        //TODO

        if (debug) System.out.println("suggest: " + Objects.toString(searchResponse.getSuggest()));
        final Suggest.Suggestion<Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>> suggestion = searchResponse.getSuggest().getSuggestion(suggestionName);
        final List<Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>> entries = suggestion.getEntries();
        assertEquals(entries.size(), 1);
        for (Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option> entry : entries) {
            assertEquals(entry.getText().string(), "valeu");
            assertEquals(entry.getOffset(), 0);
            assertEquals(entry.getLength(), 5);
            assertEquals(entry.getOptions().size(), 1);
            for (Suggest.Suggestion.Entry.Option option : entry.getOptions()) {
                assertEquals(option.getText().string(), "value");
                // FIXME WTF? assertEquals(option.getHighlighted().string(), "null");
                assertTrue(option.getScore() > 0.0);
            }
        }

        if (debug) System.out.println("maxScore" + Objects.toString(searchResponse.getHits().getMaxScore()));
        assertTrue(searchResponse.getHits().getMaxScore() > 0.0);
        if (debug) System.out.println("totalHits: " + Objects.toString(searchResponse.getHits().getTotalHits()));
        assertEquals(searchResponse.getHits().getTotalHits(), 1);
        if (debug) System.out.println("hits: " + Objects.toString(searchResponse.getHits().getHits()));
        assertEquals(searchResponse.getHits().getHits().length, 1);
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            if (debug) System.out.println("index: " + Objects.toString(hit.getIndex()));
            assertEquals(hit.index(), INDEX);
            if (debug) System.out.println("shard: " + Objects.toString(hit.getShard()));
            // ignoring, unserialized...
            if (debug) System.out.println("type: " + Objects.toString(hit.getType()));
            assertEquals(hit.getType(), TYPE);
            if (debug) System.out.println("id: " + Objects.toString(hit.getId()));
            assertEquals(hit.getId(), ID);
            if (debug) System.out.println("version: " + Objects.toString(hit.getVersion()));
            // ignoring, unserialized...
            if (debug) System.out.println("source: " + Objects.toString(hit.getSourceAsString()));
            assertEquals(hit.getSource().get("field"), "value");
            if (debug) System.out.println("explanation: " + Objects.toString(hit.getExplanation()));
            assertNull(hit.explanation());
            if (debug) System.out.println("fields: " + Objects.toString(hit.getFields()));
            assertTrue(hit.getFields().isEmpty());
            if (debug) System.out.println("highlightFields: " + Objects.toString(hit.getHighlightFields()));
            assertTrue(hit.getHighlightFields().isEmpty());
            if (debug) System.out.println("score: " + Objects.toString(hit.getScore()));
            // ignoring, unserialized...
            if (debug) System.out.println("sortValues#: " + Objects.toString(hit.getSortValues().length));
            assertEquals(hit.getSortValues().length, 0);
            if (debug) System.out.println("matchedQueries#: " + Objects.toString(hit.getMatchedQueries().length));
            assertEquals(hit.getMatchedQueries().length, 0);
        }
    }
}
