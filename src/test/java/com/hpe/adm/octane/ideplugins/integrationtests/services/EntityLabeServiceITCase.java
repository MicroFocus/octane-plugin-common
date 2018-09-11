package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.EntityLabelService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class EntityLabeServiceITCase extends IntegrationTestBase {

    @Inject
    private EntityLabelService entityLabelService;

    private String[] entityTypes = new String[]{"defect",
            "story",
            "quality_story",
            "feature",
            "epic",
            "task",
            "test_manual",
            "gherkin_test",
            "test_suite",
            "run_manual",
            "run_suite",
            "test_automated",
            "comment",
            "requirement"};

    @Test
    public void testGetEntityLabelDetails() {
        Map<String, EntityModel> entityLabelMap = entityLabelService.getEntityLabelDetails();
        boolean areEntityTypesCovered = Arrays.stream(entityTypes).allMatch(e -> entityLabelMap.get(e) != null);
        Assert.assertTrue(areEntityTypesCovered);
    }
}