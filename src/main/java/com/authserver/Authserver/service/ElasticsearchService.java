package com.authserver.Authserver.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.authserver.Authserver.model.Finding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient esClient;

    // The name of the index, e.g. "findings-index"
    @Value("${app.elasticsearch.findings-index}")
    private String findingsIndex;

    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * Retrieve ALL documents from findingsIndex.
     */
    public List<Finding> findAll() throws IOException {
        SearchRequest request = SearchRequest.of(s -> s
                .index(findingsIndex)
                .query(q -> q.matchAll(m -> m)) // match all query
                .size(10000)                    // set size if you want more than default 10
        );

        SearchResponse<Finding> response = esClient.search(request, Finding.class);

        // Convert the hits to a List<Finding>
        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all documents by a given toolType from findingsIndex.
     */
    public List<Finding> findByToolType(String toolType) throws IOException {
        SearchResponse<Finding> response = esClient.search(s -> s
                        .index(findingsIndex)
                        .query(q -> q
                                .match(m -> m
                                        .field("toolType")
                                        .query(toolType)
                                )
                        )
                        .size(10000),
                Finding.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

}