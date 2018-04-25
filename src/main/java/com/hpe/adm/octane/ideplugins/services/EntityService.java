/*
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpe.adm.octane.ideplugins.services;

import static com.hpe.adm.octane.ideplugins.services.util.Util.getUiDataFromModel;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.entities.EntityList;
import com.hpe.adm.nga.sdk.entities.get.GetEntities;
import com.hpe.adm.nga.sdk.entities.get.GetEntity;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.extension.entities.ExtendedGetEntities;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import com.hpe.adm.octane.ideplugins.services.util.SdkUtil;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import com.hpe.adm.octane.ideplugins.services.util.Util;

public class EntityService {

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Inject
    private OctaneVersionService versionService;

    public Collection<EntityModel> findEntities(Entity entity) {
        return findEntities(entity, null, null);
    }

    public Collection<EntityModel> findEntities(Entity entity, Query.QueryBuilder query, Set<String> fields) {
        return findEntities(entity, query, fields, null);
    }

    public Collection<EntityModel> findEntities(Entity entity, Query.QueryBuilder query, Set<String> fields, Map<String, Set<String>> expand) {
        return findEntities(entity, query, fields, expand, null, null);
    }

    public Collection<EntityModel> findEntities(Entity entity, Query.QueryBuilder query, Set<String> fields, Map<String, Set<String>> expand, Integer offset, Integer limit) {
        return findEntities(entity, query, fields, expand, offset, limit, null, null);
    }

    public Collection<EntityModel> findEntities(Entity entity, Query.QueryBuilder query, Set<String> fields, Map<String, Set<String>> expand, Integer offset, Integer limit, String orderByField, Boolean orderByAsc) {
        EntityList entityList = octaneProvider.getOctane().entityList(entity.getApiEntityName());

        Query.QueryBuilder queryBuilder = null;

        if (entity.isSubtype()) {
            queryBuilder = entity.createMatchSubtypeQueryBuilder();
        }

        if (query != null) {
            if (queryBuilder == null) {
                queryBuilder = query;
            } else {
                queryBuilder = queryBuilder.and(query);
            }
        }

        GetEntities getRequest = entityList.get();
        if (queryBuilder != null) {
            getRequest = getRequest.query(queryBuilder.build());
        }

        OctaneVersion version = versionService.getOctaneVersion(true);
        version.discardBuildNumber();

        if (OctaneVersion.compare(version, OctaneVersion.Operation.HIGHER, OctaneVersion.EVERTON_P2)) {

            // Expand is integrated into the fields param
            Set<String> expandFields = new HashSet<>();

            if (fields != null) {
                expandFields.addAll(fields);
            }

            if (expand != null) {
                expand.keySet().forEach(relationFieldName -> {
                    if (expandFields.contains(relationFieldName)) {
                        expandFields.remove(relationFieldName);
                    }
                    // re-add it with expand
                    expandFields.add(
                            relationFieldName + "{" +
                                    expand.get(relationFieldName)
                                            .stream()
                                            .collect(Collectors.joining(","))
                                    + "}");
                });
            }

            if (fields != null || expand != null) {
                getRequest = getRequest.addFields(expandFields.toArray(new String[] {}));
            }

        } else {
            // Separate expand and fields query param
            if (expand != null) {
                getRequest = ((ExtendedGetEntities) getRequest).expand(expand);
            }
            if (fields != null && fields.size() != 0) {
                getRequest = getRequest.addFields(fields.toArray(new String[] {}));
            }
        }

        if (offset != null) {
            getRequest = getRequest.offset(offset);
        }

        if (limit != null) {
            getRequest = getRequest.limit(limit);
        }

        if(orderByField != null && orderByAsc != null){
            getRequest = getRequest.addOrderBy(orderByField, orderByAsc);
        } else {
            getRequest = getRequest.addOrderBy("id", true);
        }

        return getRequest.execute();
    }

    /**
     * Does not create the subtype match filter for you, like the methods that
     * use {@link Entity} as a param
     */
    private Collection<EntityModel> findEntities(String apiEntity, Query.QueryBuilder query, Set<String> fields) {
        EntityList entityList = octaneProvider.getOctane().entityList(apiEntity);
        GetEntities getRequest = entityList.get();
        if (query != null) {
            getRequest = getRequest.query(query.build());
        }
        if (fields != null && fields.size() != 0) {
            getRequest = getRequest.addFields(fields.toArray(new String[] {}));
        }
        getRequest.addOrderBy("id", true);

        Collection<EntityModel> col = getRequest.execute();
        return col;
    }

    /**
     * Useful when you have to fetch more than one type of entity at the same
     * time This method will make concurrent rest calls for each type of entity
     * (key of the maps)
     *
     * @param filterCriteria
     *            a query builder used for querying the entity type (key of the
     *            maps)
     * @param fieldListMap
     *            a map of the fields that will be returned after querying the
     *            entity type
     * @return a map with the result entities organized by entity type
     */
    public Map<Entity, Collection<EntityModel>> concurrentFindEntities(Map<Entity, Query.QueryBuilder> filterCriteria,
            Map<Entity, Set<String>> fieldListMap) {
        Map<Entity, Collection<EntityModel>> resultMap = new ConcurrentHashMap<>();

        // TODO, known subtypes should be under same rest call
        filterCriteria
                .keySet()
                .parallelStream()
                .forEach(
                        entityType -> resultMap.put(entityType,
                                findEntities(
                                        entityType.getApiEntityName(),
                                        filterCriteria.get(entityType),
                                        fieldListMap.get(entityType))));

        return resultMap;
    }

    /**
     * Return a single entity model
     * 
     * @param entityType
     *            {@link Entity}
     * @param entityId
     *            id
     * @param fields
     *            fields to be returned for the entity
     * @return EntityModel
     * @throws ServiceException
     *             on sdk error
     */
    public EntityModel findEntity(Entity entityType, Long entityId, Set<String> fields) throws ServiceException {
        try {
            GetEntity get = octaneProvider.getOctane()
                    .entityList(entityType.getApiEntityName())
                    .at(entityId.toString())
                    .get();

            if (fields != null && fields.size() != 0) {
                get = get.addFields(fields.toArray(new String[] {}));
            }

            EntityModel retrivedEntity = get.execute();
            
            //Make sure subtype is always set
            if(entityType.isSubtype()) {
                retrivedEntity.setValue(new StringFieldModel("subtype", entityType.getSubtypeName()));
            }
            
            return retrivedEntity;
            
        } catch (Exception e) {
            String message = "Failed to get " + entityType.name() + ": " + entityId;
            if (e instanceof OctaneException) {
                message = message + "<br>" + SdkUtil.getMessageFromOctaneException((OctaneException) e);
            }
            throw new ServiceException(message, e);
        }
    }

    public EntityModel findEntity(Entity entityType, Long entityId) throws ServiceException {
        return findEntity(entityType, entityId, null);
    }

    /**
     * Get next possible phases for an entity
     *
     * @param entityType
     *            {@link Entity}
     * @param currentPhaseId
     *            id of the current phase object
     * @return list of {@link Entity#PHASE}
     */
    public Collection<EntityModel> findPossibleTransitionFromCurrentPhase(Entity entityType, String currentPhaseId) {
        Set<String> fields = new HashSet<>();
        fields.add("source_phase");
        fields.add("target_phase");
        fields.add("is_primary");
        fields.add("entity");

        ArrayList<EntityModel> possibleTransitions = new ArrayList<>();
        String entityName;
        if (entityType.isSubtype()) {
            entityName = entityType.getSubtypeName();
        } else {
            entityName = entityType.getTypeName();
        }
        Collection<EntityModel> transitions = findEntities(Entity.TRANSITION,
                Query.statement("entity", QueryMethod.EqualTo, entityName), fields);

        for (EntityModel transition : transitions) {
            String tempPhase = Util.getUiDataFromModel(transition.getValue("source_phase"), "id");
            if (currentPhaseId.equals(tempPhase)) {
                if (transition.getValue("is_primary").getValue().equals(Boolean.TRUE)) {
                    possibleTransitions.add(0, transition);
                } else {
                    possibleTransitions.add(transition);
                }
            }
        }
        return possibleTransitions;
    }

    @SuppressWarnings("rawtypes")
    public void updateEntityPhase(EntityModel entityModel, ReferenceFieldModel nextPhase) {
        String entityId = getUiDataFromModel(entityModel.getValue("id"));
        Entity entityType = Entity.getEntityType(entityModel);
        EntityList entityList = octaneProvider.getOctane().entityList(entityType.getApiEntityName());

        ReferenceFieldModel updatePhaseModel = new ReferenceFieldModel("phase", nextPhase.getValue());

        Set<FieldModel> fields = new HashSet<>();
        fields.add(updatePhaseModel);
        EntityModel updatedEntity = new EntityModel(fields);

        entityList.at(entityId).update().entity(updatedEntity).execute();
    }

    public void updateEntity(EntityModel entityModel) {
        String entityId = getUiDataFromModel(entityModel.getValue("id"));
        Entity entityType = Entity.getEntityType(entityModel);
        EntityList entityList = octaneProvider.getOctane().entityList(entityType.getApiEntityName());
        entityList.at(entityId).update().entity(entityModel).execute();
    }

    public void openInBrowser(EntityModel entityModel) {
        Entity entityType = Entity.getEntityType(entityModel);
        Integer entityId = Integer.valueOf(getUiDataFromModel(entityModel.getValue("id")));
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                Entity ownerEntityType = null;
                Integer ownerEntityId = null;
                if (entityType == Entity.COMMENT) {
                    ReferenceFieldModel owner = (ReferenceFieldModel) Util.getContainerItemForCommentModel(entityModel);
                    ownerEntityType = Entity.getEntityType(owner.getValue());
                    ownerEntityId = Integer.valueOf(Util.getUiDataFromModel(owner, "id"));
                }
                URI uri = UrlParser.createEntityWebURI(
                        connectionSettingsProvider.getConnectionSettings(),
                        entityType == Entity.COMMENT ? ownerEntityType : entityType,
                        entityType == Entity.COMMENT ? ownerEntityId : entityId);
                desktop.browse(uri);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
