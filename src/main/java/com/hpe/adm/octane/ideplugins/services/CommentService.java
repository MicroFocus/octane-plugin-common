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
package com.hpe.adm.octane.ideplugins.services;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.entities.get.GetEntities;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Comments only seem to work for work_item and tests (composite types)
 */
public class CommentService {

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Inject
    protected HttpClientProvider httpClientProvider;

    @Inject
    private UserService userService;

    // Aggregate types include their subtypes when the check is done
    private static final BiMap<Entity, String> supportedEntities = HashBiMap.create();
    static {
        supportedEntities.put(Entity.WORK_ITEM, "owner_work_item");
        supportedEntities.put(Entity.TEST, "owner_test");
        supportedEntities.put(Entity.REQUIREMENT, "owner_requirement");
        supportedEntities.put(Entity.TEST_RUN, "owner_run");

    }

    private String getCommentReferenceFieldName(Entity entityType) {
        if (supportedEntities.keySet().contains(entityType)) {
            return supportedEntities.get(entityType);
        }
        if (entityType.isSubtype() && supportedEntities.keySet().contains(entityType.getSubtypeOf())) {
            return supportedEntities.get(entityType.getSubtypeOf());
        }
        return null;
    }

    public Collection<EntityModel> getComments(EntityModel entityModel) {
        Entity entityType = Entity.getEntityType(entityModel);
        String id = entityModel.getValue("id").getValue().toString();
        return getComments(entityType, id);
    }

    public Collection<EntityModel> getComments(Entity entityType, String id) {
        // Check if comments are supported

        String referenceFieldName = getCommentReferenceFieldName(entityType);
        if (referenceFieldName != null) {
            return getComments(referenceFieldName, id);
        }

        // TODO: atoth: probably better to check features using the sdk metadata
        // service
        throw new ServiceRuntimeException("Comments not supported for: " + entityType);
    }

    private Collection<EntityModel> getComments(String referenceFieldName, String id) {
        Octane octane = octaneProvider.getOctane();

        GetEntities get = octane.entityList(Entity.COMMENT.getApiEntityName()).get();

        Query query = Query.statement(referenceFieldName, QueryMethod.EqualTo,
                Query.statement("id", QueryMethod.EqualTo, id)).build();

        return get.query(query)
                .addOrderBy("creation_time", false)
                .execute();
    }

    public boolean dismissComment(EntityModel entityModel) {
        try {

            ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();

            String putUrl = connectionSettings.getBaseUrl() + "/internal-api/shared_spaces/" +
                    connectionSettings.getSharedSpaceId() + "/workspaces/" +
                    connectionSettings.getWorkspaceId() + "/comments/" +
                    entityModel.getValue("id").getValue() + "/dismiss";

            OctaneHttpRequest dismissCommentRequest = new OctaneHttpRequest.PutOctaneHttpRequest(putUrl, OctaneHttpRequest.JSON_CONTENT_TYPE,"");
            httpClientProvider.getOctaneHttpClient().execute(dismissCommentRequest);

            return true;
        } catch (Exception e) {
            //log me
        }
        return false;
    }

    /**
     * Add comment to entity
     * 
     * @param entityType
     *            {@link Entity}
     * @param id
     *            entity id
     * @param text
     *            should be html
     */
    public void postComment(Entity entityType, String id, String text) {
        Octane octane = octaneProvider.getOctane();
        String referenceFieldName = getCommentReferenceFieldName(entityType);

        if (referenceFieldName == null) {
            throw new ServiceRuntimeException("Comments not supported for: " + entityType);
        }

        // Create comment entity
        Set<FieldModel> fields = new HashSet<>();

        fields.add(new ReferenceFieldModel("author", userService.getCurrentUser()));
        fields.add(new ReferenceFieldModel(referenceFieldName, createOwner(entityType, id)));
        fields.add(new StringFieldModel("text", text));
        EntityModel newComment = new EntityModel(fields);

        octane.entityList(
                Entity.COMMENT.getApiEntityName())
                .create()
                .entities(Lists.asList(newComment, new EntityModel[] {}))
                .execute();
    }

    public void postComment(EntityModel entityModel, String text) {
        Entity entityType = Entity.getEntityType(entityModel);
        String id = entityModel.getValue("id").getValue().toString();
        postComment(entityType, id, text);
    }

    public EntityModel deleteComment(String commentId) {
        Octane octane = octaneProvider.getOctane();

        return octane.entityList(Entity.COMMENT.getApiEntityName())
                .at(commentId)
                .delete()
                .execute();
    }

    private EntityModel createOwner(Entity entityType, String id) {
        // Create owner entity (needs id and type)
        Set<FieldModel> ownerFields = new HashSet<>();
        ownerFields.add(new StringFieldModel("id", id));
        String apiEntityType = supportedEntities.inverse().get(getCommentReferenceFieldName(entityType)).getTypeName();
        ownerFields.add(new StringFieldModel("type", apiEntityType));
        EntityModel owner = new EntityModel(ownerFields);
        return owner;
    }

}
