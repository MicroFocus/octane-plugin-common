/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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

package com.hpe.adm.octane.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.TestService;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static com.hpe.adm.octane.services.filtering.Entity.DEFECT;
import static com.hpe.adm.octane.services.filtering.Entity.USER_STORY;

public class EntityServiceITCase extends IntegrationTestBase {

    @Inject
    private EntityService entityService;

    @Inject
    private TestService testService;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Test
    public void testEntityConstants() {

        Collection<EntityModel> entities = entityService.findEntities(Entity.MANUAL_TEST);

        if (entities.size() > 0) {
            EntityModel firstEntity = entities.iterator().next();
            Assert.assertEquals(Entity.getEntityType(firstEntity), Entity.MANUAL_TEST);
        }

        entities = entityService.findEntities(DEFECT);
        if (entities.size() > 0) {
            EntityModel firstEntity = entities.iterator().next();
            Assert.assertEquals(Entity.getEntityType(firstEntity), DEFECT);
        }

        entities = entityService.findEntities(USER_STORY);
        if (entities.size() > 0) {
            EntityModel firstEntity = entities.iterator().next();
            Assert.assertEquals(Entity.getEntityType(firstEntity), USER_STORY);
        }
    }

    @Test
    public void testConnection() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();

        try {
            testService.getOctane(connectionSettings);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }

        try {
            testService.getOctane(connectionSettings);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

}
