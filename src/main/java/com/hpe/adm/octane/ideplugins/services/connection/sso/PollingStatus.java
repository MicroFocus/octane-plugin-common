package com.hpe.adm.octane.ideplugins.services.connection.sso;

public class PollingStatus {
    public long timeoutTimeStamp;
    public Boolean shouldPoll = true; // needs to be an object to be able to, modify it while it's being passed as a parameter
}