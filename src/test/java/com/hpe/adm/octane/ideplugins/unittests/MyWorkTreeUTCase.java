package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.integrationtests.util.Entities;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkSpace;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WorkSpace(clean = false)
public class MyWorkTreeUTCase extends IntegrationTestBase {


    public List<EntityModel> testAddEntities() {
        List<EntityModel> entities = new ArrayList<>();
        entities.add(createEntity(Entity.DEFECT));

        entities.add(createEntity(Entity.QUALITY_STORY));

        entities.add(createEntity(Entity.MANUAL_TEST));
        entities.add(createEntity(Entity.GHERKIN_TEST));

        EntityModel manualTest = createEntity(Entity.MANUAL_TEST);
        entities.add(manualTest);
        entities.add(createManualRun(manualTest, "manual test 1 " + UUID.randomUUID()));

        EntityModel userStoryWithTask = createEntity(Entity.USER_STORY);
        entities.add(userStoryWithTask);
        entities.add(createTask(userStoryWithTask, "task 1" + UUID.randomUUID()));

        entities.add(createTestSuite("suite 1" + UUID.randomUUID()));

        EntityModel testSuiteRun = createTestSuite("test suite 3" + UUID.randomUUID());
        entities.add(testSuiteRun);
        entities.add(createTestSuiteRun(testSuiteRun, "test suite run 1" + UUID.randomUUID()));

        entities.add(createAutomatedTest("automated test 1" + UUID.randomUUID()));


        return entities;
    }


    private List<EntityModel> addEntitiesToMyWork(List<EntityModel> entities) {
        List<EntityModel> entityModels = new ArrayList<>();
        for (EntityModel entityModel : entities) {
            if (entityModel.getValue("type").getValue().equals("test_automated") || entityModel.getValue("type").getValue().equals("test_suite")) {
                continue;
            }
            addToMyWork(entityModel);
            entityModels.add(entityModel);
        }
        return entityModels;
    }

    @Test
    public void setUpMyWorkTree() {
        List<EntityModel> entitiesForMyWork = testAddEntities();
        List<EntityModel> entities = testAddEntities();
        List<EntityModel> entityModelsInMyWork = addEntitiesToMyWork(entitiesForMyWork);
        List<EntityModel> workItems = getMyWorkItems();

        assert entityModelsInMyWork.size() == workItems.size();

        boolean expectedWorkItems = false;
        int rounds = 0;
        for (EntityModel entityModel : workItems) {
            rounds++;
            ReferenceFieldModel subField = null;
            if (entityModel.getValue("entity_type").getValue().toString().equals("work_item")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_work_item");
            }
            if (entityModel.getValue("entity_type").getValue().toString().equals("test")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_test");
            }
            if (entityModel.getValue("entity_type").getValue().toString().equals("run")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_run");
            }
            if (entityModel.getValue("entity_type").getValue().toString().equals("task")) {
                subField = (ReferenceFieldModel) entityModel.getValue("my_follow_items_task");
            }
            for (EntityModel entityModel1 : entityModelsInMyWork) {
                if (subField.getValue().getValue("id").getValue().toString().equals(entityModel1.getValue("id").getValue().toString())) {
                    expectedWorkItems = true;
                    break;
                }
            }
            if (!expectedWorkItems) {
                break;
            }
            if (rounds == workItems.size()) {
                break;
            }
            expectedWorkItems = false;
        }
        assert expectedWorkItems;
    }


}
