package com.authserver.Authserver.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import com.authserver.Authserver.model.Finding;
import com.authserver.Authserver.model.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient esClient;

    @Value("${app.elasticsearch.findings-index}")
    private String findingsIndex;

    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }


    public void deleteAllFindings() throws IOException {
        esClient.deleteByQuery(dbq -> dbq
                .index(findingsIndex)
                .query(q -> q.matchAll(m -> m))
        );
    }

public Map<String, Object> searchFindings(
        List<String> toolTypes,
        List<String> severities,
        List<String> statuses,
        int page,
        int size) throws IOException {

    List<Query> mustQueries = new ArrayList<>();

    if (toolTypes != null && !toolTypes.isEmpty()) {
        List<FieldValue> toolTypeValues = toolTypes.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        mustQueries.add(Query.of(q -> q
                .terms(t -> t
                        .field("toolType.keyword")
                        .terms(ts -> ts.value(toolTypeValues))
                )
        ));
    }

    if (severities != null && !severities.isEmpty()) {
        List<FieldValue> severityValues = severities.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        mustQueries.add(Query.of(q -> q
                .terms(t -> t
                        .field("severity.keyword")
                        .terms(ts -> ts.value(severityValues))
                )
        ));
    }

    if (statuses != null && !statuses.isEmpty()) {
        List<FieldValue> statusValues = statuses.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        mustQueries.add(Query.of(q -> q
                .terms(t -> t
                        .field("status.keyword")
                        .terms(ts -> ts.value(statusValues))
                )
        ));
    }

    BoolQuery boolQuery = BoolQuery.of(b -> b.must(mustQueries));
    int from = (page - 1) * size;

    SearchResponse<Finding> response = esClient.search(s -> s
                    .index(findingsIndex)
                    .query(q -> q.bool(boolQuery))
                    .sort(sort -> sort
                            .field(f -> f
                                    .field("updatedAt")
                                    .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                            )
                    )
                    .from(from)
                    .size(size),
            Finding.class);

    List<Finding> content = response.hits().hits().stream()
            .map(hit -> hit.source())
            .collect(Collectors.toList());

    long totalHits = (response.hits().total() != null) ? response.hits().total().value() : 0L;

    Map<String, Object> result = new HashMap<>();
    result.put("content", content);
    result.put("totalElements", totalHits);
    result.put("offset", from + 1);

    return result;
}
    public void updateState(String uuid, String state, String dismissedReason) throws IOException {
        Map<String, Object> partial = new HashMap<>();
        Status finalStatus = mapStringToStatus(state);
        partial.put("status", finalStatus);

        UpdateRequest<Map<String, Object>, Map<String, Object>> updateReq = UpdateRequest.of(u -> u
                .index(findingsIndex)
                .id(uuid)
                .doc(partial)
        );

        esClient.update(updateReq, Map.class);
    }

    private Status mapStringToStatus(String rawState) {

        if (rawState == null) {
            return Status.OPEN;
        }
        switch (rawState.toLowerCase()) {
            case "open":
                return Status.OPEN;
            case "dismissed":
                return Status.FALSE_POSITIVE;
            case "resolved":
            default:
                return Status.FIXED;
        }
    }
    public Map<String, Object> getDashboardData() throws IOException {

        // 1) Execute a search with multiple aggregations
        SearchResponse<Void> response = esClient.search(s -> s
                        .index(findingsIndex)
                        .size(0)
                        .aggregations("toolAgg", a -> a.terms(t -> t.field("toolType.keyword")))
                        .aggregations("statusAgg", a -> a.terms(t -> t.field("status.keyword")))
                        .aggregations("severityAgg", a -> a.terms(t -> t.field("severity.keyword")))
                        .aggregations("cvssAgg", a -> a.histogram(h -> h
                                .field("cvss")
                                .interval(2.0)
                        )),
                Void.class
        );

        // 2) Build final result
        Map<String, Object> result = new HashMap<>();

        // Parse "toolAgg" -> string terms
        result.put("toolWise", parseStringTerms(response.aggregations().get("toolAgg")));

        // Parse "statusAgg" -> string terms
        result.put("statusWise", parseStringTerms(response.aggregations().get("statusAgg")));

        // Parse "severityAgg" -> string terms
        result.put("severityWise", parseStringTerms(response.aggregations().get("severityAgg")));

        // Parse "cvssAgg" -> histogram
        result.put("cvssRanges", parseHistogram(response.aggregations().get("cvssAgg")));

        return result;
    }

    public Map<String, Object> getDashboardDataForTools(List<String> tools) throws IOException {
        // If user passes no tools or "ALL" logic -> just return all
        if (tools == null || tools.isEmpty()) {
            return getDashboardData();
        }

        // Build a terms query for toolType.keyword IN (tools)
        List<FieldValue> toolValues = tools.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        SearchResponse<Void> response = esClient.search(s -> s
                        .index(findingsIndex)
                        .size(0)
                        .query(q -> q.terms(t -> t
                                .field("toolType.keyword")
                                .terms(ts -> ts.value(toolValues))
                        ))
                        // same aggregations as your original getDashboardData():
                        .aggregations("toolAgg", a -> a.terms(t -> t.field("toolType.keyword")))
                        .aggregations("statusAgg", a -> a.terms(t -> t.field("status.keyword")))
                        .aggregations("severityAgg", a -> a.terms(t -> t.field("severity.keyword")))
                        .aggregations("cvssAgg", a -> a.histogram(h -> h
                                .field("cvss")
                                .interval(2.0)
                        )),
                Void.class
        );

        // Build final result using your existing parsing
        Map<String, Object> result = new HashMap<>();
        result.put("toolWise", parseStringTerms(response.aggregations().get("toolAgg")));
        result.put("statusWise", parseStringTerms(response.aggregations().get("statusAgg")));
        result.put("severityWise", parseStringTerms(response.aggregations().get("severityAgg")));
        result.put("cvssRanges", parseHistogram(response.aggregations().get("cvssAgg")));

        return result;
    }
    /**
     * Check if aggregate.stringTerms() is non-null. If so, parse buckets. Otherwise return empty.
     */
    private Map<String, Long> parseStringTerms(Aggregate agg) {
        Map<String, Long> map = new HashMap<>();
        if (agg == null) {
            return map;
        }

        // If this aggregator is actually a string terms aggregator, stringTerms() is non-null
        StringTermsAggregate sta = agg.sterms();
        if (sta != null) {
            for (StringTermsBucket bucket : sta.buckets().array()) {
                String key = bucket.key().stringValue();
                long docCount = bucket.docCount();
                map.put(key, docCount);
            }
        }
        return map;
    }

    /**
     * Check if aggregate.histogram() is non-null. If so, parse the histogram buckets.
     */
    private List<Map<String, Object>> parseHistogram(Aggregate agg) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (agg == null) {
            return list;
        }

        HistogramAggregate hist = agg.histogram();
        if (hist != null) {
            for (HistogramBucket bucket : hist.buckets().array()) {
                double key = bucket.key();
                long docCount = bucket.docCount();
                Map<String, Object> row = new HashMap<>();
                row.put("key", key);
                row.put("count", docCount);
                list.add(row);
            }
        }

        return list;
    }
}

