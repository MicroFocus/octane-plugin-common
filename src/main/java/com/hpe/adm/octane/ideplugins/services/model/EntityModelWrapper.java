/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.services.model;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class EntityModelWrapper {

    private EntityModel entityModel;
    private List<FieldModelChangedHandler> changeHandlers = new ArrayList<>();

    public static interface FieldModelChangedHandler {
        public void fieldModelChanged(FieldModel fieldModel);
    }

    public Entity getEntityType() {
        return Entity.getEntityType(entityModel);
    }

    public EntityModelWrapper(EntityModel entityModel) {
        this.entityModel = entityModel;
    }

    public ReadOnlyEntityModel getReadOnlyEntityModel() {
        return new ReadOnlyEntityModel(entityModel.getValues());
    }

    public EntityModel getEntityModel() {
        return entityModel;
    }

    public FieldModel getValue(String key) {
        return entityModel.getValue(key);
    }

    public boolean hasValue(String key) {
        return entityModel.getValue(key) != null && entityModel.getValue(key).getValue() != null;
    }

    public void setValue(FieldModel fieldModel) {
        entityModel.setValue(fieldModel);
        callChangeHandlers(fieldModel);
    }

    public void setValues(Set<FieldModel> values) {
        entityModel.setValues(values);
        values.forEach(fieldModel -> callChangeHandlers(fieldModel));
    }


    private void callChangeHandlers(FieldModel fieldModel) {
        changeHandlers.forEach(handler -> handler.fieldModelChanged(fieldModel));
    }

    public boolean addFieldModelChangedHandler(FieldModelChangedHandler fieldModelChangedHandler) {
        return changeHandlers.add(fieldModelChangedHandler);
    }

    public boolean removeFieldModelChangedHandler(FieldModelChangedHandler fieldModelChangedHandler) {
        return changeHandlers.remove(fieldModelChangedHandler);
    }

}

