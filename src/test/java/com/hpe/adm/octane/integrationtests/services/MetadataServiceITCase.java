package com.hpe.adm.octane.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.octane.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.services.MetadataService;
import com.hpe.adm.octane.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class MetadataServiceITCase extends IntegrationTestBase {

    @Inject
    MetadataService metadataService;

    @Test
    public void testGetFormLayoutsForEntityType() {
        try {
            Assert.assertNotNull(metadataService.getFormLayoutForSpecificEntityType(Entity.USER_STORY));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}

