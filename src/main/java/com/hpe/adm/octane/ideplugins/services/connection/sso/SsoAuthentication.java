package com.hpe.adm.octane.ideplugins.services.connection.sso;

import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;

/**
 * Used by custom http client
 */
public class SsoAuthentication implements Authentication {

    public SsoAuthentication() {
    }

    @Override
    public String getAuthenticationString() {
        return null;
    }

    @Override
    public String getClientHeader() {
        return ServiceModule.CLIENT_TYPE.name();
    }

}
