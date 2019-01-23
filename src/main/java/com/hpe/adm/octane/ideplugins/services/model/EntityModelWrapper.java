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

