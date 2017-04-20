package com.hpe.adm.octane.services.mywork;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.filtering.PredefinedEntityComparator;

public interface MyWorkService {

    // Used whenever a sorting is done based on entity type
    Comparator<Entity> entityTypeComparator = new PredefinedEntityComparator();

    Collection<EntityModel> getMyWork();

    Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap);

    boolean isAddingToMyWorkSupported();

    boolean isAddingToMyWorkSupported(Entity entityType);

    boolean isInMyWork(EntityModel entityModel);

    boolean addToMyWork(EntityModel entityModel);

    boolean removeFromMyWork(EntityModel entityModel);

}