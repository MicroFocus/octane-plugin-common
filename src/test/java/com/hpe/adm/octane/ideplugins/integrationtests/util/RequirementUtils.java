/*
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementUtils {

    @Inject
    private OctaneProvider octaneProvider;

    public EntityModel findRequirementById(long id) {
        List<EntityModel> requirements = getRequirements();
        for (EntityModel entityModel : requirements) {
            if (Long.parseLong(entityModel.getValue(Constants.ID).getValue().toString()) == id) {
                return entityModel;
            }
        }
        return null;
    }

    public EntityModel getRequirementsRoot() {
        List<EntityModel> requirements = getRequirements();
        for (EntityModel entityModel : requirements) {
            if (Constants.REQUIREMENT_ROOT.equals(entityModel.getValue(Constants.SUBTYPE).getValue().toString())) {
                return entityModel;
            }
        }
        return null;
    }

    public List<EntityModel> getRequirements() {
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList(Constants.REQUIREMENTS).get().execute());
    }

    public EntityModel createRequirement(String requirementName, EntityModel parent) {
        EntityModel phase = new EntityModel(Constants.TYPE, Constants.PHASE);
        phase.setValue(new StringFieldModel(Constants.ID, Constants.Requirement.ID));
        phase.setValue(new StringFieldModel(Constants.NAME, Constants.Requirement.NAME));
        phase.setValue(new StringFieldModel(Constants.LOGICAL_NAME, Constants.Requirement.LOGICAL_NAME));
        EntityModel requirement = new EntityModel(Constants.TYPE, Entity.REQUIREMENT_BASE_ENTITY.getEntityName());
        requirement.setValue(new StringFieldModel(Constants.NAME, requirementName));
        requirement.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.REQUIREMENT.getEntityName()));
        requirement.setValue(new ReferenceFieldModel(Constants.PARENT, parent));
        requirement.setValue(new ReferenceFieldModel(Constants.PHASE, phase));
        Entity entity = Entity.getEntityType(requirement);
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(requirement)).execute().iterator().next();
    }

    public EntityModel createRequirementFolder(String folderName) {
        EntityModel requirement = new EntityModel(Constants.TYPE, Entity.REQUIREMENT_BASE_ENTITY.getEntityName());
        requirement.setValue(new StringFieldModel(Constants.NAME, folderName));
        requirement.setValue(new StringFieldModel(Constants.SUBTYPE, Constants.Requirement.FOLDER));
        requirement.setValue(new ReferenceFieldModel(Constants.PARENT, getRequirementsRoot()));
        Entity entity = Entity.getEntityType(requirement);
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(requirement)).execute().iterator().next();
    }
}
