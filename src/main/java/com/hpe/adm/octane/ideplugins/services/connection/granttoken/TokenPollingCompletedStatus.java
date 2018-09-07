package com.hpe.adm.octane.ideplugins.services.connection.granttoken;

public class TokenPollingCompletedStatus {
	
	public enum Result {
		SUCCESS, FAIL, TIMEOUT
	}
	
	public Result result;
	public Exception exception; //In case of failure
    
}