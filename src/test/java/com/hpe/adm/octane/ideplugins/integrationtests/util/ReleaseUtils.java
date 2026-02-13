/*
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
 */
package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.fail;

public class ReleaseUtils {

    @Inject
    private OctaneVersionService versionService;

    @Inject
    private OctaneProvider octaneProvider;

    /**
     * Returns the first release in the list of releases
     *
     * @return the entityModel of the release
     */
    public EntityModel getRelease() {
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(Constants.Release.RELEASES).get().execute().iterator().next();
    }

    public void createRelease() {
        EntityModel agileType = new EntityModel();
        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.HIGHER_EQ, OctaneVersion.EVERTON_P3)) {
            agileType.setValue(new StringFieldModel(Constants.ID, Constants.AgileType.NEW_ID));
        } else {
            agileType.setValue(new StringFieldModel(Constants.ID, Constants.AgileType.OLD_ID));
        }
        agileType.setValue(new StringFieldModel(Constants.NAME, Constants.AgileType.NAME));
        agileType.setValue(new StringFieldModel(Constants.TYPE, Constants.AgileType.TYPE));
        agileType.setValue(new StringFieldModel(Constants.LOGICAL_NAME, Constants.AgileType.NEW_ID));

        EntityModel releaseEntityModel = new EntityModel();
        releaseEntityModel.setValue(new StringFieldModel(Constants.NAME, Constants.Release.NAME + UUID.randomUUID().toString()));
        releaseEntityModel.setValue(new StringFieldModel(Constants.TYPE, Constants.Release.TYPE));
        LocalDateTime localDateTImeNow = LocalDateTime.now();
        releaseEntityModel.setValue(new StringFieldModel(Constants.Release.START_DATE, localDateTImeNow.toString() + "Z"));
        releaseEntityModel.setValue(new StringFieldModel(Constants.Release.END_DATE, localDateTImeNow.toString() + "Z"));
        releaseEntityModel.setValue(new ReferenceFieldModel(Constants.AgileType.AGILE_TYPE, agileType));
        try {
            octaneProvider.getOctane().entityList(Entity.RELEASE.getApiEntityName()).create().execute();
        } catch (Exception e) {
            fail(e.toString());
        }
    }


    public void deleteRelease() {
        Octane octane = octaneProvider.getOctane();
        octane.entityList(Constants.Release.RELEASES)
                .delete()
                .query(Query.statement(Constants.ID, QueryMethod.EqualTo, getRelease().getValue(Constants.ID).getValue().toString()).build())
                .execute();
    }
}
