package com.authserver.Authserver.service.mappers;

import com.authserver.Authserver.model.Severity;
import com.authserver.Authserver.model.Status;

public class SecretScanMapper {

    public Status mapStatus(String rawState) {
        if (rawState == null) {
            return Status.OPEN;
        }
        switch (rawState.toLowerCase()) {
            case "open":
                return Status.OPEN;
            case "resolved":
                return Status.FIXED;
            default:
                return Status.OPEN;
        }
    }

    public Severity mapSeverity(String rawSeverity) {
        return Severity.CRITICAL;

    }
}