package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.integrationtests.util.Entities;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkSpace;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Test;

@WorkSpace(clean = true)
@Entities(requiredEntities = {Entity.DEFECT,Entity.DEFECT,Entity.QUALITY_STORY,Entity.QUALITY_STORY,
        Entity.MANUAL_TEST,Entity.MANUAL_TEST,Entity.GHERKIN_TEST,Entity.GHERKIN_TEST})
public class MyWorkTreeUTCase extends IntegrationTestBase {

    @Test
    public void testAddEntities() {

        EntityModel testModel = createEntity(Entity.MANUAL_TEST);
        createManualRun(testModel, "a2");
        createManualRun(testModel, "a3");

        EntityModel userStoryEntityModel = createEntity(Entity.USER_STORY);
        createTask(userStoryEntityModel, "task1");
        createTask(userStoryEntityModel, "task2");

        createTestSuite("suite 1");
        createTestSuite("suite 2");

        createTestSuiteRun(createTestSuite("test suite 3"), "test suite run 1");
        createTestSuiteRun(createTestSuite("test suite 3"), "test suite run 2");

        createAutomatedTest("automated test 1");
        createAutomatedTest("automated test 2");
    }


}
