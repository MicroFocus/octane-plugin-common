package com.hpe.adm.octane.ideplugins.services.connection.granttoken;

public interface TokenPollingInProgressHandler {
	
	TokenPollingStatus polling(TokenPollingStatus pollingStatus);
	
}