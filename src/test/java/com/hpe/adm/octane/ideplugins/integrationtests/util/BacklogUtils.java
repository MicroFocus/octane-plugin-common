package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BacklogUtils {

    @Inject
    private OctaneProvider octaneProvider;

    /**
     * Retrieves the backlog items: tests and work items
     *
     * @return a list of the work items and lists
     */
    public List<EntityModel> retrieveBacklog() {
        Octane octane = octaneProvider.getOctane();
        List<EntityModel> workItems = new ArrayList<>(octane.entityList(Entity.WORK_ITEM.getApiEntityName()).get()
                .query(Query.not(Constants.SUBTYPE, QueryMethod.EqualTo, Constants.WORK_ITEM_ROOT).build()).execute());
        List<EntityModel> tests = new ArrayList<>(octane.entityList(Entity.TEST.getApiEntityName()).get().execute());
        return Stream.concat(workItems.stream(), tests.stream()).collect(Collectors.toList());

    }

    /**
     * Deletes the backlog items
     */
    public void deleteBacklogItems() {
        List<EntityModel> workspaceEntities = retrieveBacklog();
        Query.QueryBuilder workItemsQuery = null;
        Query.QueryBuilder testItemsQuery = null;
        for (EntityModel entityModel : workspaceEntities) {
            String entityType = entityModel.getValue(Constants.TYPE).getValue().toString();
            if (Entity.WORK_ITEM.getEntityName().equals(entityType)) {
                if (workItemsQuery != null) {
                    workItemsQuery = workItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    workItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
            if (Entity.TEST.getEntityName().equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    testItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
            if (Entity.TEST_RUN.getEntityName().equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    testItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
        }
        if (workspaceEntities.size() > 0) {
            if (testItemsQuery != null)
                octaneProvider.getOctane().entityList(Entity.TEST.getApiEntityName()).delete().query(testItemsQuery.build()).execute();
            if (workItemsQuery != null)
                octaneProvider.getOctane().entityList(Entity.WORK_ITEM.getApiEntityName()).delete().query(workItemsQuery.build()).execute();
        }
    }
}
