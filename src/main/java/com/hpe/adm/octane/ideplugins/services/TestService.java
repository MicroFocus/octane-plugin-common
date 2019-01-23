package com.hpe.adm.octane.ideplugins.services;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.GrantTokenAuthentication;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

/**
 * Does not rely on the Octane from the DI, instead is used to validate
 * connections settings before modifying them
 */
public class TestService {

    public Octane getOctane(ConnectionSettings connectionSettings) {
        return new Octane.Builder(connectionSettings.getAuthentication())
                .Server(connectionSettings.getBaseUrl())
                .sharedSpace(connectionSettings.getSharedSpaceId())
                .workSpace(connectionSettings.getWorkspaceId())
                .build();
    }

    public void testHttpConnection(ConnectionSettings connectionSettings) throws ServiceException {
        try {
            HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
            HttpRequest httpRequest = HTTP_TRANSPORT.createRequestFactory()
                    .buildGetRequest(new GenericUrl(connectionSettings.getBaseUrl() + "/admin/server/version"));
            int statusCode = httpRequest.execute().getStatusCode();
            if (httpRequest.execute().getStatusCode() >= 300) {
                throw new ServiceException(
                        "HTTP connection to url: " + connectionSettings.getBaseUrl() + " failed: http response code: + " + statusCode);
            }
        } catch (Exception e) {
            throw new ServiceException("HTTP connection to url: " + connectionSettings.getBaseUrl() + " failed: " + e.getMessage());
        }
    }

    /**
     * Attempts to connect to given url, basic validations should be done first
     * Check if the current connection settings are valid
     * 
     * @param connectionSettings
     *            instance of {@link ConnectionSettings} to test
     * @throws ServiceException
     *             on connection error
     */
    public void testConnection(ConnectionSettings connectionSettings) throws ServiceException {
        // Try basic http connection first
        testHttpConnection(connectionSettings);

        if (!(connectionSettings.getAuthentication() instanceof GrantTokenAuthentication)) {
            Query query = Query.statement("subtype", QueryMethod.EqualTo, Entity.WORK_ITEM_ROOT.getSubtypeName()).build();

            // Try to fetch the backlog root
            getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().query(query).execute();
        }
    }

}