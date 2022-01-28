/*
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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
package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;


public class DownloadScriptService {
    @Inject
    protected HttpClientProvider httpClientProvider;
    @Inject
    protected ConnectionSettingsProvider connectionSettingsProvider;

    private static final Logger logger = LoggerFactory.getLogger(DownloadScriptService.class.getName());

    public String getTestScriptContent(long testId) throws UnsupportedEncodingException {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();
        if (null != httpClient) {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(connectionSettings.getBaseUrl() + "/api/shared_spaces/" + connectionSettings.getSharedSpaceId() +
                    "/workspaces/" + connectionSettings.getWorkspaceId() + "/tests/" + testId + "/script");
            OctaneHttpResponse response = httpClient.execute(request);
            String jsonString = response.getContent();
            jsonString =  new JsonParser().parse(jsonString).getAsJsonObject().get("script").getAsString();

            try {
                return new String(jsonString.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("Unsupported Encoding");
                throw e;
            }
        }
        return null;
    }
}
