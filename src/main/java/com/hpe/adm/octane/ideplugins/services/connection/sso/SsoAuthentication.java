package com.hpe.adm.octane.ideplugins.services.connection.sso;

import com.hpe.adm.nga.sdk.authentication.Authentication;

/**
 * Used by custom http client
 */
public class SsoAuthentication implements Authentication {

    private String clientHeader;

    public Runnable pollingStartedHandler;

    public SsoAuthentication(String clientHeader) {
        this.clientHeader = clientHeader;
    }

    public Runnable getPollingStartedHandler() {
        return pollingStartedHandler;
    }

    public void setPollingStartedHandler(Runnable pollingStartedHandler) {
        this.pollingStartedHandler = pollingStartedHandler;
    }

    @Override
    public String getAuthenticationString() {
        return null;
    }

    @Override
    public String getClientHeader() {
        return clientHeader;
    }

}
