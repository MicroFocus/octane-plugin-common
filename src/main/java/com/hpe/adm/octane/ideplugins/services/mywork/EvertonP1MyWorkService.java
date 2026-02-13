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
package com.hpe.adm.octane.ideplugins.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.LongFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.UserService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.*;
import java.util.stream.Collectors;

import static com.hpe.adm.octane.ideplugins.services.filtering.Entity.MANUAL_TEST_RUN;
import static com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil.*;

class EvertonP1MyWorkService extends EvertonP2MyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private UserService userService;

    @Inject
    private DynamoMyWorkFilterCriteria dynamoMyWorkFilterCriteria;

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {

        Map<Entity, Collection<EntityModel>> resultMap;
        Map<Entity, Query.QueryBuilder> filterCriteria = dynamoMyWorkFilterCriteria.getStaticFilterCriteria();

        //For Everton P1 change the way MANUAL_TEST_RUNs are queried
        Query.QueryBuilder runParentSuiteQuery =
                Query.statement("parent_suite", QueryMethod.EqualTo,
                                Query.statement("run_by", QueryMethod.EqualTo, null))
                        .and(Query.not("parent_suite", QueryMethod.EqualTo, null));

        Query.QueryBuilder runParentNullQuery =
                Query.statement("parent_suite", QueryMethod.EqualTo, null);

        Query.QueryBuilder subtypeQuery = MANUAL_TEST_RUN.createMatchSubtypeQueryBuilder();

        Query.QueryBuilder statusQuery = createNativeStatusQuery("list_node.run_native_status.blocked", "list_node.run_native_status.not_completed", "list_node.run_native_status.planned");

        Query.QueryBuilder userQuery = createUserQuery("run_by", userService.getCurrentUserId());

        runParentSuiteQuery = Query.QueryBuilder.parenthesis(runParentSuiteQuery);

        Query.QueryBuilder manualTestRunQuery =
                userQuery
                        .and(subtypeQuery)
                        .and(statusQuery)
                        .and(Query.QueryBuilder.parenthesis(runParentNullQuery.or(runParentSuiteQuery)));

        filterCriteria.put(MANUAL_TEST_RUN, manualTestRunQuery);

        //Get entities by filterCriteria
        resultMap = entityService.concurrentFindEntities(filterCriteria, fieldListMap);

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

        String addToMyWorkFieldName = "user_item";

        Map<Entity, Query.QueryBuilder> followFilterCriteria = new HashMap<>();

        dynamoMyWorkFilterCriteria.getStaticFilterCriteria()
                .keySet()
                .stream()
                .filter(this::isAddingToMyWorkSupported)
                .forEach(key -> {
                    Query.QueryBuilder qb;
                    if (key.isSubtype()) {
                        qb = key.createMatchSubtypeQueryBuilder().and(createUserItemQueryBuilder());
                    } else {
                        qb = createUserItemQueryBuilder();
                    }
                    followFilterCriteria.put(key, qb);
                    if (fieldListMapCopy != null && fieldListMapCopy.containsKey(key)) {
                        fieldListMapCopy.get(key).add(addToMyWorkFieldName);
                    }
                });

        return entityService.concurrentFindEntities(followFilterCriteria, fieldListMapCopy);
    }

    @Override
    protected EntityModel createNewUserItem(EntityModel wrappedEntityModel) {
        EntityModel newUserItem = new EntityModel();

        //origin==1 means it was added manually, not because the entity matches the business rule
        newUserItem.setValue(new LongFieldModel("origin", 1L));

        //Is clear, was added manually (origin==1)
        newUserItem.setValue(new ReferenceFieldModel("reason", null));

        String entityType = getEntityTypeName(Entity.getEntityType(wrappedEntityModel));

        newUserItem.setValue(new StringFieldModel("entity_type", entityType));
        newUserItem.setValue(new ReferenceFieldModel("user", userService.getCurrentUser()));

        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(wrappedEntityModel));
        newUserItem.setValue(new ReferenceFieldModel(followField, wrappedEntityModel));

        return newUserItem;
    }

    private Query.QueryBuilder createUserItemQueryBuilder() {
        return Query.statement("user_item", QueryMethod.EqualTo,
                Query.statement("user", QueryMethod.EqualTo,
                        Query.statement("id", QueryMethod.EqualTo, userService.getCurrentUserId())
                )
        );
    }

}