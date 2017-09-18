/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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

package com.hpe.adm.octane.ideplugins.services.ui;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;

import java.util.List;

public class FormLayout {

    final private String NEW = "NEW";
    final private String EDIT = "EDIT";

    private Long formId;
    private Entity entity;
    private String formName;
    private List<FormLayoutSection> formLayoutSections;
    private String defaultField;

    public FormLayout() {
    }

    public FormLayout(Long formId, Entity entity, String formName) {
        this.formId = formId;
        this.entity = entity;
        this.formName = formName;
    }

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public List<FormLayoutSection> getFormLayoutSections() {
        return formLayoutSections;
    }

    public void setFormLayoutSections(List<FormLayoutSection> formLayoutSections) {
        this.formLayoutSections = formLayoutSections;
    }

    public String getDefaultField() {
        return defaultField;
    }

    public void setDefault(int isDefault) {
        if(isDefault <= 1){
            defaultField = NEW;
        }
        else if (isDefault >= 2) {
            defaultField = EDIT;
        }
    }

    public void setDefault(boolean isDefault){
        if(isDefault){
            defaultField = EDIT;
        } else {
            defaultField = NEW;
        }
    }


}
