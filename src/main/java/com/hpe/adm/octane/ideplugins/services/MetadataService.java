/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.ui.FormLayout;
import com.hpe.adm.octane.ideplugins.services.util.OctaneSystemDefaultForms;
import com.hpe.adm.octane.ideplugins.services.util.OctaneUrlBuilder;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import org.apache.http.client.utils.URIBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hpe.adm.octane.ideplugins.services.util.Util.createQueryForMultipleValues;

public class MetadataService {

    @Inject
    protected HttpClientProvider httpClientProvider;
    @Inject
    private OctaneProvider octaneProvider;
    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;
    private Map<Entity, Collection<FieldMetadata>> cache;
    private Map<Entity, FormLayout> octaneFormsCache;

    public Set<String> getFields(Entity entityType) {
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            init();
        }
        Octane octane = octaneProvider.getOctane();
        Collection<FieldMetadata> fields;
        if (!cache.containsKey(entityType)) {
            fields = octane.metadata().fields(entityType.getEntityName()).execute();
            cache.put(entityType, fields);
        } else {
            fields = cache.get(entityType);
        }
        return fields.stream().map(FieldMetadata::getName).collect(Collectors.toSet());
    }

    public boolean hasFields(Entity entityType, String... fieldNames) {
        Set<String> responseFieldNames = getFields(entityType);

        return Arrays.stream(fieldNames)
                .allMatch(responseFieldNames::contains);
    }

    public void eagerInit(Entity... entities) {
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            init();
        }

        Octane octane = octaneProvider.getOctane();

        Arrays.stream(entities)
                .parallel()
                .forEach(entityType -> cache.put(entityType, octane.metadata().fields(entityType.getEntityName()).execute()));
    }

    private void init() {
        cache = new ConcurrentHashMap<>();
        connectionSettingsProvider.addChangeHandler(() -> cache.clear());
    }

    public Map<Entity, FormLayout> getFormLayoutForAllEntityTypes() throws UnsupportedEncodingException {
        if (null == octaneFormsCache) {
            connectionSettingsProvider.addChangeHandler(() -> octaneFormsCache.clear());
            octaneFormsCache = retrieveFormsFromOctane();
        }
        if (octaneFormsCache.isEmpty()) {
            octaneFormsCache = retrieveFormsFromOctane();
        }
        return octaneFormsCache;
    }

    public FormLayout getFormLayoutForSpecificEntityType(Entity entityType) throws UnsupportedEncodingException {
        FormLayout entityOctaneForm = getFormLayoutForAllEntityTypes().get(entityType);
        if (null == entityOctaneForm) {
            entityOctaneForm = getSystemDefinedFormsForEntity(entityType);
        }
        return entityOctaneForm;
    }

    private Map<Entity, FormLayout> retrieveFormsFromOctane() throws UnsupportedEncodingException {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.geOctaneHttpClient();
        if (null == httpClient) {
            throw new ServiceRuntimeException("Failed to authenticate with current connection settings");
        }
        OctaneHttpResponse response;
        URIBuilder uriBuilder = OctaneUrlBuilder.buildOctaneUri(connectionSettings, "form_layouts");
        uriBuilder.setParameter("query", createQueryForMultipleValues("entity_type", Arrays.asList(
                "run", "defect", "quality_story",
                "epic", "story", "run_suite",
                "run_manual", "run_automated", "test",
                "test_automated", "test_suite", "gherkin_test",
                "test_manual", "work_item", "user_tag", "task")));

        OctaneHttpRequest request = null;
        try {
            request = new OctaneHttpRequest.GetOctaneHttpRequest(uriBuilder.build().toASCIIString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        response = httpClient.execute(request);
        List<FormLayout> formList = new ArrayList<>();
        if (response.isSuccessStatusCode()) {
            formList = Util.parseJsonWithFormLayoutData(response.getContent());
        }

        return formList
                .stream()
                .filter(FormLayout::isDefault)
                .collect(Collectors.toMap(FormLayout::getEntity, Function.identity()));
    }

    private FormLayout getSystemDefinedFormsForEntity(Entity entityType) {
        Map<Entity, FormLayout> formsMap;
        List<FormLayout> formList = Util.parseJsonWithFormLayoutData(OctaneSystemDefaultForms.ALL);
        formsMap = formList
                .stream()
                .filter(FormLayout::isDefault)
                .collect(Collectors.toMap(FormLayout::getEntity, Function.identity()));
        return formsMap.get(entityType);
    }
}