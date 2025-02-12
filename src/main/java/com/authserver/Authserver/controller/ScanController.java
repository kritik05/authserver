package com.authserver.Authserver.controller;

import com.authserver.Authserver.model.ScanEvent;
import com.authserver.Authserver.service.ScanEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final ScanEventProducer scanEventProducer;

    public ScanController(ScanEventProducer scanEventProducer) {
        this.scanEventProducer = scanEventProducer;
    }

    @PostMapping("/scan")
    public ResponseEntity<String> initiateScan(@RequestBody ScanEvent request) {
        scanEventProducer.sendScanEvent(request);

        return ResponseEntity.ok("Scan event sent successfully.");
    }
}