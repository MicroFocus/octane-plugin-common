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
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;

public class UserService {

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private EntityModel currentUserEntityModel;
    private ConnectionSettings lastConnectionSettings;

    private Runnable getCurrentUserRunnable = new Runnable() {
        @Override
        public void run() {
            Octane octane = octaneProvider.getOctane();

            currentUserEntityModel = octane.entityList("current_user").at("").get().execute();

            /*
            String currentUser = connectionSettingsProvider.getConnectionSettings().getUserName();

            EntityList entityList = octane.entityList(Entity.WORKSPACE_USER.getApiEntityName());
            Collection<EntityModel> entityModels =
                    entityList.get().query(
                             Query.statement("name", QueryMethod.EqualTo, currentUser).build())
                            .execute();

            if(entityModels.size()!=1){
                throw new ServiceRuntimeException("Failed to retrieve logged in user id");
            } else {
                currentUserEntityModel = entityModels.iterator().next();
            }
            */
        }
    };


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