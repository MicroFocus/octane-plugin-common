package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.services.util.OctaneVersion;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementations that should works over more version of the octane server
 */
public class BackwardsCompatibleMyWorkService implements MyWorkService {

    @Inject
    private OctaneVersionService octaneVersionService;

    @Inject
    private PreDynamoMyWorkService preDynamoMyWorkService;

    @Inject
    private PostDynamoMyWorkService postDynamoMyWorkService;

    @Override
    public Collection<EntityModel> getMyWork() {
        return getMyWork(new HashMap<>());
    }

    @Override
    public Collection<EntityModel> getMyWork(Map<Entity, Set<String>> fieldListMap) {
        if(compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO)){
            return preDynamoMyWorkService.getMyWork(fieldListMap);
        } else {
            return postDynamoMyWorkService.getMyWork(fieldListMap);
        }
    }

    @Override
    public boolean isAddingToMyWorkSupported() {
        return !compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.CHELSEA);
    }

    @Override
    public boolean isAddingToMyWorkSupported(Entity entityType) {

        if(!isAddingToMyWorkSupported()) return false;

        if(compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO)){
            return preDynamoMyWorkService.isAddingToMyWorkSupported(entityType);
        } else {
            return postDynamoMyWorkService.isAddingToMyWorkSupported(entityType);
        }
    }

    @Override
    public boolean isInMyWork(EntityModel entityModel) {
        if(compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO)){
            return preDynamoMyWorkService.isInMyWork(entityModel);
        } else {
            return postDynamoMyWorkService.isInMyWork(entityModel);
        }
    }

    @Override
    public boolean addToMyWork(EntityModel entityModel) {
        if(compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO)){
            return preDynamoMyWorkService.addToMyWork(entityModel);
        } else {
            return postDynamoMyWorkService.addToMyWork(entityModel);
        }
    }

    @Override
    public boolean removeFromMyWork(EntityModel entityModel) {
        if(compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO)){
            return preDynamoMyWorkService.removeFromMyWork(entityModel);
        } else {
            return postDynamoMyWorkService.removeFromMyWork(entityModel);
        }
    }

    private boolean compareServerVersion(OctaneVersion.Operation operation, OctaneVersion otherVersion){
        OctaneVersion version = octaneVersionService.getOctaneVersion();
        version.discardBuildNumber();
        return OctaneVersion.compare(version, operation, otherVersion);
    }

}