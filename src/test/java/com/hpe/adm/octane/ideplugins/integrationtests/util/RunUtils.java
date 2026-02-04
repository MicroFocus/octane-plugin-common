/*******************************************************************************
 * Copyright 2017-2026 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.Collections;

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
     * @param manualTest - the Manual Test to which the test run is planned
     * @param name       - the name of the run
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
     * @param testSuite        the test suite which is used to create the test suite run
     * @param testSuiteRunName the name of the suite run
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
