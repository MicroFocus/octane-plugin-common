package com.hpe.adm.octane.ideplugins.unittests;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import org.junit.Test;

import java.util.UUID;

public class RequirementsUTCase extends IntegrationTestBase {

    @Test
    public void createRequirementTest() {
        if (isNewerOctane()) {
            EntityModel requirementFolder = createRequirementFolder("folder " + UUID.randomUUID());
            EntityModel requirement = createRequirement("requirement " + UUID.randomUUID().toString(), requirementFolder);
            EntityModel createdRequirement = findRequirementById(Long.parseLong(requirement.getValue("id").getValue().toString()));
            assert (Long.parseLong(requirement.getValue("id").getValue().toString()) == Long.parseLong(createdRequirement.getValue("id").getValue().toString()));
        }
    }


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
            assert compareEntities(createdRequirement, testSearch("name", createdRequirement.getValue("name").getValue().toString()));
            assert compareEntities(createdRequirement, testSearch("id", createdRequirement.getValue("id").getValue().toString()));
            assert compareEntities(createdRequirement, testSearch("description", descriptionText));
        }
    }
}
