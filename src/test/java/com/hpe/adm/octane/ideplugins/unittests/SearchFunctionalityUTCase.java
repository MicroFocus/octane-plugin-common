package com.hpe.adm.octane.ideplugins.unittests;


import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

public class SearchFunctionalityUTCase extends IntegrationTestBase {
    @Inject
    private EntitySearchService searchService;


    private EntityModel testSearch(String query) {
        return searchService.searchGlobal(query, 1, Entity.WORK_ITEM).stream().collect(Collectors.toList()).iterator().next();
    }

    private List<EntityModel> createSearchableEntities() {
        List<EntityModel> entities = new ArrayList<>();
        entities.add(createEntity(Entity.DEFECT));
        entities.add(createEntity(Entity.MANUAL_TEST));
        entities.add(createEntity(Entity.GHERKIN_TEST));
        EntityModel userStoryWithTask = createEntity(Entity.USER_STORY);
        entities.add(userStoryWithTask);
        entities.add(createTask(userStoryWithTask, "task 1" + UUID.randomUUID()));
        return entities;
    }

    private void setDescription(List<EntityModel> entities) {
        int descriptionCount = 0;
        for (EntityModel entityModel : entities) {
            setDescription(entityModel, String.valueOf(descriptionCount));
            entityModel.setValue(new StringFieldModel("description", String.valueOf(descriptionCount++)));
        }
    }

    private boolean compareEntities(EntityModel entity1, EntityModel entity2) {
        if (entity1.getValue("id").getValue().toString().equals(entity2.getValue("id").getValue().toString())) {
            return true;
        }
        return false;
    }

    @Test
    public void testSearchEntities() {

        List<EntityModel> entityModels = createSearchableEntities();

        setDescription(entityModels);
        try {
            Thread.sleep(40000);//--wait until the elastic search is updated with the entities
        } catch (Exception e) {
            e.printStackTrace();
        }
        int descriptionCount = 0;
        for (EntityModel entityModel : entityModels) {
            //search by name
            EntityModel retrievedEntity = testSearch(entityModel.getValue("name").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
            //search by id
            retrievedEntity = testSearch(entityModel.getValue("id").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
            //search by description
            retrievedEntity = testSearch(entityModel.getValue("description").getValue().toString());
            assert compareEntities(entityModel, retrievedEntity);
        }
    }

    @Test
    public void testSearchWithBadID() {
        int badId = 19000;
        //bad id
        try {
            testSearch(String.valueOf(badId));
            assert false;
        } catch (NoSuchElementException e) {
            assert true;
        }
    }
     @Test
    public void testSearchWithBadNameOrDescription(){
        //bad name or description
        try {
            testSearch(String.valueOf(UUID.randomUUID().toString()));
            assert false;
        }catch(NoSuchElementException e){
            assert true;
        }
    }

}
