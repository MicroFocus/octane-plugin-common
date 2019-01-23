package com.hpe.adm.octane.ideplugins.services.model;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;

import java.util.Set;

@SuppressWarnings("rawtypes")
public class ReadOnlyEntityModel extends EntityModel {

    public ReadOnlyEntityModel(Set<FieldModel> values) {
        super(values);
    }

    public EntityModel setValue(FieldModel fieldModel) {
        throw new RuntimeException(ReadOnlyEntityModel.class.toString() + " is read-only");
    }

    public EntityModel setValues(Set<FieldModel> values) {
        throw new RuntimeException(ReadOnlyEntityModel.class.toString() + " is read-only");
    }

}
