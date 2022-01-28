/*
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.TestServiceModule;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityUtils;
import com.hpe.adm.octane.ideplugins.integrationtests.util.RequirementUtils;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequirementsITCase {

    @Inject
    private RequirementUtils requirementUtils;

    @Inject
    private OctaneVersionService versionService;

    @Inject
    private EntityUtils entityUtils;

    private List<EntityModel> entityModelList;

    private long timeoutCount = 15;
    private long timeout = 5000; // 5 seconds

    @Before
    public void setUp() {
        ServiceModule serviceModule = TestServiceModule.getServiceModule();
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
        entityModelList = new ArrayList<>();
    }

    @Test
    public void testCreateRequirement() {
        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.LOWER_EQ, OctaneVersion.EVERTON_P3)) {
            EntityModel requirementFolder = requirementUtils.createRequirementFolder("folder " + UUID.randomUUID());
            EntityModel requirement = requirementUtils.createRequirement("requirement " + UUID.randomUUID().toString(), requirementFolder);
            EntityModel createdRequirement = requirementUtils.findRequirementById(Long.parseLong(requirement.getValue("id").getValue().toString()));
            entityModelList.add(requirementFolder);
            entityModelList.add(requirement);
            assert (Long.parseLong(requirement.getValue("id").getValue().toString()) == Long.parseLong(createdRequirement.getValue("id").getValue().toString()));
        }
    }

    @Test
    public void testSearchRequirement() {
        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.LOWER_EQ, OctaneVersion.EVERTON_P3)) {
            EntityModel requirementFolder = requirementUtils.createRequirementFolder("folder" + UUID.randomUUID());
            EntityModel requirement = requirementUtils.createRequirement("requirement " + UUID.randomUUID().toString(), requirementFolder);
            entityModelList.add(requirementFolder);
            entityModelList.add(requirement);
            String descriptionText = UUID.randomUUID().toString();
            entityUtils.setDescription(requirement, descriptionText);
            EntityModel createdRequirement = requirementUtils.findRequirementById(Long.parseLong(requirement.getValue("id").getValue().toString()));

            int retryCount = 0;
            while(retryCount < timeoutCount) {
                boolean searchByName = entityUtils.compareEntities(createdRequirement, entityUtils.search("name", createdRequirement.getValue("name").getValue().toString()));
                boolean searchById = entityUtils.compareEntities(createdRequirement, entityUtils.search("id", createdRequirement.getValue("id").getValue().toString()));
                boolean searchByDescription = entityUtils.compareEntities(createdRequirement, entityUtils.search("description", descriptionText));
                if(searchByDescription && searchById && searchByName) {
                    assert true;
                    return;
                } else {
                    try {
                        Thread.sleep(timeout);
                        retryCount++;
                    } catch (Exception e) {
                        Assert.fail("Failed sleep while waiting for elasticsearch: " + e.getMessage());
                    }
                }
            }

            Assert.fail("Failed to test search requirements...");
        }
    }

    @After
    public void tearDown() {
        entityModelList.forEach(em -> {
            try {
                entityUtils.deleteEntityModel(em);
            } catch (Exception e) {
                Assert.fail("Failed to delete created requirements: " + e.getMessage());
            }
        });
    }
}
