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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.TestServiceModule;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityUtils;
import com.hpe.adm.octane.ideplugins.integrationtests.util.TaskUtils;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFunctionalityITCase {

    @Inject
    private EntityUtils entityUtils;

    @Inject
    private TaskUtils taskUtils;

    private static final String randomUUID = UUID.randomUUID().toString();
    private long timeoutCount = 15;
    private long timeout = 5000; // 5 seconds

    @Before
    public void setUp() {
        ServiceModule serviceModule = TestServiceModule.getServiceModule();
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
    }

    private List<EntityModel> createSearchableEntities() {
        List<EntityModel> entities = new ArrayList<>();
        EntityModel userStoryWithTask2 = entityUtils.createEntity(Entity.USER_STORY);
        entities.add(userStoryWithTask2);
        taskUtils.createTask(userStoryWithTask2, "task 2 with \" double quotes \"" + randomUUID);
        taskUtils.createTask(userStoryWithTask2, "task 2 with \\ 2 backslashes \\" + randomUUID);
        entities.add(entityUtils.createEntity(Entity.DEFECT));
        entities.add(entityUtils.createEntity(Entity.MANUAL_TEST));
        entities.add(entityUtils.createEntity(Entity.GHERKIN_TEST));

        return Stream.concat(entities.stream(), taskUtils.getTasks().stream()).collect(Collectors.toList());
    }

    private void setDescription(List<EntityModel> entities) {
        int descriptionCount = 0;
        for (EntityModel entityModel : entities) {
            entityUtils.setDescription(entityModel, String.valueOf(descriptionCount));
            entityModel.setValue(new StringFieldModel("description", String.valueOf(descriptionCount++)));
        }
    }

    @Test
    public void testSearchEntities() {
        List<EntityModel> entityModels = createSearchableEntities();
        setDescription(entityModels);

        int retryCount = 0;
        while(retryCount < timeoutCount) {
            boolean found = true;
            for (EntityModel entityModel : entityModels) {
                // search by name
                if(entityUtils.search("name", entityModel.getValue("name").getValue().toString()) == null
                    || entityUtils.search("id", entityModel.getValue("id").getValue().toString()) == null
                    || entityUtils.search("description", entityModel.getValue("description").getValue().toString()) == null) {
                    found = false;
                    break;
                }
            }
            if(found) {
                assert true;
                return;
            } else {
                try {
                    Thread.sleep(timeout);
                    retryCount++;
                } catch (Exception e) {
                    Assert.fail("Failed sleep while waiting for elasticsearch: " + e.getMessage());
                }
            }
        }
        Assert.fail("Failed to test search entities...");


    }

    @Test
    public void testSearchWithBadID() {
        int badId = 19000;
        Assert.assertNull(entityUtils.search("id", String.valueOf(badId)));
    }

    @Test
    public void testSearchWithBadName() {
        Assert.assertNull(entityUtils.search("name", String.valueOf(UUID.randomUUID().toString())));
    }

    @Test
    public void testSearchWithBadDescription() {
        Assert.assertNull(entityUtils.search("description", String.valueOf(UUID.randomUUID().toString())));
    }

    @Test
    public void testSearchWithGoodDoubleQuotes() {
        Assert.assertNotNull(entityUtils.search("name", "task 2 with \" double quotes \"" + randomUUID));
    }
    
    @Test
    public void testSearchWithGoodBackslash() {
        Assert.assertNotNull(entityUtils.search("name", "\\"));
    }
    
    @Test
    public void testCompareIfReturnedEntitiesMatch() {
        EntityModel myUS = entityUtils.createEntity(Entity.USER_STORY);
        EntityModel myTask = taskUtils.createTask(myUS, "something searchable" + randomUUID);
        try {
            Thread.sleep(40000);// --wait until the elastic search is updated with the entities
        } catch (Exception e) {
            e.printStackTrace();
        }

        EntityModel retrievedEntity = entityUtils.search("name", "something searchable" + randomUUID);
        Assert.assertTrue(entityUtils.compareEntities(retrievedEntity, myTask));
    }

}
