package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.integrationtests.util.Entities;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkSpace;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Test;

@WorkSpace(clean = false)
public class MyWorkTreeUTCase extends IntegrationTestBase {

    @Test
    public void testAddEntities() {

        createEntity(Entity.DEFECT);
        addToMyWork(createEntity(Entity.DEFECT));

        createEntity(Entity.QUALITY_STORY);
        addToMyWork(createEntity(Entity.QUALITY_STORY));

        createEntity(Entity.MANUAL_TEST);

        createEntity(Entity.GHERKIN_TEST);
        addToMyWork(createEntity(Entity.GHERKIN_TEST));

        addToMyWork(createManualRun(createEntity(Entity.MANUAL_TEST), "a2"));
        createManualRun(createEntity(Entity.MANUAL_TEST), "a3");

        addToMyWork(createTask(createEntity(Entity.USER_STORY), "task1"));
        createTask(createEntity(Entity.USER_STORY), "task2");

        createTestSuite("suite 1");
        createTestSuite("suite 2");

        createTestSuiteRun(createTestSuite("test suite 3"), "test suite run 1");
        addToMyWork(createTestSuiteRun(createTestSuite("test suite 3"), "test suite run 2"));

        createAutomatedTest("automated test 1");
        createAutomatedTest("automated test 2");
    }


}
