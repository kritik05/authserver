package com.authserver.Authserver.model;

public class DependabotStateRequest {
    private String state;
    private String dismissedReason;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDismissedReason() {
        return dismissedReason;
    }

    public void setDismissedReason(String dismissedReason) {
        this.dismissedReason = dismissedReason;
    }
}