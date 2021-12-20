/*
 * Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
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

import com.google.api.client.util.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.entities.OctaneCollection;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ModelParser;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class EntityLabelService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ENTITY_TYPE = "entity_type";
    private static final String ENTITY_INITIALS = "initials";
    private static final String DEFAULT_ENTITY_LABELS_FILE_NAME = "defaultEntityLabels.json";

    private OctaneProvider octaneProvider;
    private static  Map<Entity, EntityModel> defaultLabels = getDefaultEntityLabels();
    private Map<Entity, EntityModel> serverSideLabels;

    @Inject
    public EntityLabelService(OctaneProvider octaneProvider, ConnectionSettingsProvider connectionSettingsProvider) {
        this.octaneProvider = octaneProvider;
        connectionSettingsProvider.addChangeHandler(() -> serverSideLabels = null);
    }

    public Map<Entity, EntityModel> getEntityLabelDetails() {

        if(serverSideLabels == null || serverSideLabels.isEmpty()) {

            try {
                serverSideLabels = getEntityLabelsFromServer();

            } catch (Exception ex) {
                logger.warn("Failed to get labels from the server, using defaults: " + ex);
                serverSideLabels = new HashMap<>();
            }
        }

        final Map<Entity, EntityModel> resultMap = new HashMap<>(serverSideLabels);

        defaultLabels.forEach((entity, entityModel) -> {
            if(resultMap.putIfAbsent(entity, entityModel) == null) {
                logger.trace("Adding default entity label details for " + entity + " since the server side one could not be determined.");
            }
        });

        return resultMap;
    }

    public boolean areServerSideLabelsLoaded() {
        return serverSideLabels != null && !serverSideLabels.isEmpty();
    }

    public String getDefaultEntityInitials(Entity entity) {
        return defaultLabels.get(entity).getValue(ENTITY_INITIALS).getValue().toString();
    }

    public String getEntityInitials(Entity entity) {
        return getEntityLabelDetails().get(entity).getValue(ENTITY_INITIALS).getValue().toString();
    }

    private Map<Entity, EntityModel> getEntityLabelsFromServer() {
        OctaneCollection<EntityModel> labelEntityModels =
                octaneProvider.getOctane()
                        .entityList("entity_labels")
                        .get()
                        .query(Query.statement("language", QueryMethod.EqualTo, "lang.en").build())
                        .execute();

        return convertToMap(labelEntityModels);
    }

    private static Map<Entity, EntityModel> getDefaultEntityLabels() {
        try {
            ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader();
            InputStream input = classpathResourceLoader.getResourceStream(DEFAULT_ENTITY_LABELS_FILE_NAME);
            String jsonString = CharStreams.toString(new InputStreamReader(input, Charsets.UTF_8));

            return convertToMap(ModelParser.getInstance().getEntities(jsonString));

        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to parse " + DEFAULT_ENTITY_LABELS_FILE_NAME + " file ", e);
        }
    }

    private static Map<Entity, EntityModel> convertToMap(OctaneCollection<EntityModel> entityModels) {
        Map<Entity, EntityModel> map = new HashMap<>();

        entityModels.forEach(entityModel -> {
            Entity entityType = Entity.getEntityType(entityModel.getValue(ENTITY_TYPE).getValue().toString());

            // Octane bug, the label is saved for REQUIREMENT_ROOT, not REQUIREMENT or even all of them
            entityType = entityType == Entity.REQUIREMENT_ROOT ? Entity.REQUIREMENT : entityType;

            if(entityType != null) {
                map.put(entityType, entityModel);
            } else {
                logger.debug("Unknown entity_type string: "
                        + entityModel.getValue(ENTITY_TYPE).getValue().toString()
                        + " . will be ignored");
            }
        });

        return map;
    }

}