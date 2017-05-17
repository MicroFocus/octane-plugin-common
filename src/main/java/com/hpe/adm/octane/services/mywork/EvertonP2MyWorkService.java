package com.hpe.adm.octane.services.mywork;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.MetadataService;
import com.hpe.adm.octane.services.UserService;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.util.EntityUtil;

import java.util.*;
import java.util.stream.Collectors;

import static com.hpe.adm.octane.services.filtering.Entity.COMMENT;
import static com.hpe.adm.octane.services.mywork.MyWorkUtil.*;

class EvertonP2MyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private UserService userService;

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private MetadataService metadataService;

    private static final BiMap<String, Entity> relationFieldTypeMap = HashBiMap.create();
    static {
        relationFieldTypeMap.put("my_follow_items_work_item", Entity.WORK_ITEM);
        relationFieldTypeMap.put("my_follow_items_task", Entity.TASK);
        relationFieldTypeMap.put("my_follow_items_test", Entity.TEST);
        relationFieldTypeMap.put("my_follow_items_run", Entity.TEST_RUN);
    }

    @Override
    public Collection<EntityModel> getMyWork() {
        return getMyWork(new HashMap<>());
    }

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {
        Collection<EntityModel> result = new ArrayList<>();

        Query.QueryBuilder qUser = createUserQuery("user", userService.getCurrentUserId());

        Collection<EntityModel> userItems = entityService.findEntities(Entity.USER_ITEM, qUser, metadataService.getFields(Entity.USER_ITEM), createExpandFieldsMap(fieldListMap));
        userItems = sortUserItems(userItems);
        result.addAll(userItems);

        result.addAll(getCommentsAsUserItems());

        return result;
    }

    private Collection<EntityModel> getCommentsAsUserItems(){
        // Also get comments
        Collection<EntityModel> comments = entityService.findEntities(
                COMMENT,
                createUserQuery("mention_user", userService.getCurrentUserId()),
                metadataService.getFields(Entity.COMMENT));

        return MyWorkUtil.wrapCollectionIntoUserItem(comments, -1);
    }

    /**
     * Converts the req fields into an expand clause for user item relations
     *
     * @param fieldListMap
     * @return
     */
    private static Map<String, Set<String>> createExpandFieldsMap(Map<Entity, Set<String>> fieldListMap) {
        Map<String, Set<String>> result = new HashMap<>();
        fieldListMap.keySet().stream().forEach(entity -> {
            //Group by parent type if needed (only exception is task), only consider fields for relevant relations
            Entity type = entity.isSubtype() ? entity.getSubtypeOf() : entity;
            if (relationFieldTypeMap.values().contains(type)) {
                //get the relation field for this type
                String fieldName = relationFieldTypeMap.inverse().get(type);
                if (!result.containsKey(fieldName)) {
                    result.put(fieldName, new HashSet<>());
                }
                //Add the expand clause
                result.get(fieldName).addAll(fieldListMap.get(entity));
            }
        });
        return result;
    }

    private static Collection<EntityModel> sortUserItems(Collection<EntityModel> userItems) {
        try {
            return userItems
                    .stream()
                    .sorted((userItemLeft, userItemRight) -> {
                        EntityModel entityLeft = MyWorkUtil.getEntityModelFromUserItem(userItemLeft);
                        EntityModel entityRight = MyWorkUtil.getEntityModelFromUserItem(userItemRight);

                        Entity entityTypeLeft = Entity.getEntityType(entityLeft);
                        Entity entityTypeRight = Entity.getEntityType(entityRight);

                        if (entityTypeLeft != entityTypeRight) {
                            return entityTypeComparator.compare(entityTypeLeft, entityTypeRight);
                        } else {
                            Long leftId = Long.parseLong(entityLeft.getValue("id").getValue().toString());
                            Long rightId = Long.parseLong(entityRight.getValue("id").getValue().toString());
                            return leftId.compareTo(rightId);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return userItems;
        }
    }

    @Override
    public boolean isAddingToMyWorkSupported() {
        return true;
    }

    @Override
    public boolean isAddingToMyWorkSupported(Entity entityType) {
        return addToMyWorkEntities.contains(entityType);
    }

    private EntityModel findUserItemForEntity(EntityModel entityModel) {
        String entityType = getEntityTypeName(Entity.getEntityType(entityModel));
        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(entityModel));
        String id = entityModel.getValue("id").getValue().toString();

        Query.QueryBuilder qItem = Query.statement(
                followField, QueryMethod.EqualTo, Query.statement("id", QueryMethod.EqualTo, id));

        Query.QueryBuilder qUser = createUserQuery("user", userService.getCurrentUserId());

        Query.QueryBuilder qType = Query.statement("entity_type", QueryMethod.EqualTo, entityType);
        Query.QueryBuilder qOrigin = Query.statement("origin", QueryMethod.EqualTo, 1);

        Collection<EntityModel> userItems = entityService.findEntities(Entity.USER_ITEM,
                qUser.and(qType).and(qOrigin).and(qItem),
                null);

        if (userItems.size() != 1) {
            return null;
        } else {
            return userItems.iterator().next();
        }
    }

    @Override
    public boolean isInMyWork(EntityModel entityModel) {
        // TODO: can be optimized
        Collection<EntityModel> myWork = getMyWork();
        myWork = MyWorkUtil.getEntityModelsFromUserItems(myWork);
        return EntityUtil.containsEntityModel(myWork, entityModel);
    }

    @Override
    public boolean addToMyWork(EntityModel entityModel) {
        if (isInMyWork(entityModel)) {
            return false;
        }

        EntityModel newUserItem = createNewUserItem(entityModel);

        octaneProvider
                .getOctane()
                .entityList(Entity.USER_ITEM.getApiEntityName())
                .create()
                .entities(Collections.singletonList(newUserItem))
                .execute();

        return true;
    }

    protected EntityModel createNewUserItem(EntityModel wrappedEntityModel) {
        EntityModel newUserItem = new EntityModel();
        newUserItem.setValue(new LongFieldModel("origin", 1L));
        newUserItem.setValue(new BooleanFieldModel("is_new", true));
        newUserItem.setValue(new ReferenceFieldModel("reason", null));

        String entityType = getEntityTypeName(Entity.getEntityType(wrappedEntityModel));

        newUserItem.setValue(new StringFieldModel("entity_type", entityType));

        newUserItem.setValue(new ReferenceFieldModel("user", userService.getCurrentUser()));

        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(wrappedEntityModel));

        newUserItem.setValue(new ReferenceFieldModel(followField, wrappedEntityModel));

        return newUserItem;
    }

    @Override
    public boolean removeFromMyWork(EntityModel entityModel) {

        EntityModel userItem = findUserItemForEntity(entityModel);
        if (userItem == null) {
            return false;
        }

        Integer id = Integer.valueOf(userItem.getValue("id").getValue().toString());
        try {
            octaneProvider.getOctane().entityList(Entity.USER_ITEM.getApiEntityName()).at(id).delete().execute();
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

}