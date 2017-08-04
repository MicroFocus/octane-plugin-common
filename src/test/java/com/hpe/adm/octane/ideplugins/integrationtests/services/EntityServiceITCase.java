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

package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.integrationtests.util.User;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkSpace;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.TestService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
@WorkSpace(clean = false)
@User(create = false, firstName = "",lastName = "")
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

        entities = entityService.findEntities(Entity.DEFECT);
        if (entities.size() > 0) {
            EntityModel firstEntity = entities.iterator().next();
            Assert.assertEquals(Entity.getEntityType(firstEntity), Entity.DEFECT);
        }

        entities = entityService.findEntities(Entity.USER_STORY);
        if (entities.size() > 0) {
            EntityModel firstEntity = entities.iterator().next();
            Assert.assertEquals(Entity.getEntityType(firstEntity), Entity.USER_STORY);
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
