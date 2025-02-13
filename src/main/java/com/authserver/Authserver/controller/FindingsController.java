package com.authserver.Authserver.controller;
import com.authserver.Authserver.model.Role;
import com.authserver.Authserver.model.StateRequest;
import com.authserver.Authserver.security.RequiresRoles;
import com.authserver.Authserver.service.ElasticsearchService;
import com.authserver.Authserver.service.GithubService;
import org.springframework.http.ResponseEntity;
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
    @RequiresRoles({Role.SUPER_ADMIN})
    public ResponseEntity<Void> deleteAllFindings(
            @RequestParam(value = "tenantId") int tenantId
    ) {
        try {
            elasticsearchService.deleteAllFindings(tenantId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

@GetMapping("/search")
@RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
public ResponseEntity<Map<String, Object>> searchFindings(
        @RequestParam(required = false) List<String> toolType,
        @RequestParam(required = false) List<String> severity,
        @RequestParam(required = false) List<String> status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "11") int size,
        @RequestParam(value = "tenantId") int tenantId
) {
    try {
        Map<String, Object> result = elasticsearchService.searchFindings(toolType, severity, status, page, size,tenantId);
        return ResponseEntity.ok(result);
    } catch (IOException e) {
        return ResponseEntity.internalServerError().build();
    }
}

    @PutMapping("/{uuid}/{tooltype}/alerts/{alertNumber}/state")
    @RequiresRoles({Role.SUPER_ADMIN})
    public ResponseEntity<Void> updateState(
            @PathVariable String uuid,
            @PathVariable String tooltype,
            @RequestBody StateRequest request,
            @PathVariable String alertNumber,
            @RequestParam(value = "tenantId") int tenantId
    ) {
        try {
            githubService.updateAlert(alertNumber,tooltype, request.getState(), request.getDismissedReason(),tenantId);
            elasticsearchService.updateState(uuid,tooltype ,request.getState(), request.getDismissedReason(),tenantId);

            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}