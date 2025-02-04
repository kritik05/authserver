package com.authserver.Authserver.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
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
    public void updateDependabotState(String uuid, String state, String dismissedReason) throws IOException {
        Map<String, Object> partial = new HashMap<>();
        Status finalStatus = mapStringToStatusDependabot(state);
        partial.put("status", finalStatus);

        UpdateRequest<Map<String, Object>, Map<String, Object>> updateReq = UpdateRequest.of(u -> u
                .index(findingsIndex)
                .id(uuid)
                .doc(partial)
        );

        esClient.update(updateReq, Map.class);
    }

    private Status mapStringToStatusDependabot(String rawState) {

        if (rawState == null) {
            return Status.OPEN;
        }
        switch (rawState.toLowerCase()) {
            case "open":
                return Status.OPEN;
            case "dismissed":
                return Status.FALSE_POSITIVE;
            default:
                return Status.OPEN;
        }
    }
}

