package com.hpe.adm.octane.ideplugins.integrationtests.services;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

public class RequirementsITCase extends IntegrationTestBase {

    @Test
    public void createRequirementTest() {
        if (isNewerOctane()) {
            EntityModel requirementFolder = createRequirementFolder("folder " + UUID.randomUUID());
            EntityModel requirement = createRequirement("requirement " + UUID.randomUUID().toString(), requirementFolder);
            EntityModel createdRequirement = findRequirementById(Long.parseLong(requirement.getValue("id").getValue().toString()));
            assert (Long.parseLong(requirement.getValue("id").getValue().toString()) == Long.parseLong(createdRequirement.getValue("id").getValue().toString()));
        }
    }

    @Test
    public void searchRequirementTest() {
        if (isNewerOctane()) {
            EntityModel requirementFolder = createRequirementFolder("folder" + UUID.randomUUID());
            EntityModel entityModel = createRequirement("requirement " + UUID.randomUUID().toString(), requirementFolder);
            String descriptionText = UUID.randomUUID().toString();
            setDescription(entityModel, descriptionText);
            EntityModel createdRequirement = findRequirementById(Long.parseLong(entityModel.getValue("id").getValue().toString()));
            try {
                Thread.sleep(30000);//--wait until the elastic search is updated with the entities
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert compareEntities(createdRequirement, search("name", createdRequirement.getValue("name").getValue().toString()));
            assert compareEntities(createdRequirement, search("id", createdRequirement.getValue("id").getValue().toString()));
            assert compareEntities(createdRequirement, search("description", descriptionText));
        }
    }
}
