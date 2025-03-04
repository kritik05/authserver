package com.authserver.Authserver.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.authserver.Authserver.model.Finding;
import com.authserver.Authserver.model.Tenant;
import com.authserver.Authserver.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient esClient;
    private final TenantRepository tenantRepository;


    public ElasticsearchService(ElasticsearchClient esClient,TenantRepository tenantRepository) {
        this.esClient = esClient;
        this.tenantRepository=tenantRepository;
    }


    public void deleteAllFindings(int tenantId) throws IOException {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.get();
        String esindex= tenant.getFindingindex();
        esClient.deleteByQuery(dbq -> dbq
                .index(esindex)
                .query(q -> q.matchAll(m -> m))
        );
    }


    public Map<String, Object> searchFindings(
        List<String> toolTypes,
        List<String> severities,
        List<String> statuses,
        int page,
        int size,
        int tenantId) throws IOException {

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
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.get();
        String esindex= tenant.getFindingindex();
        boolean indexExists = esClient.indices().exists(e -> e.index(esindex)).value();
        if (!indexExists) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("content", Collections.emptyList());
            emptyResult.put("totalElements", 0L);
            emptyResult.put("offset", 0);
            return emptyResult;
        }
    SearchResponse<Finding> response = esClient.search(s -> s
                    .index(esindex)
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

    public Map<String, Long> getToolDataForTools(List<String> tools,int tenantId) throws IOException {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.get();
        String esindex= tenant.getFindingindex();
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(esindex)
                .size(0);

        if (tools != null && !tools.isEmpty() && !tools.contains("ALL")) {
            List<FieldValue> toolValues = tools.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());
            builder.query(q -> q.terms(t -> t
                    .field("toolType.keyword")
                    .terms(ts -> ts.value(toolValues))));
        }

        builder.aggregations("toolAgg", a -> a.terms(t -> t.field("toolType.keyword")));
        SearchResponse<Void> response = esClient.search(builder.build(), Void.class);
        Aggregate toolAggregate = response.aggregations().get("toolAgg");
        return parseStringTerms(toolAggregate);
    }

    public List<Map<String, Object>> getCvssDataForTools(List<String> tools,int tenantId) throws IOException {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.get();
        String esindex= tenant.getFindingindex();
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(esindex)
                .size(0);
        if (tools != null && !tools.isEmpty() && !tools.contains("ALL")) {
            List<FieldValue> toolValues = tools.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());
            builder.query(q -> q.terms(t -> t
                    .field("toolType.keyword")
                    .terms(ts -> ts.value(toolValues))));
        }
        builder.aggregations("cvssAgg", a -> a.histogram(h -> h.field("cvss").interval(2.0)));
        SearchResponse<Void> response = esClient.search(builder.build(), Void.class);
        Aggregate agg = response.aggregations().get("cvssAgg");
        return parseHistogram(agg);
    }

    public Map<String, Long> getStatusDataForTools(List<String> tools,int tenantId) throws IOException {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.get();
        String esindex= tenant.getFindingindex();
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(esindex)
                .size(0);

        if (tools != null && !tools.isEmpty() && !tools.contains("ALL")) {
            List<FieldValue> toolValues = tools.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());
            builder.query(q -> q.terms(t -> t
                    .field("toolType.keyword")
                    .terms(ts -> ts.value(toolValues))));
        }

        builder.aggregations("statusAgg", a -> a.terms(t -> t.field("status.keyword")));
        SearchResponse<Void> response = esClient.search(builder.build(), Void.class);
        Aggregate agg = response.aggregations().get("statusAgg");
        return parseStringTerms(agg);
    }

    public Map<String, Long> getSeverityDataForTools(List<String> tools,int tenantId) throws IOException {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.get();
        String esindex= tenant.getFindingindex();
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(esindex)
                .size(0);
        if (tools != null && !tools.isEmpty() && !tools.contains("ALL")) {
            List<FieldValue> toolValues = tools.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());
            builder.query(q -> q.terms(t -> t
                    .field("toolType.keyword")
                    .terms(ts -> ts.value(toolValues))));
        }
        builder.aggregations("severityAgg", a -> a.terms(t -> t.field("severity.keyword")));
        SearchResponse<Void> response = esClient.search(builder.build(), Void.class);
        Aggregate agg = response.aggregations().get("severityAgg");
        return parseStringTerms(agg);
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

