package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;

import java.util.ArrayList;
import java.util.List;

public class MyWorkUtils {

    @Inject
    private ServiceModule serviceModule;

    /**
     * Retrieves the items in My Work
     *
     * @return a list of entities representing the items in my work
     */
    public List<EntityModel> getMyWorkItems() {
        MyWorkService myWorkService = serviceModule.getMyWorkService();

        return new ArrayList<>(myWorkService.getMyWork());
    }

    /**
     * Adds an entity into the my work section
     *
     * @param entityModel
     *            - the entity to be added
     */
    public void addToMyWork(EntityModel entityModel) {
        MyWorkService myWorkService = serviceModule.getInstance(MyWorkService.class);

        try {
            myWorkService.addToMyWork(entityModel);
        } catch (OctaneException e) {
            e.printStackTrace();
        }
    }
}
