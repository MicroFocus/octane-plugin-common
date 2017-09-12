package com.hpe.adm.octane.ideplugins.unittests;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFunctionalityUTCase extends IntegrationTestBase {


    private List<EntityModel> createSearchableEntities() {
        List<EntityModel> entities = new ArrayList<>();
        entities.add(createEntity(Entity.DEFECT));
        entities.add(createEntity(Entity.MANUAL_TEST));
        entities.add(createEntity(Entity.GHERKIN_TEST));
        EntityModel userStoryWithTask = createEntity(Entity.USER_STORY);
        entities.add(userStoryWithTask);
        createTask(userStoryWithTask, "task 1" + UUID.randomUUID());
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
            Thread.sleep(30000);//--wait until the elastic search is updated with the entities
        } catch (Exception e) {
            e.printStackTrace();
        }
        EntityModel retrievedEntity = null;
        for (EntityModel entityModel : entityModels) {
            //search by name
            retrievedEntity = testSearch("name", entityModel.getValue("name").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
            //search by id
            retrievedEntity = testSearch("id", entityModel.getValue("id").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
            //search by description
            retrievedEntity = testSearch("description", entityModel.getValue("description").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
            retrievedEntity = null;
        }
    }

    public void testSearchWithBadID() {
        int badId = 19000;
        if (testSearch("id", String.valueOf(badId)) == null)
            assert true;
        else
            assert false;
    }

    public void testSearchWithBadName() {
        if (testSearch("name", String.valueOf(UUID.randomUUID().toString())) == null)
            assert true;
        else
            assert false;
    }

    public void testSearchWithBadDescription() {
        if (testSearch("description", String.valueOf(UUID.randomUUID().toString())) == null)
            assert true;
        else
            assert false;
    }
}
