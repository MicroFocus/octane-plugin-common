package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.MetadataService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.DefaultEntityFieldsUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MetadataServiceITCase extends IntegrationTestBase {

    @Inject
    MetadataService metadataService;

    @Test
    public void testMethodForVisibleFields(){
        Collection<String> defaultFields = DefaultEntityFieldsUtil.getDefaultFields().get(Entity.USER_STORY);
        Collection<String> returnedFields = metadataService.getVisibleFields(Entity.USER_STORY).stream().map(FieldMetadata::getName).collect(Collectors.toCollection(HashSet::new));
        assert defaultFields.stream().allMatch(returnedFields::contains);
    }

    @Test
    public void testGetFields(){
        Collection<String> defaultFields = DefaultEntityFieldsUtil.getDefaultFields().get(Entity.USER_STORY);
        Collection<String> returnedFields = metadataService.getFields(Entity.USER_STORY).stream().map(FieldMetadata::getName).collect(Collectors.toCollection(HashSet::new));
        assert defaultFields.stream().allMatch(returnedFields::contains);
    }
    
    @Test
    public void testGetVisibleFieldParallel(){   
        Arrays.stream(Entity.values())
            .parallel()
            .forEach(entity -> {
                try {
                    metadataService.getVisibleFields(entity);
                } catch (Exception ex) {
                    Assert.fail("getVisibleFields failed for entity: " + entity + ": " + ex);
                }
            });
    }

}