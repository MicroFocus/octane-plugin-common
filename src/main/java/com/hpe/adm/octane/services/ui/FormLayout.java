package com.hpe.adm.octane.services.ui;

import com.hpe.adm.octane.services.filtering.Entity;

import java.util.List;

public class FormLayout {
    private Long formId;
    private Entity entity;
    private String formName;
    private List<FormLayoutSection> formLayoutSections;
    private boolean isDefault;
    private boolean isDefaultNew;

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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isDefaultNew() {
        return isDefaultNew;
    }

    public void setDefaultNew(boolean defaultNew) {
        isDefaultNew = defaultNew;
    }
}
