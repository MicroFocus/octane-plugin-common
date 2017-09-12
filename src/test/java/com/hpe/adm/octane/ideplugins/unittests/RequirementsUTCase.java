package com.hpe.adm.octane.ideplugins.unittests;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class RequirementsUTCase extends IntegrationTestBase {

    @Test
    public void createRequirementTest() {
        EntityModel requirementFolder = createRequirementFolder("folder " + UUID.randomUUID());
        EntityModel requirement = createRequirement("requirement " + UUID.randomUUID().toString(),requirementFolder);
        EntityModel createdRequirement = findRequirementById(Long.parseLong(requirement.getValue("id").getValue().toString()));
        List<EntityModel> requirements = getRequirements();

        for (EntityModel entityModel : requirements) {
            if (entityModel.getValue("name").getValue().equals(createdRequirement.getValue("name").getValue())) {
                assert true;
                return;
            }
        }
        assert false;
    }

    @Test
    public void searchRequirementTest() {
        EntityModel requirementFolder = createRequirementFolder("folder" + UUID.randomUUID());
        EntityModel entityModel = createRequirement("requirement " + UUID.randomUUID().toString(),requirementFolder);
        String descriptionText = UUID.randomUUID().toString();
        setDescription(entityModel,descriptionText);
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
