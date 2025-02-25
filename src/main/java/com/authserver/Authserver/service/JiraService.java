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
    public String createTicket(String summary, String description, int tenantId) {
        // 1. Fetch tenant to retrieve project_name, project_key, username, token, etc.
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant
                .orElseThrow(() -> new RuntimeException("Tenant not found with ID: " + tenantId));

        // 2. Build the Jira URL
        //    E.g., https://capstoneticket.atlassian.net/rest/api/2/issue/
        String jiraUrl = "https://" + tenant.getProject_name() + "/rest/api/2/issue/";

        // 3. Construct the JSON body (you can use a HashMap or a custom DTO)
        Map<String, Object> issueType = new HashMap<>();
        // Typically, the "id" for a standard "Story" or "Task" might vary in your Jira
        // Check your Jira for the correct issue type ID
        issueType.put("name", "Bug");

        Map<String, Object> projectField = new HashMap<>();
        projectField.put("key", tenant.getProject_key());

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", projectField);
        fields.put("summary", summary);
        fields.put("description", description);
        fields.put("issuetype", issueType);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fields", fields);

        // 4. Build headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Basic auth with username:token
        String authString = tenant.getUsername() + ":" + tenant.getToken();
        String base64Creds = Base64.getEncoder().encodeToString(authString.getBytes());
        headers.add("Authorization", "Basic " + base64Creds);

        // 5. Execute the POST request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> responseEntity = restTemplate
                .postForEntity(jiraUrl, requestEntity, Map.class);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            // Jira usually returns a JSON with "key" among other fields
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody != null && responseBody.containsKey("key")) {
                return (String) responseBody.get("key"); // e.g. "CAP-123"
            } else {
                throw new RuntimeException("Jira ticket created, but 'key' not found in response.");
            }
        } else {
            throw new RuntimeException("Failed to create Jira ticket. HTTP Status: "
                    + responseEntity.getStatusCode());
        }
    }

    public Ticket getIssueDetails(String ticketId, int tenantId) {
        Tenant tenant = getTenantOrThrow(tenantId);

        // e.g. https://<project_name>.atlassian.net/rest/api/2/issue/{ticketId}
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

    /**
     * Transition a ticket from "To Do" to "Done" behind the scenes.
     *  - In your scenario, you know there's only 1 valid transition from "To Do" -> "In Progress"
     *    and 1 from "In Progress" -> "Done".
     *  - We'll just loop transitions until we can no longer move forward (or we hit "Done").
     */
    public void transitionToDone(String ticketId, int tenantId) {
        Tenant tenant = getTenantOrThrow(tenantId);
        String baseUrl = "https://" + tenant.getProject_name() + "/rest/api/2/issue/" + ticketId;
        HttpHeaders headers = buildAuthHeaders(tenant.getUsername(), tenant.getToken());
        RestTemplate restTemplate = new RestTemplate();

        while (true) {
            // 1) GET current transitions
            String transitionsUrl = baseUrl + "/transitions?expand=transitions.fields";
            ResponseEntity<Map> getResponse = restTemplate.exchange(
                    transitionsUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            if (getResponse.getStatusCode() != HttpStatus.OK || getResponse.getBody() == null) {
                throw new RuntimeException("Failed to retrieve transitions for: " + ticketId);
            }

            // transitions data is typically in getResponseBody.get("transitions")
            Map<String, Object> body = getResponse.getBody();
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) body.get("transitions");
            if (transitions == null || transitions.isEmpty()) {
                // No more transitions => we must be at "Done" or an unknown state
                break;
            }

            // 2) The scenario says there's only one valid next transition in each step
            //    So let's pick the first (or only) transition and do a POST to apply it
            Map<String, Object> transition = transitions.get(0);
            String transitionId = (String) transition.get("id");

            // 3) Perform the transition
            Map<String, Object> payload = Map.of("transition", Map.of("id", transitionId));
            HttpEntity<Map<String, Object>> postEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> postResponse = restTemplate.postForEntity(
                    baseUrl + "/transitions", postEntity, String.class);

            if (postResponse.getStatusCode() != HttpStatus.NO_CONTENT
                    && postResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to transition ticket " + ticketId
                        + " using transition ID " + transitionId
                        + ". Status: " + postResponse.getStatusCode());
            }

            // We repeat until there are no more transitions (which presumably means "Done")
        }
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
