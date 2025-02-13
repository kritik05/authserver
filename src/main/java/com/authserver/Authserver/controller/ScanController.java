package com.authserver.Authserver.controller;

import com.authserver.Authserver.model.Role;
import com.authserver.Authserver.model.ScanEvent;
import com.authserver.Authserver.security.RequiresRoles;
import com.authserver.Authserver.service.ScanEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final ScanEventProducer scanEventProducer;

    public ScanController(ScanEventProducer scanEventProducer) {
        this.scanEventProducer = scanEventProducer;
    }

    @PostMapping("/scan")
    @RequiresRoles({Role.ADMIN, Role.SUPER_ADMIN})
    public ResponseEntity<String> initiateScan(@RequestBody ScanEvent request) {
        scanEventProducer.sendScanEvent(request);
        return ResponseEntity.ok("Scan event sent successfully.");
    }
}