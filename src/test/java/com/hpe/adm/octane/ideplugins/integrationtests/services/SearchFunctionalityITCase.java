package com.hpe.adm.octane.ideplugins.integrationtests.services;


import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFunctionalityITCase extends IntegrationTestBase {
    @Inject
    private EntitySearchService searchService;


    private EntityModel testSearch(String searchField, String query) {
        List<EntityModel> entityModels = searchService.searchGlobal(query, 1000, Entity.WORK_ITEM, Entity.MANUAL_TEST, Entity.GHERKIN_TEST, Entity.TASK).stream().collect(Collectors.toList());

        for (EntityModel entityModel : entityModels) {
            if (removeTags(entityModel.getValue(searchField).getValue().toString()).contains(query)) {
                return entityModel;
            }
        }
        return null;
    }

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

    private String removeTags(String s) {
        String result = null;
        if (s.contains("<em>")) {
            result = s.replaceAll("<em>", "");
            result = result.replaceAll("</em>", "");
            return result;
        }
        return s;
    }

    private boolean compareEntities(EntityModel entity1, EntityModel entity2) {
        if (entity1.getValue("id").getValue().toString().equals(entity2.getValue("id").getValue().toString())) {
            return true;
        }
        return false;
    }

    @Test
    @Ignore
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

    @Test
    public void testSearchWithBadID() {
        int badId = 19000;
        if (testSearch("id", String.valueOf(badId)) == null)
            assert true;
        else
            assert false;

    }

    @Test
    public void testSearchWithBadName() {
        if (testSearch("name", String.valueOf(UUID.randomUUID().toString())) == null)
            assert true;
        else
            assert false;
    }

    @Test
    public void testSearchWithBadDescription() {
        if (testSearch("description", String.valueOf(UUID.randomUUID().toString())) == null)
            assert true;
        else
            assert false;
    }
}
