package com.hpe.adm.octane.integrationtests.util;


import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.filtering.Entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Data generator for unit tests
 * TODO: find a nice way to move this into tests only (DI issues, currently part of the ServiceModule)
 */
public class EntityGenerator {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat generateNameDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");

    private static final Set<String> transitionFields = new HashSet<>();
    static {
        transitionFields.add("source_phase");
        transitionFields.add("target_phase");
        transitionFields.add("is_primary");
        transitionFields.add("entity");
    }

    private OctaneProvider octaneProvider;

    public EntityGenerator(OctaneProvider octaneProvider) {
        this.octaneProvider = octaneProvider;
    }

    private Map<Entity, Integer> generatedEntityCount = new HashMap<>();

    public EntityModel createEntityModel(Entity entity) {

        EntityModel newEntity = new EntityModel();

        if(entity.isSubtype()){
            newEntity.setValue(new StringFieldModel("subtype", entity.getSubtypeName()));
        }

        newEntity.setValue(new StringFieldModel("name", generateEntityName(entity)));
        newEntity.setValue(new ReferenceFieldModel("parent", getWorkItemRoot()));
        newEntity.setValue(new ReferenceFieldModel("phase", getDefaultPhase(entity)));

        Collection<EntityModel> createdEntities = octaneProvider
                .getOctane()
                .entityList(entity.getApiEntityName())
                .create()
                .entities(Collections.singleton(newEntity))
                .execute();

        if(createdEntities.size() != 1){
            throw new RuntimeException("Failed to create entity of type: " + entity);
        }

        int newEntityId = Integer.valueOf(createdEntities.iterator().next().getValue("id").getValue().toString());

        //Refresh entity fields, some might have been set by the server
        newEntity = octaneProvider
                .getOctane()
                .entityList(entity.getApiEntityName())
                .at(newEntityId)
                .get()
                .execute();

        return newEntity;
    }

    public void deleteEntityModel(EntityModel entityModel){
        octaneProvider
                .getOctane()
                .entityList(Entity.getEntityType(entityModel).getApiEntityName())
                .at(Integer.valueOf(entityModel.getValue("id").getValue().toString()))
                .delete()
                .execute();

    }

    private String generateEntityName(Entity entity) {
        if (!generatedEntityCount.containsKey(entity)) {
            generatedEntityCount.put(entity, 0);
        }

        int count = generatedEntityCount.get(entity);
        count++;
        generatedEntityCount.put(entity, count);

        return entity.getEntityName() + ":" + generateNameDateFormat.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
    }

    private EntityModel getWorkItemRoot() {
        Octane octane = octaneProvider.getOctane();
        Query query = Query.statement("subtype", QueryMethod.EqualTo, "work_item_root").build();
        Collection<EntityModel> roots = octane.entityList("work_items").get().query(query).execute();

        if (roots.size() != 1) {
            throw new RuntimeException("Error fetching work item root, got num of results: " + roots.size());
        }

        return roots.iterator().next();
    }

    public EntityModel getDefaultPhase(Entity entityType){

        String entityName;
        if (entityType.isSubtype()) {
            entityName = entityType.getSubtypeName();
        } else {
            entityName = entityType.getTypeName();
        }

        Octane octane = octaneProvider.getOctane();

        EntityModel transitionEntity = octane
                .entityList(Entity.TRANSITION.getApiEntityName())
                .get()
                .query(Query.statement("entity", QueryMethod.EqualTo, entityName).build())
                .addFields(transitionFields.toArray(new String[]{}))
                .execute()
                .iterator()
                .next();

        EntityModel phaseEntity = (EntityModel) transitionEntity.getValue("source_phase").getValue();

        return phaseEntity;
    }

}