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
            Thread.sleep(60000);//--wait until the elastic search is updated with the entities
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
    public void testSearchWithBadParameters() {
        int badId = 0;
        //bad id
        assert testSearch(String.valueOf(badId)) == null;
        //bad name or description
        assert testSearch(UUID.randomUUID().toString()) == null;
    }

}
