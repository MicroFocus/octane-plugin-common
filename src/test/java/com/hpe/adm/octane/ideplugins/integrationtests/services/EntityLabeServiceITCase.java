package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.EntityLabelService;
import org.junit.Test;

import java.util.Map;

public class EntityLabeServiceITCase extends IntegrationTestBase {

    @Inject
    private EntityLabelService entityLabelService;

    @Test
    public void testGetEntityLabelDetails() {
        Map<String, EntityModel> entityLabelList = entityLabelService.getEntityLabelDetails();
        System.out.println(entityLabelList.size());
    }
}
