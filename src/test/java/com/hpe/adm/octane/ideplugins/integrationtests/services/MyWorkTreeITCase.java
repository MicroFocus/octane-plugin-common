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
package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.util.*;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MyWorkTreeITCase {

    @Inject
    private UserUtils userUtils;

    @Inject
    private EntityUtils entityUtils;

    @Inject
    private RunUtils runUtils;

    @Inject
    private TaskUtils taskUtils;

    @Inject
    private TestUtils testUtils;

    @Inject
    private MyWorkUtils myWorkUtils;

    private List<EntityModel> testAddEntities() {
        List<EntityModel> entities = new ArrayList<>();
        entities.add(entityUtils.createEntity(Entity.DEFECT));

        entities.add(entityUtils.createEntity(Entity.QUALITY_STORY));

        entities.add(entityUtils.createEntity(Entity.MANUAL_TEST));
        entities.add(entityUtils.createEntity(Entity.GHERKIN_TEST));

        EntityModel manualTest = entityUtils.createEntity(Entity.MANUAL_TEST);
        entities.add(manualTest);
        entities.add(runUtils.createManualRun(manualTest, "manual test 1 " + UUID.randomUUID()));

        EntityModel userStoryWithTask = entityUtils.createEntity(Entity.USER_STORY);
        entities.add(userStoryWithTask);
        entities.add(taskUtils.createTask(userStoryWithTask, "task 1" + UUID.randomUUID()));

        entities.add(testUtils.createTestSuite("suite 1" + UUID.randomUUID()));

        EntityModel testSuiteRun = testUtils.createTestSuite("test suite 3" + UUID.randomUUID());
        entities.add(testSuiteRun);
        entities.add(runUtils.createTestSuiteRun(testSuiteRun, "test suite run 1" + UUID.randomUUID()));

        entities.add(testUtils.createAutomatedTest("automated test 1" + UUID.randomUUID()));


        return entities;
    }


    private List<EntityModel> addEntitiesToMyWork(List<EntityModel> entities) {
        List<EntityModel> entityModels = new ArrayList<>();
        for (EntityModel entityModel : entities) {
            if (entityModel.getValue("type").getValue().equals("test_automated") || entityModel.getValue("type").getValue().equals("test_suite")) {
                continue;
            }
            myWorkUtils.addToMyWork(entityModel);
            entityModels.add(entityModel);
        }
        return entityModels;
    }

    /**
     * This method is generating 3 entities of each entity type
     * Out of this 3 entities 1 will be added to my work section using the method addToMyWork
     * Another entity will be added to my work section by updating their owner sections
     * the last entity will be left as a backlog item
     * The test checks whether the methods add the items to my work and verifies if they are present
     */
    @Test
    @Ignore // TODO
    public void testSetUpMyWorkTree() {
        testAddEntities();
        List<EntityModel> entitiesForMyWork = testAddEntities();
        List<EntityModel> entitiesWithOwners = addToMyWorkByOwnerField(testAddEntities());
        List<EntityModel> entityModelsInMyWork = addEntitiesToMyWork(entitiesForMyWork);
        List<EntityModel> workItems = myWorkUtils.getMyWorkItems();

        List<EntityModel> myWorkEntities = Stream.concat(entityModelsInMyWork.stream(), entitiesWithOwners.stream()).collect(Collectors.toList());

        assert myWorkEntities.size() == workItems.size();

        boolean expectedWorkItems = false;
        int count = 0;
        int itemCount = workItems.size();
        for (EntityModel entityModel : workItems) {
            count++;
            ReferenceFieldModel subField = null;
            String entityType = entityModel.getValue("entity_type").getValue().toString();
            if (entityType.equals("work_item")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_work_item");
            }
            if (entityType.equals("test")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_test");
            }
            if (entityType.equals("run")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_run");
            }
            if (entityType.equals("task")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_task");
            }
            for (EntityModel entityModel1 : myWorkEntities) {
                if (subField.getValue().getValue("id").getValue().toString().equals(entityModel1.getValue("id").getValue().toString())) {
                    expectedWorkItems = true;
                    break;
                }
            }
            if (!expectedWorkItems) {
                break;
            }
            if (count == itemCount) {
                break;
            }
            expectedWorkItems = false;
        }
        assert expectedWorkItems;
    }


    public List<EntityModel> addToMyWorkByOwnerField(List<EntityModel> backlogItems) {
        EntityModel currentUser = userUtils.getCurrentUser();
        List<EntityModel> entityModels = new ArrayList<>();
        for (EntityModel entityModel : backlogItems) {
            if (entityModel.getValue("type").getValue().equals("test_automated") || entityModel.getValue("type").getValue().equals("test_suite") || entityModel.getValue("type").getValue().toString().contains("run")) {
                continue;
            }
            userUtils.setOwner(entityModel, currentUser);
            entityModels.add(entityModel);
        }
        return entityModels;
    }


}


