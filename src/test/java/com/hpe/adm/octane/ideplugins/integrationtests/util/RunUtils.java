package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RunUtils {

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private EntityUtils entityUtils;

    @Inject
    private ReleaseUtils releaseUtils;

    /**
     * Creates a manual test run
     *
     * @param manualTest
     *            - the Manual Test to which the test run is planned
     * @param name
     *            - the name of the run
     * @return the entityModel of the run
     */
    public EntityModel createManualRun(EntityModel manualTest, String name) {
        EntityModel nativeStatus = entityUtils.getNativeStatus();
        EntityModel manualRun = new EntityModel(Constants.TYPE, Entity.TEST_RUN.getEntityName());
        manualRun.setValue(new StringFieldModel(Constants.NAME, name));
        manualRun.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.MANUAL_TEST_RUN.getEntityName()));
        manualRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, nativeStatus));
        manualRun.setValue(new ReferenceFieldModel(Constants.Release.TYPE, releaseUtils.getRelease()));
        manualRun.setValue(new ReferenceFieldModel(Entity.TEST.getEntityName(), manualTest));
        Entity entity = Entity.getEntityType(manualRun);
        Octane octane = octaneProvider.getOctane();
        try {
            EntityModel run = octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(manualRun)).execute().iterator().next();
            return getRunById(run.getValue("id").getValue().toString());
        } catch (Exception e) {
            manualRun.removeValue(Constants.NATIVE_STATUS);
            EntityModel newNativeStatus = nativeStatus;
            newNativeStatus.removeValue(Constants.ID);
            newNativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_RUN_ID));
            manualRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, newNativeStatus));
            EntityModel run = octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(manualRun)).execute().iterator().next();
            return getRunById(run.getValue("id").getValue().toString());
        }
    }

    /**
     * Creates a test suite run
     *
     * @param testSuite
     *            the test suite which is used to create the test suite run
     * @param testSuiteRunName
     *            the name of the suite run
     * @return the created entityModel of the suite run
     */
    public EntityModel createTestSuiteRun(EntityModel testSuite, String testSuiteRunName) {
        EntityModel nativeStatus = entityUtils.getNativeStatus();
        EntityModel testSuiteRun = new EntityModel(Constants.TYPE, Entity.TEST_RUN.getEntityName());
        testSuiteRun.setValue(new StringFieldModel(Constants.NAME, testSuiteRunName));
        testSuiteRun.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.TEST_SUITE_RUN.getEntityName()));
        testSuiteRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, nativeStatus));
        testSuiteRun.setValue(new ReferenceFieldModel(Constants.Release.TYPE, releaseUtils.getRelease()));
        testSuiteRun.setValue(new ReferenceFieldModel(Entity.TEST.getEntityName(), testSuite));
        Entity entity = Entity.getEntityType(testSuiteRun);
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuiteRun)).execute().iterator().next();
    }

    private EntityModel getRunById(String id) {
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(Entity.MANUAL_TEST_RUN.getApiEntityName()).at(id).get().execute();
    }

}
