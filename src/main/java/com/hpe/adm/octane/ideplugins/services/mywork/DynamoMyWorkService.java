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
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.MultiReferenceFieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.UserService;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.EntityUtil;

import java.util.*;
import java.util.stream.Collectors;

import static com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil.*;

class DynamoMyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private UserService userService;

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private DynamoMyWorkFilterCriteria dynamoMyWorkFilterCriteria;

    private static final String FOLLOW_ITEMS_OWNER_FIELD = "my_follow_items_owner";
    private static final String NEW_ITEMS_OWNER_FIELD = "my_new_items_owner";

    @Override
    public Collection<EntityModel> getMyWork() {
        return getMyWork(new HashMap<>());
    }

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {

        Map<Entity, Collection<EntityModel>> resultMap;

        //Get entities by query
        resultMap = entityService.concurrentFindEntities(dynamoMyWorkFilterCriteria.getStaticFilterCriteria(), fieldListMap);

        // Wrap into user items, for backwards compatibility with the UI
        // origin is 0 (because they were fetched via the static query (business rule in the future)
        resultMap
                .keySet()
                .forEach(entityType ->
                        resultMap.put(entityType,
                                MyWorkUtil.wrapCollectionIntoUserItem(resultMap.get(entityType), 0))
                );

        //Get items that were added manually
        Map<Entity, Collection<EntityModel>> addedEntities = getAddedItems(fieldListMap);

        //Also wrap the addedEntities with origin 1
        addedEntities
                .keySet()
                .forEach(entityType ->
                        addedEntities.put(entityType,
                                MyWorkUtil.wrapCollectionIntoUserItem(addedEntities.get(entityType), 1))
                );

        //Make sure the result map has all the keys necessary to merge the two maps
        addedEntities
                .keySet()
                .stream()
                .filter(entityType -> !resultMap.containsKey(entityType))
                .forEach(entityType -> resultMap.put(entityType, new ArrayList<>()));

        //Merge the two maps, check to not add duplicates
        addedEntities
                .keySet()
                .forEach(entityType -> {
                    Collection<EntityModel> queryEntitiesByKey = resultMap.get(entityType);
                    Collection<EntityModel> addedEntitiesByKey = addedEntities.get(entityType);

                    for (EntityModel userItem : addedEntitiesByKey) {
                        if (!containsUserItem(queryEntitiesByKey, userItem)) {
                            resultMap.get(entityType).add(userItem);
                        }
                    }
                });

        //Convert map to a list and return
        return resultMap
                .keySet()
                .stream()
                .sorted(entityTypeComparator)
                .flatMap(entityType -> resultMap.get(entityType).stream())
                .collect(Collectors.toList());
    }

    protected Map<Entity, Collection<EntityModel>> getAddedItems(Map<Entity, Set<String>> fieldListMap) {

        final Map<Entity, Set<String>> fieldListMapCopy = cloneFieldListMap(fieldListMap);

        String addToMyWorkFieldName = "my_follow_items_owner";

        Map<Entity, Query.QueryBuilder> followFilterCriteria = new HashMap<>();

        dynamoMyWorkFilterCriteria.getStaticFilterCriteria()
                .keySet()
                .stream()
                .filter(this::isAddingToMyWorkSupported)
                .forEach(key -> {

                    Query.QueryBuilder qb;
                    if (key.isSubtype()) {
                        qb = key.createMatchSubtypeQueryBuilder().and(createUserQuery(addToMyWorkFieldName, userService.getCurrentUserId()));
                    } else {
                        qb = createUserQuery(addToMyWorkFieldName, userService.getCurrentUserId());
                    }

                    followFilterCriteria.put(key, qb);

                    if (fieldListMapCopy != null && fieldListMapCopy.containsKey(key)) {
                        fieldListMapCopy.get(key).add(addToMyWorkFieldName);
                    }
                });

        return entityService.concurrentFindEntities(followFilterCriteria, fieldListMapCopy);
    }

    @Override
    public boolean isAddingToMyWorkSupported() {
        return false;
    }

    @Override
    public boolean isAddingToMyWorkSupported(Entity entityType) {
        return addToMyWorkEntities.contains(entityType);
    }

    @Override
    public boolean isInMyWork(EntityModel entityModel) {
        //TODO: can be optimized
        Collection<EntityModel> myWork = getMyWork();
        myWork = getEntitiesFromUserItems(myWork);
        return EntityUtil.containsEntityModel(myWork, entityModel);
    }

    @Override
    public boolean addToMyWork(EntityModel entityModel) {
        if (isInMyWork(entityModel)) {
            return false;
        }

        EntityModel updateEntityModel = createUpdateEntityModelForFollow(entityModel);
        EntityModel currentUser = userService.getCurrentUser();

        MultiReferenceFieldModel fieldModelFollow = (MultiReferenceFieldModel) updateEntityModel.getValue(FOLLOW_ITEMS_OWNER_FIELD);
        MultiReferenceFieldModel fieldModelNew = (MultiReferenceFieldModel) updateEntityModel.getValue(NEW_ITEMS_OWNER_FIELD);

        if (!EntityUtil.containsEntityModel(fieldModelFollow.getValue(), currentUser)) {
            fieldModelFollow.getValue().add(currentUser);

            if (!EntityUtil.containsEntityModel(fieldModelNew.getValue(), currentUser)) {
                fieldModelNew.getValue().add(currentUser);
            }

            Octane octane = octaneProvider.getOctane();
            String id = entityModel.getValue("id").getValue().toString();
            octane.entityList(Entity.getEntityType(entityModel).getApiEntityName())
                    .at(id)
                    .update()
                    .entity(updateEntityModel)
                    .execute();

            return true;
        }

        return false;
    }

    @Override
    public boolean removeFromMyWork(EntityModel entityModel) {
        EntityModel updateEntityModel = createUpdateEntityModelForFollow(entityModel);
        EntityModel currentUser = userService.getCurrentUser();
        MultiReferenceFieldModel fieldModel = (MultiReferenceFieldModel) updateEntityModel.getValue(FOLLOW_ITEMS_OWNER_FIELD);

        if (EntityUtil.removeEntityModel(fieldModel.getValue(), currentUser)) {
            Octane octane = octaneProvider.getOctane();

            try {
                String id = entityModel.getValue("id").getValue().toString();
                octane.entityList(Entity.getEntityType(entityModel).getApiEntityName())
                        .at(id)
                        .update()
                        .entity(updateEntityModel)
                        .execute();

            } catch (Exception ex) {
                fieldModel.getValue().add(currentUser);
                throw ex;
            }

            return true;
        }

        return false;
    }

    private EntityModel createUpdateEntityModelForFollow(EntityModel entityModel) {
        if (entityModel.getValue(FOLLOW_ITEMS_OWNER_FIELD) == null ||
                entityModel.getValue(FOLLOW_ITEMS_OWNER_FIELD).getValue() == null ||
                entityModel.getValue(NEW_ITEMS_OWNER_FIELD) == null ||
                entityModel.getValue(NEW_ITEMS_OWNER_FIELD).getValue() == null) {

            entityModel = fetchEntityFields(entityModel, FOLLOW_ITEMS_OWNER_FIELD, NEW_ITEMS_OWNER_FIELD);
        }

        EntityModel updateEntityModel = new EntityModel();
        updateEntityModel.setValue(entityModel.getValue(FOLLOW_ITEMS_OWNER_FIELD));
        updateEntityModel.setValue(entityModel.getValue(NEW_ITEMS_OWNER_FIELD));
        return updateEntityModel;
    }

    private EntityModel fetchEntityFields(EntityModel entityModel, String... fields) {
        return entityService.findEntity(
                Entity.getEntityType(entityModel),
                Long.parseLong(entityModel.getValue("id").getValue().toString()),
                new HashSet<>(Arrays.asList(fields))
        );
    }

}