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


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.junit.Test;

import java.util.UUID;

public class RequirementsITCase extends IntegrationTestBase {

    @Test
    public void testCreateRequirement() {
        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.LOWER_EQ, OctaneVersion.EVERTON_P3)) {
            EntityModel requirementFolder = createRequirementFolder("folder " + UUID.randomUUID());
            EntityModel requirement = createRequirement("requirement " + UUID.randomUUID().toString(), requirementFolder);
            EntityModel createdRequirement = findRequirementById(Long.parseLong(requirement.getValue("id").getValue().toString()));
            assert (Long.parseLong(requirement.getValue("id").getValue().toString()) == Long.parseLong(createdRequirement.getValue("id").getValue().toString()));
        }
    }

    @Test
    public void testSearchRequirement() {
        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.LOWER_EQ, OctaneVersion.EVERTON_P3)) {
            EntityModel requirementFolder = createRequirementFolder("folder" + UUID.randomUUID());
            EntityModel entityModel = createRequirement("requirement " + UUID.randomUUID().toString(), requirementFolder);
            String descriptionText = UUID.randomUUID().toString();
            setDescription(entityModel, descriptionText);
            EntityModel createdRequirement = findRequirementById(Long.parseLong(entityModel.getValue("id").getValue().toString()));
            try {
                Thread.sleep(40000);//--wait until the elastic search is updated with the entities
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert compareEntities(createdRequirement, search("name", createdRequirement.getValue("name").getValue().toString()));
            assert compareEntities(createdRequirement, search("id", createdRequirement.getValue("id").getValue().toString()));
            assert compareEntities(createdRequirement, search("description", descriptionText));
        }
    }
}
