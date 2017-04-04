package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.UserService;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.*;

import static com.hpe.adm.octane.services.mywork.MyWorkUtil.cloneFieldListMap;
import static com.hpe.adm.octane.services.mywork.MyWorkUtil.getEntityTypeName;
import static com.hpe.adm.octane.services.mywork.MyWorkUtil.wrapCollectionIntoUserItem;

class EvertonP1MyWorkService extends EvertonP2MyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private UserService userService;

    @Inject
    private MyWorkFilterCriteria myWorkFilterCriteria;

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {

        Collection<EntityModel> result = new ArrayList<>();

        Map<Entity, Collection<EntityModel>> entities = entityService.concurrentFindEntities(myWorkFilterCriteria.getStaticFilterCriteria(), fieldListMap);

        //Done for backwards compatibility, the UI excepts user_item entities from later server version of Octane,
        //We can wrap the simple entities into dummy user items to not have to change the UI code
        entities
                .keySet()
                .forEach(key -> entities.put(key, wrapCollectionIntoUserItem(entities.get(key), 0)));

        //Also need to get the added items manually
        Map<Entity, Collection<EntityModel>> addedEntities = getAddedItems(fieldListMap);

        addedEntities
                .keySet()
                .forEach(key -> addedEntities.put(key, wrapCollectionIntoUserItem(addedEntities.get(key), 1)));

        entities
                .keySet()
                .stream()
                .sorted(Comparator.comparing(Enum::name))
                .forEach(entity -> {
                    if(entities.containsKey(entity)) {
                        result.addAll(entities.get(entity));
                    }
                    if(addedEntities.containsKey(entity)) {
                        result.addAll(addedEntities.get(entity));
                    }
                });

        return result;
    }

    protected Map<Entity, Collection<EntityModel>> getAddedItems(Map<Entity, Set<String>> fieldListMap) {

        final Map<Entity, Set<String>> fieldListMapCopy = cloneFieldListMap(fieldListMap);

        String addToMyWorkFieldName = "user_item";

        Map<Entity, Query.QueryBuilder> followFilterCriteria = new HashMap<>();

        myWorkFilterCriteria.getStaticFilterCriteria()
                .keySet()
                .stream()
                .filter(this::isAddingToMyWorkSupported)
                .forEach(key -> {
                    Query.QueryBuilder qb;
                    if (key.isSubtype()) {
                        qb = key.createMatchSubtypeQueryBuilder().and(createUserItemQueryBuilder());
                    } else {
                        qb = createUserItemQueryBuilder();
                    }
                    followFilterCriteria.put(key, qb);
                    if (fieldListMapCopy != null && fieldListMapCopy.containsKey(key)) {
                        fieldListMapCopy.get(key).add(addToMyWorkFieldName);
                    }
                });

        return entityService.concurrentFindEntities(followFilterCriteria, fieldListMapCopy);
    }

    @Override
    protected EntityModel createNewUserItem(EntityModel wrappedEntityModel){
        EntityModel newUserItem = new EntityModel();
        newUserItem.setValue(new LongFieldModel("origin", 1L));
        newUserItem.setValue(new ReferenceFieldModel("reason", null));
        String entityType =  getEntityTypeName(Entity.getEntityType(wrappedEntityModel));
        newUserItem.setValue(new StringFieldModel("entity_type", entityType));
        newUserItem.setValue(new ReferenceFieldModel("user", userService.getCurrentUser()));
        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(wrappedEntityModel));
        newUserItem.setValue(new ReferenceFieldModel(followField, wrappedEntityModel));
        return newUserItem;
    }

    private Query.QueryBuilder createUserItemQueryBuilder() {
        return Query.statement("user_item", QueryMethod.EqualTo,
                Query.statement("user", QueryMethod.EqualTo,
                        Query.statement("id", QueryMethod.EqualTo, userService.getCurrentUserId())
                )
        );
    }

}