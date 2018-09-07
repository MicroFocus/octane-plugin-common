package com.hpe.adm.octane.ideplugins.services.connection.granttoken;

public class TokenPollingStatus {
	
    public long timeoutTimeStamp;
    public long startedTimeStamp;
    public boolean shouldPoll = true; // needs to be an object to be able to, modify it while it's being passed as a parameter
    
}