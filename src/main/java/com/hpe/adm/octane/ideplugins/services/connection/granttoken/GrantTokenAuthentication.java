package com.hpe.adm.octane.ideplugins.services.connection.granttoken;

import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.octane.ideplugins.services.connection.IdePluginsOctaneHttpClient;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;

/**
 * Used by {@link IdePluginsOctaneHttpClient}
 */
public class GrantTokenAuthentication implements Authentication {

    public GrantTokenAuthentication() {
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
