package com.hpe.adm.octane.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.octane.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.services.MetadataService;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class MetadataServiceITCase extends IntegrationTestBase {

    @Inject
    MetadataService metadataService;

    @Test
    public void testGetAllFormLayoutsForEntityTypes() {
        try {
            metadataService.getFormLayoutForAllEntityTypes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}

