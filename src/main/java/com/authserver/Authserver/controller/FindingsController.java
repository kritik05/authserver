package com.authserver.Authserver.controller;
import com.authserver.Authserver.model.StateRequest;
import com.authserver.Authserver.service.ElasticsearchService;
import com.authserver.Authserver.service.GithubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/findings")
public class FindingsController {

    private final ElasticsearchService elasticsearchService;
    private final GithubService githubService;

    public FindingsController(ElasticsearchService elasticsearchService, GithubService githubService) {
        this.elasticsearchService = elasticsearchService;
        this.githubService=githubService;
    }

    @DeleteMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAllFindings() {
        try {
            elasticsearchService.deleteAllFindings();
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

@GetMapping("/search")
@PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
public ResponseEntity<Map<String, Object>> searchFindings(
        @RequestParam(required = false) List<String> toolType,
        @RequestParam(required = false) List<String> severity,
        @RequestParam(required = false) List<String> status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "11") int size
) {
    try {
        Map<String, Object> result = elasticsearchService.searchFindings(toolType, severity, status, page, size);
        return ResponseEntity.ok(result);
    } catch (IOException e) {
        return ResponseEntity.internalServerError().build();
    }
}

    @PutMapping("/{uuid}/{tooltype}/alerts/{alertNumber}/state")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updateDependabotState(
            @PathVariable String uuid,
            @PathVariable String tooltype,
            @RequestBody StateRequest request,
            @PathVariable String alertNumber
    ) {
        try {
            githubService.updateAlert(alertNumber,tooltype, request.getState(), request.getDismissedReason());
            elasticsearchService.updateState(uuid,tooltype ,request.getState(), request.getDismissedReason());

            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}