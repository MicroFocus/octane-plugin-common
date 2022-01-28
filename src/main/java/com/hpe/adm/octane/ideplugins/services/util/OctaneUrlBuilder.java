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
package com.hpe.adm.octane.ideplugins.services.util;

import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
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
