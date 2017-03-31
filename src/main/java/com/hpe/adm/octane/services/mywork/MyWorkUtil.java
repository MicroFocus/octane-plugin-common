package com.hpe.adm.octane.services.mywork;

import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.LongFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.ArrayList;
import java.util.Collection;

public class MyWorkUtil {

    /**
     * Constructs a metaphase query builder to match "logical_name":"metaphase.entity.phasename",
     *
     * @param entity
     * @param phases
     * @return
     */
    static Query.QueryBuilder createPhaseQuery(Entity entity, String... phases) {
        Query.QueryBuilder phaseQueryBuilder = null;
        for (String phaseName : phases) {
            String phaseLogicalName = "metaphase." + entity.getTypeName() + "." + phaseName;
            Query.QueryBuilder currentPhaseQueryBuilder =
                    Query.statement("metaphase", QueryMethod.EqualTo,
                            Query.statement("logical_name", QueryMethod.EqualTo, phaseLogicalName)
                    );
            if (phaseQueryBuilder == null) {
                phaseQueryBuilder = currentPhaseQueryBuilder;
            } else {
                phaseQueryBuilder = phaseQueryBuilder.or(currentPhaseQueryBuilder);
            }
        }

        return Query.statement("phase", QueryMethod.EqualTo, phaseQueryBuilder);
    }

    /**
     * @param logicalNames
     * @return
     */
    static Query.QueryBuilder createNativeStatusQuery(String... logicalNames) {
        Query.QueryBuilder nativeStatusQueryBuilder = null;
        for (String logicalName : logicalNames) {
            Query.QueryBuilder currentNativeStatusQueryBuilder =
                    Query.statement("logical_name", QueryMethod.EqualTo, logicalName);
            if (nativeStatusQueryBuilder == null) {
                nativeStatusQueryBuilder = currentNativeStatusQueryBuilder;
            } else {
                nativeStatusQueryBuilder = nativeStatusQueryBuilder.or(currentNativeStatusQueryBuilder);
            }
        }
        return  Query.statement("native_status", QueryMethod.EqualTo, nativeStatusQueryBuilder);
    }

    static Query.QueryBuilder createUserQuery(String fieldName, Long userId) {
        return  Query.statement(fieldName, QueryMethod.EqualTo,
                Query.statement("id", QueryMethod.EqualTo, userId));
    }

    /**
     * Fetch the entity model wrapped in the user item for the UI
     * @param userItem
     * @return
     */
    public static EntityModel getEntityModelFromUserItem(EntityModel userItem){
        if(Entity.USER_ITEM != Entity.getEntityType(userItem)){
            throw new ServiceRuntimeException("Given param entity is not of type: user_item");
        }
        String followField = "my_follow_items_" + userItem.getValue("entity_type").getValue();
        return (EntityModel) userItem.getValue(followField).getValue();
    }

    /**
     * Used for backwards compatibility for older server version
     * For older server version plain entity models will be wrapped in user items
     * @param entityModel
     * @return
     */
    static EntityModel wrapEntityModelIntoUserItem(EntityModel entityModel, long origin){
        EntityModel userItem = new EntityModel();

        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(entityModel));
        userItem.setValue(new ReferenceFieldModel(followField, entityModel));
        userItem.setValue(new StringFieldModel("entity_type", getEntityTypeName(Entity.getEntityType(entityModel))));

        userItem.setValue(new LongFieldModel("origin", origin));
        userItem.setValue(new StringFieldModel("type", "user_item"));
        userItem.setValue(new StringFieldModel("id", null));

        return userItem;
    }

    static Collection<EntityModel> wrapCollectionIntoUserItem(Collection<EntityModel> entityModels, long origin){
        Collection<EntityModel> result = new ArrayList<>();
        entityModels.forEach(entityModel -> result.add(wrapEntityModelIntoUserItem(entityModel, origin)));
        return result;
    }

    static String getEntityTypeName(Entity entity){
        if(entity.isSubtype()){
            return entity.getSubtypeOf().getTypeName();
        } else {
            return entity.getTypeName();
        }
    }

}