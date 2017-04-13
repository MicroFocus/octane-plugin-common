package com.hpe.adm.octane.services.util;

import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.exception.ServiceRuntimeException;
import org.apache.http.client.utils.URIBuilder;

public class OctaneUrlBuilder {

    public static URIBuilder buildOctaneUri(ConnectionSettings connectionSettings, String entityName) {
        URIBuilder uriBuilder = new URIBuilder();
        //set the scheme, the protocol must be removed for the uri builder setHost method
        if (connectionSettings.getBaseUrl().contains("https")) {
            uriBuilder.setScheme("https");
            uriBuilder.setHost(connectionSettings.getBaseUrl().replace("https://", ""));
        } else if (connectionSettings.getBaseUrl().contains("http")) {
            uriBuilder.setScheme("http");
            uriBuilder.setHost(connectionSettings.getBaseUrl().replace("http://", ""));
        } else {
            throw new ServiceRuntimeException("Cannot find http/https protocol is connections settings base URL");
        }

        uriBuilder.setPath(
                "/api"
                        + "/shared_spaces/" + connectionSettings.getSharedSpaceId()
                        + "/workspaces/" + connectionSettings.getWorkspaceId()
                        + "/" + entityName);
        return uriBuilder;
    }
}
