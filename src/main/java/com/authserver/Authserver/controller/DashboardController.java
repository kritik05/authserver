package com.authserver.Authserver.controller;

import com.authserver.Authserver.service.ElasticsearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ElasticsearchService esService;

    public DashboardController(ElasticsearchService esService) {
        this.esService = esService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(name="tools", required=false) List<String> tools
    ) {
       ;
        try {
            if (tools == null || tools.isEmpty()) {
                Map<String, Object> data = esService.getDashboardData();
                return ResponseEntity.ok(data);
            } else {
                // Multi (or single) selected tools => pass them as a list
                Map<String, Object> data = esService.getDashboardDataForTools(tools);
                return ResponseEntity.ok(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}