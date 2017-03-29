package com.hpe.adm.octane.services.ui;

import java.util.List;


public class FormLayoutSection {
    private String sectionTitle;
    private List<FormField> fields;

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public List<FormField> getFields() {
        return fields;
    }

    public void setFields(List<FormField> fields) {
        this.fields = fields;
    }
}
