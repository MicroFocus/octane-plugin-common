package com.hpe.adm.octane.services.mywork;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface MyWorkService {

    Collection<EntityModel> getMyWork();

    Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap);

    boolean isAddingToMyWorkSupported(Entity entityType);

    boolean isInMyWork(EntityModel entityModel);

    boolean addToMyWork(EntityModel entityModel);

    boolean removeFromMyWork(EntityModel entityModel);

}