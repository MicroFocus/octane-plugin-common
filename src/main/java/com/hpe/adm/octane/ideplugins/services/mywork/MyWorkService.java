/*******************************************************************************
 * Copyright 2017-2023 Open Text.
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
package com.hpe.adm.octane.ideplugins.services.mywork;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.filtering.PredefinedEntityComparator;
import com.hpe.adm.octane.ideplugins.services.util.EntityUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface MyWorkService {

    // Used whenever a sorting is done based on entity type
    Comparator<Entity> entityTypeComparator = new PredefinedEntityComparator();

    Collection<EntityModel> getMyWork();

    Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap);

    boolean isAddingToMyWorkSupported();

    boolean isAddingToMyWorkSupported(Entity entityType);

    //default
    boolean isInMyWork(EntityModel entityModel);

    boolean addToMyWork(EntityModel entityModel);

    boolean removeFromMyWork(EntityModel entityModel);

    default EntityModel getEntityFromUserItem(EntityModel entity) {
        if (Entity.USER_ITEM != Entity.getEntityType(entity)) {
            throw new ServiceRuntimeException("Given param entity is not of type: user_item, type is: " + Entity.getEntityType(entity));
        }
        String followField = "my_follow_items_" + entity.getValue("entity_type").getValue();

        return (EntityModel) entity.getValue(followField).getValue();
    }

    ;

    default Collection<EntityModel> getEntitiesFromUserItems(Collection<EntityModel> entities) {
        return entities
                .stream()
                .map(e -> getEntityFromUserItem(e))
                .collect(Collectors.toList());
    }

    ;

    default boolean containsUserItem(Collection<EntityModel> entities, EntityModel entity) {
        return entities
                .stream()
                .map(e -> getEntityFromUserItem(e))
                .anyMatch(entityModel -> EntityUtil.areEqual(entityModel, getEntityFromUserItem(entity)));
    }

    ;
}