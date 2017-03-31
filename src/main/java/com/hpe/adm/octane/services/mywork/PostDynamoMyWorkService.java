package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.UserService;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.*;

import static com.hpe.adm.octane.services.filtering.Entity.COMMENT;
import static com.hpe.adm.octane.services.mywork.MyWorkUtil.*;

class PostDynamoMyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private UserService userService;

    @Inject
    private OctaneProvider octaneProvider;

    @Override
    public Collection<EntityModel> getMyWork() {
        return getMyWork(new HashMap<>());
    }

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {

        Collection<EntityModel> result = new ArrayList<>();

        Query.QueryBuilder qUser = createUserQuery("user", userService.getCurrentUserId());

        Collection<EntityModel> userItems = entityService.findEntities(Entity.USER_ITEM, qUser, null);
        result.addAll(userItems);

        //Also get comments
        Collection<EntityModel> comments = entityService.findEntities(
                COMMENT,
                createUserQuery("mention_user", userService.getCurrentUserId()),
                fieldListMap.get(COMMENT)
        );

        result.addAll(MyWorkUtil.wrapCollectionIntoUserItem(comments, -1));

        return result;
    }

    @Override
    public boolean isAddingToMyWorkSupported() {
        return true;
    }

    @Override
    public boolean isAddingToMyWorkSupported(Entity entityType) {
        return addToMyWorkEntities.contains(entityType);
    }

    private EntityModel findUserItemForEntity(EntityModel entityModel){
        String entityType =  getEntityTypeName(Entity.getEntityType(entityModel));
        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(entityModel));
        String id = entityModel.getValue("id").getValue().toString();

        Query.QueryBuilder qItem = Query.statement(
                followField, QueryMethod.EqualTo, Query.statement("id", QueryMethod.EqualTo, id)
        );

        Query.QueryBuilder qUser = createUserQuery("user", userService.getCurrentUserId());

        Query.QueryBuilder qType = Query.statement("entity_type", QueryMethod.EqualTo, entityType);
        Query.QueryBuilder qOrigin = Query.statement("origin", QueryMethod.EqualTo, 1);

        Collection<EntityModel> userItems =
                entityService.findEntities(Entity.USER_ITEM,
                        qUser.and(qType).and(qOrigin).and(qItem),
                        null);

        if(userItems.size()!=1){
            return null;
        } else {
            return userItems.iterator().next();
        }
    }

    @Override
    public boolean isInMyWork(EntityModel entityModel) {
        if(!isAddingToMyWorkSupported(Entity.getEntityType(entityModel))){
            return false;
        }
        EntityModel userItem = findUserItemForEntity(entityModel);
        return userItem != null;
    }

    @Override
    public boolean addToMyWork(EntityModel entityModel) {
        if(isInMyWork(entityModel)) {
            return false;
        }

        EntityModel newUserItem = new EntityModel();
        newUserItem.setValue(new LongFieldModel("origin", 1L));
        newUserItem.setValue(new BooleanFieldModel("is_new", true));
        newUserItem.setValue(new ReferenceFieldModel("reason", null));

        String entityType =  getEntityTypeName(Entity.getEntityType(entityModel));

        newUserItem.setValue(new StringFieldModel("entity_type", entityType));

        newUserItem.setValue(new ReferenceFieldModel("user", userService.getCurrentUser()));

        String followField = "my_follow_items_" + getEntityTypeName(Entity.getEntityType(entityModel));

        newUserItem.setValue(new ReferenceFieldModel(followField, entityModel));

        octaneProvider
                .getOctane()
                .entityList(Entity.USER_ITEM.getApiEntityName())
                .create()
                .entities(Collections.singletonList(newUserItem))
                .execute();

        return true;
    }

    @Override
    public boolean removeFromMyWork(EntityModel entityModel) {

        EntityModel userItem = findUserItemForEntity(entityModel);
        if(userItem == null){
            return false;
        }

        Integer id = Integer.valueOf(userItem.getValue("id").getValue().toString());
        try {
            octaneProvider.getOctane().entityList(Entity.USER_ITEM.getApiEntityName()).at(id).delete().execute();
        } catch (Exception ex){
            return false;
        }

        return true;
    }

}
