package com.authserver.Authserver.service;

import com.authserver.Authserver.model.Tenant;
import com.authserver.Authserver.model.Ticket;
import com.authserver.Authserver.repository.TenantRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class JiraService {
    private final TenantRepository tenantRepository;

    public JiraService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Ticket getIssueDetails(String ticketId, int tenantId) {
        Tenant tenant = getTenantOrThrow(tenantId);

        String url = "https://" + tenant.getProject_name() + "/rest/api/2/issue/" + ticketId;

        // Set up headers with Basic Auth
        HttpHeaders headers = buildAuthHeaders(tenant.getUsername(), tenant.getToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return parseIssueToDto(ticketId, response.getBody());
        }
        throw new RuntimeException("Failed to fetch issue details for: " + ticketId);
    }



    private Ticket parseIssueToDto(String ticketId, Map<String, Object> rawJson) {
        // rawJson.get("fields") -> Map of fields
        Map<String, Object> fields = (Map<String, Object>) rawJson.get("fields");

        if (fields == null) {
            throw new RuntimeException("No 'fields' in Jira response for ticket: " + ticketId);
        }

        // issuetype
        Map<String, Object> issueType = (Map<String, Object>) fields.get("issuetype");
        String issueTypeName = issueType != null ? (String) issueType.get("name") : null;
        String issueTypeDescription = issueType != null ? (String) issueType.get("description") : null;

        // summary
        String summary = (String) fields.get("summary");

        // status
        Map<String, Object> statusObj = (Map<String, Object>) fields.get("status");
        String statusName = statusObj != null ? (String) statusObj.get("name") : null;

        return new Ticket(
                ticketId,
                issueTypeName,
                issueTypeDescription,
                summary,
                statusName
        );
    }

    // Helper method to fetch Tenant or throw if not found
    private Tenant getTenantOrThrow(int tenantId) {
        Optional<Tenant> opt = tenantRepository.findById(tenantId);
        return opt.orElseThrow(() ->
                new RuntimeException("Tenant not found with ID: " + tenantId));
    }

    // Helper method to build Basic Auth headers
    private HttpHeaders buildAuthHeaders(String username, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authString = username + ":" + token;
        String base64Creds = Base64.getEncoder().encodeToString(authString.getBytes());
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
}
