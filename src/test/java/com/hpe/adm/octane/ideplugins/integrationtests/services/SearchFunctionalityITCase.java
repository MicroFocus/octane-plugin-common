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

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFunctionalityITCase extends IntegrationTestBase {

    private static final String randomUUID = UUID.randomUUID().toString();

    private List<EntityModel> createSearchableEntities() {
        List<EntityModel> entities = new ArrayList<>();
        EntityModel userStoryWithTask2 = createEntity(Entity.USER_STORY);
        entities.add(userStoryWithTask2);
        createTask(userStoryWithTask2, "task 2 with \" double quotes \"" + randomUUID);
        createTask(userStoryWithTask2, "task 2 with \\ 2 backslashes \\" + randomUUID);
        entities.add(createEntity(Entity.DEFECT));
        entities.add(createEntity(Entity.MANUAL_TEST));
        entities.add(createEntity(Entity.GHERKIN_TEST));

        return Stream.concat(entities.stream(), getTasks().stream()).collect(Collectors.toList());
    }

    private void setDescription(List<EntityModel> entities) {
        int descriptionCount = 0;
        for (EntityModel entityModel : entities) {
            setDescription(entityModel, String.valueOf(descriptionCount));
            entityModel.setValue(new StringFieldModel("description", String.valueOf(descriptionCount++)));
        }
    }

    @Test
    public void testSearchEntities() {
        deleteBacklogItems();
        List<EntityModel> entityModels = createSearchableEntities();
        setDescription(entityModels);
        try {
            Thread.sleep(40000);// --wait until the elastic search is updated
                                // with the entities
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (EntityModel entityModel : entityModels) {
            // search by name
            Assert.assertNotNull(search("name", entityModel.getValue("name").getValue().toString()));
            // search by id
            Assert.assertNotNull(search("id", entityModel.getValue("id").getValue().toString()));
            // search by description
            Assert.assertNotNull(search("description", entityModel.getValue("description").getValue().toString()));
        }
    }

    @Test
    public void testSearchWithBadID() {
        int badId = 19000;
        Assert.assertNull(search("id", String.valueOf(badId)));
    }

    @Test
    public void testSearchWithBadName() {
        Assert.assertNull(search("name", String.valueOf(UUID.randomUUID().toString())));
    }

    @Test
    public void testSearchWithBadDescription() {
        Assert.assertNull(search("description", String.valueOf(UUID.randomUUID().toString())));
    }

    @Test
    public void testSearchWithGoodDoubleQuotes() {
        Assert.assertNotNull(search("name", "task 2 with \" double quotes \"" + randomUUID));
    }
    
    @Test
    public void testSearchWithGoodBackslash() {
        Assert.assertNotNull(search("name", "\\"));        
    }
    
    @Test
    public void testCompareIfReturnedEntitiesMatch() {
        EntityModel myUS = createEntity(Entity.USER_STORY);
        EntityModel myTask = createTask(myUS, "something searchable" + randomUUID);
        try {
            Thread.sleep(40000);// --wait until the elastic search is updated with the entities
        } catch (Exception e) {
            e.printStackTrace();
        }

        EntityModel retrievedEntity = search("name", "something searchable" + randomUUID);
        Assert.assertTrue(compareEntities(retrievedEntity, myTask));
    }

}
