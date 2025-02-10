package com.authserver.Authserver.controller;

import com.authserver.Authserver.service.ElasticsearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/tools")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public Map<String, Long> getToolData(
            @RequestParam(value = "tools", required = false) List<String> tools
    ) throws IOException {
        return esService.getToolDataForTools(tools);
    }

    @GetMapping("/cvss")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public List<Map<String, Object>> getCvssData(
            @RequestParam(value = "tools", required = false) List<String> tools
    ) throws IOException {
        return esService.getCvssDataForTools(tools);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public Map<String, Long> getStatusData(
            @RequestParam(value = "tools", required = false) List<String> tools
    ) throws IOException {
        return esService.getStatusDataForTools(tools);
    }

    @GetMapping("/severity")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public Map<String, Long> getSeverityData(
            @RequestParam(value = "tools", required = false) List<String> tools
    ) throws IOException {
        return esService.getSeverityDataForTools(tools);
    }

}