/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.services;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.IdePluginsOctaneHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.GrantTokenAuthentication;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;

/**
 * Does not rely on the Octane from the DI, instead is used to validate
 * connections settings before modifying them
 */
public class TestService {

    public Octane getOctane(ConnectionSettings connectionSettings) {
        IdePluginsOctaneHttpClient idePluginsOctaneHttpClient = new IdePluginsOctaneHttpClient(connectionSettings.getBaseUrl(), ClientType.OCTANE_IDE_PLUGIN);
        idePluginsOctaneHttpClient.setLastUsedAuthentication(connectionSettings.getAuthentication());

        return new Octane.Builder(connectionSettings.getAuthentication(), idePluginsOctaneHttpClient)
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
     * @param connectionSettings instance of {@link ConnectionSettings} to test
     * @throws ServiceException on connection error
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