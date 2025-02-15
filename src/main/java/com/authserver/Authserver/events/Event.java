package com.authserver.Authserver.events;

public interface Event <T>{
    String getType();
    T getPayload();
    String getEventId();
}
