package com.hpe.adm.octane.ideplugins.integrationtests.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

public class SearchFunctionalityITCase extends IntegrationTestBase {

    private String randomUUID = String.valueOf(UUID.randomUUID().toString());

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
        Assert.assertNotNull(search("name", "task 2 with \" double quotes \""));
    }
    
    @Test
    public void testSearchWithGoodBackslash() {
        Assert.assertNotNull(search("name", "\\"));        
    }

}
