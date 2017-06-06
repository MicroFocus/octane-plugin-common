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

package com.hpe.adm.octane.services.util;

import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.exception.ServiceException;
import com.hpe.adm.octane.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.services.filtering.Entity;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlParser {

    private static final String INVALID_URL_FORMAT_MESSAGE = "Given server URL is not valid.";

    public static ConnectionSettings resolveConnectionSettings(String url, String userName, String password) throws ServiceException {

        ConnectionSettings connectionSettings = new ConnectionSettings();

        URL siteUrl;
        try {
            siteUrl = new URL(url);
            siteUrl.toURI(); // does the extra checking required for validation of URI
            if (!"http".equals(siteUrl.getProtocol()) && !"https".equals(siteUrl.getProtocol())) {
                throw new Exception();
            }
            int paramIndex = url.indexOf("p=");
            if (paramIndex == -1) {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new ServiceException(INVALID_URL_FORMAT_MESSAGE);
        }

        if (null == siteUrl.getQuery()) {
            throw new ServiceException("Missing query parameters.");
        } else {

            try {

                String baseUrl;
                Long sharedspaceId;
                Long workspaceId;

                baseUrl = siteUrl.getProtocol() + "://" + siteUrl.getHost();
                //Add port if not the default
                if (siteUrl.getPort() != 80 && siteUrl.getPort() != -1) {
                    baseUrl += ":" + siteUrl.getPort();
                }

                String[] split = siteUrl.getQuery().split("/");
                sharedspaceId = Long.valueOf(split[0].substring(split[0].indexOf("p=") + 2));
                workspaceId = Long.valueOf(split[1]);

                if (sharedspaceId < 0)
                    throw new Exception();

                if (workspaceId < 0)
                    throw new Exception();


                connectionSettings.setBaseUrl(baseUrl);
                connectionSettings.setSharedSpaceId(sharedspaceId);
                connectionSettings.setWorkspaceId(workspaceId);


            } catch (Exception ex) {
                throw new ServiceException("Could not get sharedspace/workspace ids from URL. ");
            }

        }

        connectionSettings.setUserName(userName);
        connectionSettings.setPassword(password);

        return connectionSettings;
    }

    /**
     * Create an octane url from a connection settings object
     *
     * @param connectionSettings {@link ConnectionSettings} object
     * @return octane browser url or null if if any of the req. fields are missing (base url, workspace id, shared space id)
     */
    public static String createUrlFromConnectionSettings(ConnectionSettings connectionSettings) {

        if (connectionSettings.getBaseUrl() == null ||
                connectionSettings.getWorkspaceId() == null ||
                connectionSettings.getSharedSpaceId() == null) {
            return null;
        }

        return connectionSettings.getBaseUrl()
                + "/?"
                + "p=" + connectionSettings.getSharedSpaceId()
                + "/" + connectionSettings.getWorkspaceId();
    }

    public static String removeHash(String url) {
        if (url.contains("#")) {
            return url.substring(0, url.indexOf("#"));
        }
        return url;
    }

    /**
     * Create an octane ui link from an entity
     *
     * @param connectionSettings
     * @param entityType
     * @param id
     * @return
     */
    public static URI createEntityWebURI(ConnectionSettings connectionSettings, Entity entityType, Integer id) {
        //ex: http://myd-vm24085.hpeswlab.net:8080/ui/entity-navigation?p=1001/1002&entityType=test&id=1001
        StringBuilder sb = new StringBuilder();

        sb.append(connectionSettings.getBaseUrl());
        sb.append("/ui/entity-navigation?p=");
        sb.append(connectionSettings.getSharedSpaceId() + "/" + connectionSettings.getWorkspaceId());
        sb.append("&entityType=" + entityType.getTypeName());
        sb.append("&id=" + id);

        URI uri = null;

        try {
            uri = new URI(sb.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new ServiceRuntimeException(e);
        }

        return uri;
    }

}
