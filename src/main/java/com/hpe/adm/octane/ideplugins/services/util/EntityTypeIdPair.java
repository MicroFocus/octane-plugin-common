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

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONObject;

/**
 * Can be used to as an ID to uniquely identify an entity from a collection of entity models
 */
public class EntityTypeIdPair {

    private Entity entityType;
    private Long entityId;

    public EntityTypeIdPair(Long entityId, Entity entityType) {
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public Entity getEntityType() {
        return entityType;
    }

    public void setEntityType(Entity entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public static JSONObject toJsonObject(EntityTypeIdPair entityTypeIdPair){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", entityTypeIdPair.getEntityId());
        jsonObject.put("entityType", entityTypeIdPair.getEntityType().name());
        return jsonObject;
    }

    public static EntityTypeIdPair fromJsonObject(JSONObject jsonObject){
        if(jsonObject == null){
            return null;
        }

        Long entityId = jsonObject.getLong("id");
        Entity entityType = Entity.valueOf(jsonObject.getString("entityType"));
        return new EntityTypeIdPair(entityId, entityType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityTypeIdPair that = (EntityTypeIdPair) o;

        if (entityType != that.entityType) return false;
        return entityId != null ? entityId.equals(that.entityId) : that.entityId == null;

    }

    @Override
    public int hashCode() {
        int result = entityType != null ? entityType.hashCode() : 0;
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        return result;
    }

}