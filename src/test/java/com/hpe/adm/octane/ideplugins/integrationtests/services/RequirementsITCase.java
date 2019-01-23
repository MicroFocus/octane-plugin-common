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
