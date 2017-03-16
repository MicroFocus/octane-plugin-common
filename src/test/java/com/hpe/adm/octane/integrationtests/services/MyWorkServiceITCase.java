package com.hpe.adm.octane.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.mywork.MyWorkService;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

public class MyWorkServiceITCase extends IntegrationTestBase {

    @Inject
    private MyWorkService myWorkService;

    @Test
    public void testAddToMyWork() {
        EntityModel entityModel = entityGenerator.createEntityModel(Entity.USER_STORY);
        entityGenerator.deleteEntityModel(entityModel);
    }

    @Test
    public void testGetMyWork() {

        Collection<EntityModel> entities = myWorkService.getMyWork();

        System.out.println("Entities size: " + entities.size());

        String entitiesString = entities
                .stream()
                .map(entityModel -> entityModel.getValue("name").getValue().toString())
                .collect(Collectors.joining(","));

        System.out.println("Entities: " + entitiesString);

    }

}
