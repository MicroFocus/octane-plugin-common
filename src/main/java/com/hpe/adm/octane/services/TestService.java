/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpe.adm.octane.services;


import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.di.ServiceModule;
import com.hpe.adm.octane.services.exception.ServiceException;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.util.SdkUtil;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Does not rely on the Octane from the DI,
 * instead is used to validate connections settings before modifying them
 */
public class TestService {

    public Octane getOctane(ConnectionSettings connectionSettings){
        Octane octane = new Octane.Builder(new SimpleUserAuthentication(connectionSettings.getUserName(),
                connectionSettings.getPassword(), ServiceModule.CLIENT_TYPE.name()))
                .Server(connectionSettings.getBaseUrl())
                .sharedSpace(connectionSettings.getSharedSpaceId())
                .workSpace(connectionSettings.getWorkspaceId())
                .build();

        return octane;
    }

    private void testHttpConnection(ConnectionSettings connectionSettings) throws ServiceException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(connectionSettings.getBaseUrl()).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                // Not OK.
            }
        } catch (Exception ex){
            throw new ServiceException("HTTP connection to url: " + connectionSettings.getBaseUrl() + " failed.");
        }
    }

    /**
     * Attempts to connect to given url, basic validations should be done first
     * Check if the current connection settings are valid
     */
    public void testConnection(ConnectionSettings connectionSettings) throws ServiceException {

        //Try basic http connection first
        testHttpConnection(connectionSettings);

        try{
            Query query =  Query.statement("subtype", QueryMethod.EqualTo, Entity.WORK_ITEM_ROOT.getSubtypeName()).build();
            //Try to fetch the backlog root
            getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().query(query).execute();
        } catch (Exception ex){
            String message = null;

            if(ex instanceof OctaneException){
                message = SdkUtil.getMessageFromOctaneException((OctaneException)ex);
            }
            //Default
            if(message == null) {
                message = ex.getMessage();
            }

            throw new ServiceException(message, ex);
        }
    }

}