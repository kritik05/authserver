package com.authserver.Authserver.controller;

import com.authserver.Authserver.model.Finding;
import com.authserver.Authserver.service.ElasticsearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/findings")
public class FindingsController {

    private final ElasticsearchService elasticsearchService;

    public FindingsController(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    /**
     * GET /api/findings
     * Returns ALL documents in findings-index.
     */
    @GetMapping
    public ResponseEntity<List<Finding>> getAllFindings() {
        try {
            List<Finding> allFindings = elasticsearchService.findAll();
            return ResponseEntity.ok(allFindings);
        } catch (IOException e) {
            // handle exception
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/findings/{toolType}
     * Returns documents in findings-index where toolType == {toolType}.
     */
    @GetMapping("/{toolType}")
    public ResponseEntity<List<Finding>> getFindingsByToolType(@PathVariable String toolType) {
        try {
            List<Finding> findings = elasticsearchService.findByToolType(toolType);
            return ResponseEntity.ok(findings);
        } catch (IOException e) {
            // handle exception
            return ResponseEntity.internalServerError().build();
        }
    }
}