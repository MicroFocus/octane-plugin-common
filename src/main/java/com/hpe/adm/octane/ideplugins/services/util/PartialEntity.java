package com.hpe.adm.octane.ideplugins.services.util;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONObject;

public class PartialEntity extends EntityTypeIdPair {

    private String entityName;

    public PartialEntity(EntityModel entityModel){
        super(
                Long.parseLong(entityModel.getValue("id").getValue().toString()),
                Entity.getEntityType(entityModel)
        );
        this.entityName = entityModel.getValue("name").getValue().toString();
    }

    public PartialEntity(Long entityId, String entityName, Entity entityType){
        super(entityId, entityType);
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public static EntityModel toEntityModel(PartialEntity partialEntity){
        EntityModel entityModel = new EntityModel();

        entityModel.setValue(new StringFieldModel("id", String.valueOf(partialEntity.getEntityId())));

        entityModel.setValue(new StringFieldModel("type", String.valueOf(partialEntity.getEntityType().getTypeName())));

        if(partialEntity.getEntityType().isSubtype()) {
            entityModel.setValue(new StringFieldModel("subtype", String.valueOf(partialEntity.getEntityType().getSubtypeName())));
        }

        return entityModel;
    }

    public static JSONObject toJsonObject(PartialEntity partialEntity){
        JSONObject jsonObject = EntityTypeIdPair.toJsonObject(partialEntity);
        jsonObject.put("entityName", partialEntity.getEntityName());
        return jsonObject;
    }

    public static PartialEntity fromJsonObject(JSONObject jsonObject){
        if (jsonObject == null)
            return null;
        EntityTypeIdPair entityTypeIdPair = EntityTypeIdPair.fromJsonObject(jsonObject);
        String entityName = jsonObject.getString("entityName");

        return new PartialEntity(
                entityTypeIdPair.getEntityId(),
                entityName,
                entityTypeIdPair.getEntityType());
    }
}