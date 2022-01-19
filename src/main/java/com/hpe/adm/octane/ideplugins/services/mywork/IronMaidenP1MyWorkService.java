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
package com.hpe.adm.octane.ideplugins.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.LongFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.MetadataService;
import com.hpe.adm.octane.ideplugins.services.UserService;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.EntityUtil;
import com.hpe.adm.octane.ideplugins.services.util.MyWorkPreviewDefaultFields;

import java.util.*;
import java.util.stream.Collectors;

import static com.hpe.adm.octane.ideplugins.services.filtering.Entity.COMMENT;
import static com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil.*;


/**
 * Service class responsible for MyWork requests, used only for Octane server versions greater than Iron Maiden P1 (16.0.208).
 * This was implemented because of the changes made on the Octane MyWork that were introduced by default in Iron Maiden P1.
 */
public class IronMaidenP1MyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private UserService userService;

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private MetadataService metadataService;

    @Override
    public Collection<EntityModel> getMyWork() {
        return getMyWork(new HashMap<>());
    }

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {
        Collection<EntityModel> result = new ArrayList<>();

        Query.QueryBuilder qUser = Query.statement("user_item", QueryMethod.EqualTo, Query.statement("user", QueryMethod.EqualTo,
                Query.statement("id", QueryMethod.EqualTo, userService.getCurrentUserId())));

        Map<Entity, Set<String>> myWorkPreviewMapFields = MyWorkPreviewDefaultFields.getDefaultFields();

        myWorkPreviewMapFields.keySet().forEach(entity -> {
            Collection<EntityModel> items = entityService.findEntities(entity, qUser, myWorkPreviewMapFields.get(entity), true);
            result.addAll(items);
        });

        result.addAll(getCommentsAsUserItems());

        return result;
    }

    private Collection<EntityModel> getCommentsAsUserItems() {
        Set<String> fields = metadataService.getFields(Entity.COMMENT).stream().map(FieldMetadata::getName).collect(Collectors.toSet());
        Collection<EntityModel> comments = entityService.findEntities(
                COMMENT,
                createUserQuery("mention_user", userService.getCurrentUserId()),
                fields);

        return comments;
    }

    private static Collection<EntityModel> sortEntityModels(Collection<EntityModel> userItems, Entity entity) {
        try {
            return userItems
                    .stream()
                    .sorted((entityModelLeft, entityModelRight) -> {
                        Entity entityTypeLeft = Entity.getEntityType(entityModelLeft);
                        Entity entityTypeRight = Entity.getEntityType(entityModelRight);

                        if (entityTypeLeft != entityTypeRight) {
                            return entityTypeComparator.compare(entityTypeLeft, entityTypeRight);
                        } else {
                            Long leftId = Long.parseLong(entityModelLeft.getId());
                            Long rightId = Long.parseLong(entityModelRight.getId());
                            return leftId.compareTo(rightId);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return userItems;
        }
    }

    @Override
    public boolean isAddingToMyWorkSupported() {
        return true;
    }

    @Override
    public boolean isAddingToMyWorkSupported(Entity entityType) {
        return addToMyWorkEntities.contains(entityType);
    }

    private EntityModel findUserItemForEntity(EntityModel entityModel) {
        String entityType = getEntityTypeName(Entity.getEntityType(entityModel));
        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(entityModel));
        String id = entityModel.getValue("id").getValue().toString();

        Query.QueryBuilder qItem = Query.statement(
                followField, QueryMethod.EqualTo, Query.statement("id", QueryMethod.EqualTo, id));

        Query.QueryBuilder qUser = createUserQuery("user", userService.getCurrentUserId());

        Query.QueryBuilder qType = Query.statement("entity_type", QueryMethod.EqualTo, entityType);

        Collection<EntityModel> userItems = entityService.findEntities(Entity.USER_ITEM,
                qUser.and(qType).and(qItem),
                null);

        if (userItems.size() != 1) {
            return null;
        } else {
            return userItems.iterator().next();
        }
    }

    @Override
    public boolean isInMyWork(EntityModel entityModel) {
        Collection<EntityModel> myWork = getMyWork();
        return EntityUtil.containsEntityModel(myWork, entityModel);
    }

    @Override
    public boolean addToMyWork(EntityModel entityModel) {
        if (isInMyWork(entityModel)) {
            return false;
        }

        EntityModel newUserItem = createNewUserItem(entityModel);

        octaneProvider
                .getOctane()
                .entityList(Entity.USER_ITEM.getApiEntityName())
                .create()
                .entities(Collections.singletonList(newUserItem))
                .execute();

        return true;
    }

    protected EntityModel createNewUserItem(EntityModel wrappedEntityModel) {
        EntityModel newUserItem = new EntityModel();
        String entityType = getEntityTypeName(Entity.getEntityType(wrappedEntityModel));
        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(wrappedEntityModel));

        newUserItem.setValue(new LongFieldModel("origin", 1L));
        newUserItem.setValue(new StringFieldModel("entity_type", entityType));
        newUserItem.setValue(new ReferenceFieldModel("user", userService.getCurrentUser()));
        newUserItem.setValue(new ReferenceFieldModel(followField, wrappedEntityModel));

        return newUserItem;
    }

    @Override
    public boolean removeFromMyWork(EntityModel entityModel) {
        EntityModel userItem = findUserItemForEntity(entityModel);

        if (userItem == null) {
            return false;
        }

        String id = userItem.getValue("id").getValue().toString();

        try {
            octaneProvider.getOctane().entityList(Entity.USER_ITEM.getApiEntityName()).at(id).delete().execute();
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    @Override
    public EntityModel getEntityFromUserItem(EntityModel entity) {
        return entity;
    }
}
