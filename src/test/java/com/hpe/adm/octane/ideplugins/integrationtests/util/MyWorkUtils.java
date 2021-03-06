/*
 * Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;

import java.util.ArrayList;
import java.util.List;

public class MyWorkUtils {

    @Inject
    private MyWorkService myWorkService;

    /**
     * Retrieves the items in My Work
     *
     * @return a list of entities representing the items in my work
     */
    public List<EntityModel> getMyWorkItems() {
        return new ArrayList<>(myWorkService.getMyWork());
    }

    /**
     * Adds an entity into the my work section
     *
     * @param entityModel - the entity to be added
     */
    public void addToMyWork(EntityModel entityModel) {
        try {
            myWorkService.addToMyWork(entityModel);
        } catch (OctaneException e) {
            e.printStackTrace();
        }
    }

    public void dismissFromMyWork(EntityModel entityModel) {
        try {
            myWorkService.removeFromMyWork(entityModel);
        } catch (OctaneException e) {
            e.printStackTrace();
        }
    }
}
