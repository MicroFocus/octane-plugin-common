package com.hpe.adm.octane.ideplugins.integrationtests.services;


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


public class SearchFunctionalityITCase extends IntegrationTestBase {

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
            Thread.sleep(40000);//--wait until the elastic search is updated with the entities
        } catch (Exception e) {
            e.printStackTrace();
        }
        EntityModel retrievedEntity;
        for (EntityModel entityModel : entityModels) {
            //search by name
            retrievedEntity = search("name", entityModel.getValue("name").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
            //search by id
            retrievedEntity = search("id", entityModel.getValue("id").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
            //search by description
            retrievedEntity = search("description", entityModel.getValue("description").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
        }
    }

    @Test
    public void testSearchWithBadID() {
        int badId = 19000;
        if (search("id", String.valueOf(badId)) == null)
            assert true;
        else
            assert false;
    }

    @Test
    public void testSearchWithBadName() {
        if (search("name", String.valueOf(UUID.randomUUID().toString())) == null)
            assert true;
        else
            assert false;
    }

    @Test
    public void testSearchWithBadDescription() {
        if (search("description", String.valueOf(UUID.randomUUID().toString())) == null)
            assert true;
        else
            assert false;
    }
}
