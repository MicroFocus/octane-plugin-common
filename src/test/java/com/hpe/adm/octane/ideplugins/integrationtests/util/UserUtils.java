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

package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.UserService;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;

import java.util.*;

public class UserUtils {

    @Inject
    protected UserService userService;

    @Inject
    protected OctaneVersionService versionService;

    @Inject
    protected ServiceModule serviceModule;

    public UserUtils() {}

    /**
     * Sets a user to be the owner of another entity
     *
     * @param backlogItem - the backlog item
     * @param owner       - the user
     */
    public void setOwner(EntityModel backlogItem, EntityModel owner) {
        EntityModel updatedEntityModel = new EntityModel();
        updatedEntityModel.setValue(backlogItem.getValue(Constants.ID));
        updatedEntityModel.setValue(backlogItem.getValue(Constants.TYPE));
        updatedEntityModel.setValue(new ReferenceFieldModel(Constants.OWNER, owner));
        Entity entity = Entity.getEntityType(updatedEntityModel);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).update().entities(Collections.singleton(updatedEntityModel)).execute();
    }

    /**
     * Retrieves all the possible roles that can be assigned to a user
     *
     * @return - a list of enitityModels representing the possible roles
     */
    public List<EntityModel> getRoles() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList(Constants.User.USER_ROLES).get().execute());
    }

    /**
     * Searches for a user by its id
     *
     * @param id - user id
     * @return @null - if not found, userEntityModel if found
     */
    public EntityModel getUserById(long id) {
        EntityService entityService = serviceModule.getInstance(EntityService.class);
        return entityService.findEntity(Entity.WORKSPACE_USER, id);
    }

    /**
     * Returns the current user
     *
     * @return the users entityModel if found, @null otherwise
     */
    public EntityModel getCurrentUser() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();

        List<EntityModel> users = new ArrayList<>(octane.entityList(Constants.WORKSPACE_ENITY_NAME).get().execute());

        for (EntityModel user : users) {
            if (user.getValue(Constants.User.EMAIL).getValue().toString().equals(userService.getCurrentUser().getValue("name").getValue())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Creates relationships between users and entities
     *
     * @param user        - the user
     * @param entityModel - the new entity to be related to
     */
    public void createRelations(EntityModel user, EntityModel entityModel) {
        user.setValue(new ReferenceFieldModel("userrel", entityModel));
    }

    /**
     * Creates a new user with default password: Welcome1
     *
     * @return the newly created user entityModel, @null if it could not be
     * created
     */
    public EntityModel createNewUser(String firstName, String lastName) {

        EntityModel userEntityModel = new EntityModel();
        @SuppressWarnings("rawtypes")
        Set<FieldModel> fields = new HashSet<>();
        List<EntityModel> roles = getRoles();

        if (roles.size() == 0) {
            return null;
        }

        fields.add(new StringFieldModel(Constants.User.LAST_NAME, lastName));
        fields.add(new StringFieldModel(Constants.TYPE, Constants.User.USER_TYPE));
        fields.add(new StringFieldModel(Constants.User.FIRST_NAME, firstName));
        fields.add(new StringFieldModel(Constants.User.EMAIL, firstName + "." + lastName + Constants.User.EMAIL_DOMAIN));
        fields.add(new StringFieldModel(Constants.User.PASSWORD, Constants.User.PASSWORD_VALUE));
        fields.add(new MultiReferenceFieldModel(Constants.ROLES, Collections.singletonList(roles.get(0))));

        if (OctaneVersion.isBetween(versionService.getOctaneVersion(), OctaneVersion.EVERTON_P3, OctaneVersion.GENT_P3, false)) {
            fields.add(new StringFieldModel(Constants.User.PHONE, Constants.User.PHONE_NR));
        }

        userEntityModel.setValues(fields);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(Constants.User.USER_TYPE).create().entities(Collections.singletonList(userEntityModel)).execute().iterator().next();
    }

}
