/*
 * Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
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
package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.EntityLabelService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class EntityLabelServiceITCase extends IntegrationTestBase {

    @Inject
    private EntityLabelService entityLabelService;

    private Entity[] entityTypes = new Entity[]{
            Entity.DEFECT,
            Entity.USER_STORY,
            Entity.QUALITY_STORY,
            Entity.FEATURE,
            Entity.EPIC,
            Entity.TASK,
            Entity.MANUAL_TEST,
            Entity.GHERKIN_TEST,
            Entity.TEST_SUITE,
            Entity.MANUAL_TEST_RUN,
            Entity.TEST_SUITE_RUN,
            Entity.AUTOMATED_TEST,
            Entity.COMMENT,
            Entity.REQUIREMENT
    };

    @Test
    public void testGetEntityLabelDetails() {
        Map<Entity, EntityModel> entityLabelMap = entityLabelService.getEntityLabelDetails();
        boolean areEntityTypesCovered = Arrays.stream(entityTypes).allMatch(e -> entityLabelMap.get(e) != null);
        Assert.assertTrue(areEntityTypesCovered);
    }

}