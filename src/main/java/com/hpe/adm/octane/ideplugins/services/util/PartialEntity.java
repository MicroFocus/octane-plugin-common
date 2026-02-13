/*
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
 */
package com.hpe.adm.octane.ideplugins.services.util;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONObject;

public class PartialEntity extends EntityTypeIdPair {

    private String entityName;

    public PartialEntity(EntityModel entityModel){
        super(
                Long.parseLong(entityModel.getValue("id").getValue().toString()),
                Entity.getEntityType(entityModel)
        );
        this.entityName = entityModel.getValue("name").getValue().toString();
    }

    public PartialEntity(Long entityId, String entityName, Entity entityType){
        super(entityId, entityType);
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public static EntityModel toEntityModel(PartialEntity partialEntity){
        EntityModel entityModel = new EntityModel();

        entityModel.setValue(new StringFieldModel("id", String.valueOf(partialEntity.getEntityId())));

        entityModel.setValue(new StringFieldModel("type", String.valueOf(partialEntity.getEntityType().getTypeName())));

        if(partialEntity.getEntityType().isSubtype()) {
            entityModel.setValue(new StringFieldModel("subtype", String.valueOf(partialEntity.getEntityType().getSubtypeName())));
        }

        return entityModel;
    }

    public static JSONObject toJsonObject(PartialEntity partialEntity){
        JSONObject jsonObject = EntityTypeIdPair.toJsonObject(partialEntity);
        jsonObject.put("entityName", partialEntity.getEntityName());
        return jsonObject;
    }

    public static PartialEntity fromJsonObject(JSONObject jsonObject){
        if (jsonObject == null)
            return null;
        EntityTypeIdPair entityTypeIdPair = EntityTypeIdPair.fromJsonObject(jsonObject);
        String entityName = jsonObject.getString("entityName");

        return new PartialEntity(
                entityTypeIdPair.getEntityId(),
                entityName,
                entityTypeIdPair.getEntityType());
    }
}