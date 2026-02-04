/*******************************************************************************
 * Copyright 2017-2026 Open Text.
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
package com.hpe.adm.octane.ideplugins.integrationtests.util;


import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.apache.commons.lang.StringEscapeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Data generator for unit tests
 * TODO: find a nice way to move this into tests only (DI issues, currently part of the ServiceModule)
 */
public class EntityUtils {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat generateNameDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");

    private static final Set<String> transitionFields = new HashSet<>();
    static {
        transitionFields.add("source_phase");
        transitionFields.add("target_phase");
        transitionFields.add("is_primary");
        transitionFields.add("entity");
    }

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private EntitySearchService searchService;

    @Inject
    private OctaneVersionService versionService;

    private EntityModel nativeStatus;

    public EntityUtils(){
    }

    private Map<Entity, Integer> generatedEntityCount = new HashMap<>();

    public EntityModel createEntityModel(Entity entity) {

        EntityModel newEntity = new EntityModel();

        if(entity.isSubtype()){
            newEntity.setValue(new StringFieldModel("subtype", entity.getSubtypeName()));
        }

        newEntity.setValue(new StringFieldModel("name", generateEntityName(entity)));

        String type = entity.getApiEntityName();
        if(!type.contains("test") && !type.contains("task")) {
            newEntity.setValue(new ReferenceFieldModel("parent", getWorkItemRoot()));
        }
        newEntity.setValue(new ReferenceFieldModel("phase", getDefaultPhase(entity)));

        Collection<EntityModel> createdEntities = octaneProvider
                .getOctane()
                .entityList(entity.getApiEntityName())
                .create()
                .entities(Collections.singleton(newEntity))
                .execute();

        if(createdEntities.size() != 1){
            throw new RuntimeException("Failed to create entity of type: " + entity);
        }

        String newEntityId = createdEntities.iterator().next().getValue("id").getValue().toString();

        //Refresh entity fields, some might have been set by the server
        newEntity = octaneProvider
                .getOctane()
                .entityList(entity.getApiEntityName())
                .at(newEntityId)
                .get()
                .execute();

        return newEntity;
    }

    public void deleteEntityModel(EntityModel entityModel){
        octaneProvider
                .getOctane()
                .entityList(Entity.getEntityType(entityModel).getApiEntityName())
                .at(entityModel.getValue("id").getValue().toString())
                .delete()
                .execute();

    }

    public String generateEntityName(Entity entity) {
        if (!generatedEntityCount.containsKey(entity)) {
            generatedEntityCount.put(entity, 0);
        }

        int count = generatedEntityCount.get(entity);
        count++;
        generatedEntityCount.put(entity, count);

        return entity.getEntityName() + ":" + generateNameDateFormat.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
    }

    public EntityModel getWorkItemRoot() {
        Octane octane = octaneProvider.getOctane();
        Query query = Query.statement("subtype", QueryMethod.EqualTo, "work_item_root").build();
        Collection<EntityModel> roots = octane.entityList("work_items").get().query(query).execute();

        if (roots.size() != 1) {
            throw new RuntimeException("Error fetching work item root, got num of results: " + roots.size());
        }

        return roots.iterator().next();
    }

    public EntityModel getDefaultPhase(Entity entityType){

        String entityName;
        if (entityType.isSubtype()) {
            entityName = entityType.getSubtypeName();
        } else {
            entityName = entityType.getTypeName();
        }

        Octane octane = octaneProvider.getOctane();

        EntityModel transitionEntity = octane
                .entityList(Entity.TRANSITION.getApiEntityName())
                .get()
                .query(Query.statement("entity", QueryMethod.EqualTo, entityName).build())
                .addFields(transitionFields.toArray(new String[]{}))
                .execute()
                .iterator()
                .next();

        EntityModel phaseEntity = (EntityModel) transitionEntity.getValue("source_phase").getValue();

        return phaseEntity;
    }

    /**
     * Sets the description of an entity
     *
     * @param backlogItem
     *            the backlog item
     * @param description
     *            the description string
     */
    public void setDescription(EntityModel backlogItem, String description) {
        EntityModel updatedEntityModel = new EntityModel();
        updatedEntityModel.setValue(backlogItem.getValue(Constants.ID));
        updatedEntityModel.setValue(backlogItem.getValue(Constants.TYPE));
        updatedEntityModel.setValue(new StringFieldModel(Constants.DESCRIPTION, description));
        Entity entity = Entity.getEntityType(updatedEntityModel);
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).update().entities(Collections.singleton(updatedEntityModel)).execute();
    }

    public String removeTags(String s) {
        String result;
        if (s.contains("<em>")) {
            s = StringEscapeUtils.unescapeHtml(s);
            result = s.replaceAll("<em>", "");
            result = result.replaceAll("</em>", "");
            return result;
        }
        return s;
    }

    public boolean compareEntities(EntityModel entity1, EntityModel entity2) {
        if((entity1.getValue("id").getValue().toString().equals(entity2.getValue(Constants.ID).getValue().toString()))
                && entity1.getValue("type").getValue().toString().equals(entity2.getValue(Constants.TYPE).getValue().toString())) {
            return true;
        }
        return false;
    }

    public EntityModel search(String searchField, String query) {
        Collection<EntityModel> searchResults = searchService.searchGlobal(
                query,
                20,
                Constants.SearchEntityTypes.toArray(new Entity[] {}));

        for (EntityModel entityModel : searchResults) {
            if (removeTags(entityModel.getValue(searchField).getValue().toString()).contains(query)) {
                return entityModel;
            }
        }

        return null;
    }

    public EntityModel getNativeStatus() {
        if(nativeStatus == null) {
            nativeStatus = new EntityModel(Constants.TYPE, Constants.NativeStatus.NATIVE_STATUS_TYPE_VALUE);
            if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.HIGHER, OctaneVersion.GENT_P3)) {
                nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_RUN_ID));
            } else if (OctaneVersion.isBetween(versionService.getOctaneVersion(), OctaneVersion.EVERTON_P3, OctaneVersion.GENT_P3, false)) {
                nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_NEW_ID));
            } else if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.LOWER_EQ, OctaneVersion.EVERTON_P3)) {
                nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_OLD_ID));
            }
        }
        return nativeStatus;
    }


    /**
     * Creates a new entity
     *
     * @param entity - the new entity
     * @return the created entityModel, @null if it could not been created
     */
    public EntityModel createEntity(Entity entity) {
        EntityModel entityModel = createEntityModel(entity);
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(entityModel));
        return entityModel;
    }
}