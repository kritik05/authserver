package com.authserver.Authserver.controller;

import com.authserver.Authserver.model.Role;
import com.authserver.Authserver.security.RequiresRoles;
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
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
    public Map<String, Long> getToolData(
            @RequestParam(value = "tools", required = false) List<String> tools,
            @RequestParam(value = "tenantId") int tenantId
    ) throws IOException {
        return esService.getToolDataForTools(tools,tenantId);
    }

    @GetMapping("/cvss")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
    public List<Map<String, Object>> getCvssData(
            @RequestParam(value = "tools", required = false) List<String> tools,
             @RequestParam(value = "tenantId") int tenantId
    ) throws IOException {
        return esService.getCvssDataForTools(tools,tenantId);
    }

    @GetMapping("/status")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
    public Map<String, Long> getStatusData(
            @RequestParam(value = "tools", required = false) List<String> tools,
             @RequestParam(value = "tenantId") int tenantId
    ) throws IOException {
        return esService.getStatusDataForTools(tools,tenantId);
    }

    @GetMapping("/severity")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
    public Map<String, Long> getSeverityData(
            @RequestParam(value = "tools", required = false) List<String> tools,
             @RequestParam(value = "tenantId") int tenantId
    ) throws IOException {
        return esService.getSeverityDataForTools(tools,tenantId);
    }

}