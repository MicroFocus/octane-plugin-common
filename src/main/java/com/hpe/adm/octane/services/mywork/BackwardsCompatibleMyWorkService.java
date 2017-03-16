package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.nonentity.OctaneVersionService;
import org.jsoup.helper.StringUtil;

import java.util.*;

/**
 * Implementations that should works over more version of the octane server
 */
public class BackwardsCompatibleMyWorkService implements MyWorkService {

    @Inject
    private OctaneVersionService octaneVersionService;

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

        Map<Entity, Collection<EntityModel>> entities;

        String version = octaneVersionService.getOctaneVersion();

        int comparison;
        if(StringUtil.isBlank(version)){
            comparison = 1;
        } else {
            comparison = version.compareTo("12.53.20");
        }

        //lower than 12.53.20
        if(comparison == 0 || comparison == -1){
            entities = entityService.concurrentFindEntities(myWorkFilterCriteria.getServersideFilterCriteria(), fieldListMap);
        } else {
            entities = entityService.concurrentFindEntities(myWorkFilterCriteria.getStaticFilterCriteria(), fieldListMap);
        }

        Collection<EntityModel> result = new ArrayList<>();

        entities
                .keySet()
                .stream()
                .sorted(Comparator.comparing(Enum::name))
                .forEach(entity -> result.addAll(entities.get(entity)));

        return result;
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