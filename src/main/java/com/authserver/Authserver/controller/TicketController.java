package com.authserver.Authserver.controller;

import com.authserver.Authserver.events.TicketCreateRequestEvent;
import com.authserver.Authserver.events.TicketTransitionRequestEvent;
import com.authserver.Authserver.model.*;
import com.authserver.Authserver.producer.EventProducer;
import com.authserver.Authserver.repository.TenantTicketRepository;
import com.authserver.Authserver.security.RequiresRoles;
import com.authserver.Authserver.service.ElasticsearchService;
import com.authserver.Authserver.service.JiraService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final ElasticsearchService elasticsearchService;
    private final JiraService jiraService;
    private final TenantTicketRepository tenantTicketRepository;
    private final EventProducer eventProducer;

    public TicketController(ElasticsearchService elasticsearchService, JiraService jiraService, TenantTicketRepository tenantTicketRepository,EventProducer eventProducer) {
        this.elasticsearchService = elasticsearchService;
        this.jiraService = jiraService;
        this.tenantTicketRepository = tenantTicketRepository;
        this.eventProducer=eventProducer;
    }

    @PostMapping("/{uuid}")
    @RequiresRoles({Role.SUPER_ADMIN,Role.ADMIN})
    public String createTicket(@PathVariable("uuid") String uuid,
                               @RequestParam("tenantId") int tenantId,
                               @RequestBody Map<String, String> requestBody
    ) throws IOException {

        String summary = requestBody.getOrDefault("summary", "Default Summary");
        String description = requestBody.getOrDefault("description", "Default Description");

        TicketCreateRequestPayload ticketCreateRequestPayload=new TicketCreateRequestPayload(uuid,tenantId,summary,description);
        TicketCreateRequestEvent ticketCreateRequestEvent=new TicketCreateRequestEvent(ticketCreateRequestPayload);
        eventProducer.sendTicketCreateEvent(ticketCreateRequestEvent);
        return "Created Jira ticket: ";
    }

    @GetMapping
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
    public Map<String, Object>  getAllTicketsForTenant(@RequestParam("tenantId") int tenantId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "12") int size ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TenantTicket> ticketsPage = tenantTicketRepository.findByTenantId(tenantId, pageable);
        List<Ticket> ticketDetails = new ArrayList<>();
        for (TenantTicket tenantTicket : ticketsPage) {
            Ticket dto = jiraService.getIssueDetails(tenantTicket.getTicketId(), tenantId);
            ticketDetails.add(dto);
        }

        // Build a response similar to your ES response
        Map<String, Object> response = new HashMap<>();
        response.put("content", ticketDetails);
        response.put("totalElements", ticketsPage.getTotalElements());  // total count
        response.put("page", page);                                     // current page
        response.put("size", size);                                     // page size

        return response;
    }

    @GetMapping("/{ticketId}")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
    public Ticket getTicketByTicketId(@PathVariable("ticketId") String ticketId,
                                            @RequestParam("tenantId") int tenantId) {
        Optional<TenantTicket> maybeTicket = tenantTicketRepository.findByTicketId(ticketId);
        if (maybeTicket.isPresent() && maybeTicket.get().getTenantId().equals(tenantId)) {
            return jiraService.getIssueDetails(ticketId, tenantId);
        }
        throw new RuntimeException("Ticket not found for tenant " + tenantId + " with ticket " + ticketId);
    }

    @GetMapping("/finding/{ticketId}")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN,Role.USER})
    public String getFindingIdByTicketId(@PathVariable("ticketId") String ticketId,
                                         @RequestParam("tenantId") int tenantId) {
        Optional<TenantTicket> maybeTicket = tenantTicketRepository.findByTicketId(ticketId);
        if (maybeTicket.isPresent() && maybeTicket.get().getTenantId().equals(tenantId)) {
            return maybeTicket.get().getFindingId();
        }
        throw new RuntimeException("Ticket not found for tenant " + tenantId + " with ticket " + ticketId);
    }

    @PutMapping("/{ticketId}/done")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN})
    public String moveTicketToDone(@PathVariable("ticketId") String ticketId,
                                   @RequestParam("tenantId") int tenantId) {
        TicketTransitionRequestPayload ticketTransitionRequestPayload=new TicketTransitionRequestPayload(tenantId,ticketId);
        TicketTransitionRequestEvent ticketTransitionRequestEvent=new TicketTransitionRequestEvent(ticketTransitionRequestPayload);
        eventProducer.sendTicketTransitionEvent(ticketTransitionRequestEvent);

        return "Ticket ";
    }

}
