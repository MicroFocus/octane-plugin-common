package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.Collections;

public class TestUtils {

    @Inject
    private OctaneProvider octaneProvider;

    /**
     * Creates a Test Suite
     *
     * @param name
     *            the name of the test suite
     * @return the entityModel of the test suite, @null if not created
     */
    public EntityModel createTestSuite(String name) {
        EntityModel testSuite = new EntityModel(Constants.TYPE, Entity.TEST.getSubtypeName());
        testSuite.setValue(new StringFieldModel(Constants.NAME, name));
        testSuite.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.TEST_SUITE.getSubtypeName()));
        Entity entity = Entity.getEntityType(testSuite);
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuite)).execute().iterator().next();
    }

    /**
     * Creates an automated test
     *
     * @param testName
     *            - the name of the new automated test
     * @return the newly created automated test entityModel
     */
    public EntityModel createAutomatedTest(String testName) {
        EntityModel automatedTest = new EntityModel(Constants.TYPE, Entity.TEST.getEntityName());
        automatedTest.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.AUTOMATED_TEST.getEntityName()));
        automatedTest.setValue(new StringFieldModel(Constants.NAME, testName));
        Entity entity = Entity.getEntityType(automatedTest);
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(automatedTest)).execute().iterator().next();
    }
}
