package com.hpe.adm.octane.ideplugins.services.connection.granttoken;

public interface TokenPollingStartedHandler {
	
    void pollingStarted(String loginUrl);
    
}