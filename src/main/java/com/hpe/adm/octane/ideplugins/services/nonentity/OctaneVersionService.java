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
package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the Octane version.
 */
public class OctaneVersionService {

    private static final Logger logger = LoggerFactory.getLogger(OctaneVersionService.class.getClass());

    public static final OctaneVersion fallbackVersion = OctaneVersion.EVERTON_P3;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private String versionString;
    private final Runnable resetVersionRunnable = () -> versionString = null;

    private static String getVersionString(ConnectionSettings connectionSettings) {
        try {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(getServerVersionUrl(connectionSettings));
            OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl(), connectionSettings.getAuthentication());
            OctaneHttpResponse response = octaneHttpClient.execute(request);
            String jsonString = response.getContent();
            return new JsonParser().parse(jsonString).getAsJsonObject().get("display_version").getAsString();
        } catch (Exception e) {
            logger.error("Failed to retrieve Octane server version");
            throw new ServiceRuntimeException("Failed to retrieve Octane server version", e);
        }
    }

    public static String getServerVersionUrl(ConnectionSettings connectionSettings){
        return connectionSettings.getBaseUrl() + "/admin/server/version";
    }

    public OctaneVersion getOctaneVersion() {
        if (versionString == null) {
            connectionSettingsProvider.addChangeHandler(resetVersionRunnable);
            versionString = getVersionString(connectionSettingsProvider.getConnectionSettings());
        }
        return new OctaneVersion(versionString);
    }

    public OctaneVersion getOctaneVersion(boolean useFallback) {
        try {
            return getOctaneVersion();
        } catch (Exception ex){
            if(useFallback){
                versionString = fallbackVersion.getVersionString();
                return fallbackVersion;
            } else {
                throw ex;
            }
        }
    }

    public static OctaneVersion getOctaneVersion(ConnectionSettings connectionSettings) {
        String versionString = getVersionString(connectionSettings);
        return new OctaneVersion(versionString);
    }

}
