package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.*;

class PostDynamoMyWorkService implements MyWorkService {

    @Inject
    private EntityService entityService;

    @Inject
    private MyWorkFilterCriteria myWorkFilterCriteria;

    @Override
    public Collection<EntityModel> getMyWork() {
        return getMyWork(new HashMap<>());
    }

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {

        Collection<EntityModel> result = new ArrayList<>();

        Map<Entity, Collection<EntityModel>> entities = entityService.concurrentFindEntities(myWorkFilterCriteria.getServersideFilterCriteria(), fieldListMap);

        entities
                .keySet()
                .stream()
                .sorted(Comparator.comparing(Enum::name))
                .forEach(entity -> result.addAll(entities.get(entity)));

        return result;
    }

    @Override
    public boolean isAddingToMyWorkSupported() {
        return false;
    }

    @Override
    public boolean isAddingToMyWorkSupported(Entity entityType) {
        return false;
    }

    @Override
    public boolean isInMyWork(EntityModel entityModel) {
        return false;
    }

    @Override
    public boolean addToMyWork(EntityModel entityModel) {
        return false;
    }

    @Override
    public boolean removeFromMyWork(EntityModel entityModel) {
        return false;
    }

}
