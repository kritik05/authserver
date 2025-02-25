package com.authserver.Authserver.controller;

import com.authserver.Authserver.events.ScanRequestEvent;
import com.authserver.Authserver.model.Role;
//import com.authserver.Authserver.model.ScanEvent;
import com.authserver.Authserver.model.ScanRequestPayload;
import com.authserver.Authserver.producer.EventProducer;
import com.authserver.Authserver.security.RequiresRoles;
//import com.authserver.Authserver.producer.ScanEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final EventProducer eventProducer;

    public ScanController(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @PostMapping("/scan")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN})
    public ResponseEntity<String> initiateScan(@RequestBody ScanRequestPayload request) {
        ScanRequestEvent event = new ScanRequestEvent(request);
        eventProducer.sendScanEvent(event);
        return ResponseEntity.ok("Scan event sent successfully.");
    }
}