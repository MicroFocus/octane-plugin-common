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
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.nga.sdk.entities.EntityList;
import com.hpe.adm.nga.sdk.entities.OctaneCollection;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ModelParser;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.connection.*;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.GrantTokenAuthentication;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONObject;

import java.util.Collection;

public class UserService {

    @Inject
    private HttpClientProvider httpClientProvider;

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private EntityModel currentUserEntityModel;
    private ConnectionSettings lastConnectionSettings;

    private Runnable getCurrentUserRunnable = new Runnable() {
        @Override
        public void run() {
            if(connectionSettingsProvider.getConnectionSettings().getAuthentication() instanceof GrantTokenAuthentication) {
                initCurrentUserWithNewAPI();
            } else {
                initCurrentUserWithOldAPI();
            }
        }
    };

    private void initCurrentUserWithNewAPI() {
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

    /**
     * TODO: you need workspace user ID, need to fix next push server side, or
     * rewrite the whole code to use the name instead of the ID
     *
     * @param siteUser
     * @return
     */
    private EntityModel convertSiteUserToWorkspaceUser(EntityModel siteUser) {

        OctaneCollection<EntityModel> result = octaneProvider
                .getOctane()
                .entityList(Entity.WORKSPACE_USER.getApiEntityName())
                .get()
                .query(Query
                        .statement("name", QueryMethod.EqualTo, siteUser.getValue("name").getValue().toString())
                        .build())
                .execute();

        if (result.size() != 1) {
            throw new ServiceRuntimeException("Failed to get current logged in worksapce user");
        }

        return result.iterator().next();
    }

    private void initCurrentUserWithOldAPI() {
        Octane octane = octaneProvider.getOctane();
        Authentication authentication = connectionSettingsProvider.getConnectionSettings().getAuthentication();

        String currentUserName = ((UserAuthentication) authentication).getUserName();

        EntityList entityList = octane.entityList(Entity.WORKSPACE_USER.getApiEntityName());
        Collection<EntityModel> entityModels =
                entityList.get().query(
                        Query.statement("name", QueryMethod.EqualTo, currentUserName).build())
                        .execute();

        if(entityModels.size()!=1){
            throw new ServiceRuntimeException("Failed to retrieve logged in user id");
        } else {
            currentUserEntityModel = entityModels.iterator().next();
        }
    }

    /**
     * Well this is horrible, this method is needed because cross filtering work
     * item owner by name does not work
     * 
     * @return id of the current user from the service context
     */
    public Long getCurrentUserId() {
        EntityModel user = getCurrentUser();
        return Long.parseLong(user.getValue("id").getValue().toString());
    }

    public EntityModel getCurrentUser() {
        if (currentUserEntityModel == null || (!lastConnectionSettings.equals(connectionSettingsProvider.getConnectionSettings()))) {
            getCurrentUserRunnable.run();
            lastConnectionSettings = connectionSettingsProvider.getConnectionSettings();
        }
        return currentUserEntityModel;
    }

}