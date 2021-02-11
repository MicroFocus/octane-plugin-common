/*
 * Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
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
package com.hpe.adm.octane.ideplugins.services.util;

import com.google.api.client.util.Charsets;
import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.UserAuthentication;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UrlParser {

    private static final String INVALID_URL_FORMAT_MESSAGE = "Given server URL is not valid.";

    public static ConnectionSettings resolveConnectionSettings(String url, Authentication authentication) throws ServiceException {
        ConnectionSettings connectionSettings = resolveConnectionSettingsFromUrl(url);
        connectionSettings.setAuthentication(authentication);
        return connectionSettings;
    }

    /**
     * @deprecated use
     *             {@link #resolveConnectionSettings(String, Authentication)}
     * @param url server url
     * @param userName octane username
     * @param password octane password
     * @return ConnectionSettings object built from params
     * @throws ServiceException for invalid url format
     */
    @Deprecated
    public static ConnectionSettings resolveConnectionSettings(String url, String userName, String password) throws ServiceException {
        ConnectionSettings connectionSettings = resolveConnectionSettingsFromUrl(url);
        UserAuthentication authentication = new UserAuthentication(userName, password);
        connectionSettings.setAuthentication(authentication);
        return connectionSettings;
    }

    private static ConnectionSettings resolveConnectionSettingsFromUrl(String url) throws ServiceException {
        ConnectionSettings connectionSettings = new ConnectionSettings();

        URL siteUrl;


        try {
            siteUrl = new URL(url);

            // get rid of what we don't need
            siteUrl = new URL(siteUrl.getProtocol(), siteUrl.getHost(), siteUrl.getPort(), siteUrl.getFile());

            siteUrl.toURI(); // does the extra checking required for validation
            // of URI
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
                long sharedspaceId = -1;
                long workspaceId = -1;

                baseUrl = siteUrl.getProtocol() + "://" + siteUrl.getHost();
                // Add port if not the default
                if (siteUrl.getPort() != 80 && siteUrl.getPort() != -1) {
                    baseUrl += ":" + siteUrl.getPort();
                }

                String siteUrlPath = siteUrl.getPath();
                if (siteUrlPath.endsWith("/ui/"))
                    baseUrl += siteUrlPath.substring(0, siteUrlPath.length() - 4); // remove the `/ui/` so we don't include it into baseUrl
                else if (!siteUrlPath.equals("/"))
                    baseUrl += siteUrlPath.substring(0, siteUrlPath.length() - 1); // remove the last `/`

                Map<String, List<String>> params = splitQueryParams(siteUrl);

                if(params.containsKey("p") && params.get("p").size() == 1) {
                    String param = params.get("p").get(0);
                    String[] split = param.split("/");
                    sharedspaceId = Long.valueOf(split[0].trim());
                    workspaceId = Long.valueOf(split[1].trim());
                }

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

        return connectionSettings;
    }

    /**
     * Create an octane url from a connection settings object
     *
     * @param connectionSettings
     *            {@link ConnectionSettings} object
     * @return octane browser url or null if if any of the req. fields are
     *         missing (base url, workspace id, shared space id)
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
     *            for the base url info
     * @param entityType
     *            added to url
     * @param id
     *            entity id
     * @return URI to Octane web ui, to display the entity
     */
    public static URI createEntityWebURI(ConnectionSettings connectionSettings, Entity entityType, Integer id) {
        // ex:
        // http://myd-vm24085.hpeswlab.net:8080/ui/entity-navigation?p=1001/1002&entityType=test&id=1001
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(connectionSettings.getBaseUrl())
                .append("/ui/entity-navigation?p=")
                .append(connectionSettings.getSharedSpaceId())
                .append("/")
                .append(connectionSettings.getWorkspaceId())
                .append("&entityType=").append(entityType.getTypeName())
                .append("&id=").append(id);

        URI uri;

        try {
            uri = new URI(stringBuilder.toString());
        } catch (URISyntaxException e) {
            throw new ServiceRuntimeException(e);
        }
        return uri;
    }

    public static Map<String, List<String>> splitQueryParams(URL url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<>();

        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");

            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;

            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;

            query_pairs.get(key).add(value);
        }

        return query_pairs;
    }

    public static String urlEncodeQueryParamValue(String queryParamValue) {
        try {
            return URLEncoder.encode(queryParamValue, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
