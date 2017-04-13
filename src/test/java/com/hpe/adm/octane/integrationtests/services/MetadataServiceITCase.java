package com.hpe.adm.octane.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.octane.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.services.MetadataService;
import org.junit.Ignore;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class MetadataServiceITCase extends IntegrationTestBase {

    @Inject
    MetadataService metadataService;

    @Test
    @Ignore
    public void testGetAllFormLayoutsForEntityTypes() {
        //TODO: osavencu: make it useful or delete it
        try {
            metadataService.getFormLayoutForAllEntityTypes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}

