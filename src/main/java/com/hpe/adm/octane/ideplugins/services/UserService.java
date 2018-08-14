/*
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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

package com.hpe.adm.octane.ideplugins.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ModelParser;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import org.json.JSONObject;

public class UserService {

    @Inject
    private HttpClientProvider httpClientProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private EntityModel currentUserEntityModel;
    private ConnectionSettings lastConnectionSettings;

    private Runnable getCurrentUserRunnable = new Runnable() {
        @Override
        public void run() {
            ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
            OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(connectionSettings.getBaseUrl() + "/api/current_user/");
            OctaneHttpResponse response = httpClient.execute(request);
            String json = response.getContent();

            if (response.isSuccessStatusCode() && (json != null && !json.isEmpty())) {
                currentUserEntityModel = ModelParser.getInstance().getEntityModel(new JSONObject(json));
                currentUserEntityModel = convertSiteUserToWorkspaceUser(currentUserEntityModel);
            } else {
                throw new ServiceRuntimeException("Failed to fetch current logged on user");
            }
        }
    };

    private EntityModel convertSiteUserToWorkspaceUser(EntityModel siteUser) {
        if("site_user".equals(siteUser.getValue("type").getValue())){
            siteUser.setValue(new StringFieldModel("type", "workspace_user"));
        }
        return siteUser;
    }


    /**
     * Well this is horrible, this method is needed because cross filtering work item owner by name does not work
     * @return id of the current user from the service context
     */
    public Long getCurrentUserId(){
        EntityModel user = getCurrentUser();
        return Long.parseLong(user.getValue("id").getValue().toString());
    }

    public EntityModel getCurrentUser(){
        if(currentUserEntityModel == null || (!lastConnectionSettings.equals(connectionSettingsProvider.getConnectionSettings()))){
            getCurrentUserRunnable.run();
            lastConnectionSettings = connectionSettingsProvider.getConnectionSettings();
        }
        return currentUserEntityModel;
    }

}