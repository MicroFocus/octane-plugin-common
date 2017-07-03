/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONObject;

public class PartialEntity extends EntityTypeIdPair {

    String entityName;

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

        PartialEntity partialEntity = new PartialEntity(
                entityTypeIdPair.getEntityId(),
                entityName,
                entityTypeIdPair.getEntityType());

        return partialEntity;
    }
}