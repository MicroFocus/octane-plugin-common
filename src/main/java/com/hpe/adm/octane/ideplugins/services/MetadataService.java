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

import com.google.gson.Gson;
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
import com.hpe.adm.octane.ideplugins.services.util.OctaneUrlBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hpe.adm.octane.ideplugins.services.util.Util.createQueryForMultipleValues;

public class MetadataService {

    /**
     * For backwards compatibility, check if this field exists
     */
    public static final String FIELD_CLIENT_LOCK_STAMP = "client_lock_stamp";

    @Inject
    private HttpClientProvider httpClientProvider;

    @Inject
    private OctaneProvider octaneProvider;
    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private Map<Entity, Collection<FieldMetadata>> fieldsCache;

    public FieldMetadata getMetadata(Entity entityType, String fieldName) {
        Collection<FieldMetadata> allFieldMetadata = getFields(entityType);

        Optional<FieldMetadata> singleFieldMetadata =
                allFieldMetadata
                        .stream()
                        .filter(metadata -> fieldName.equals(metadata.getName()))
                        .findFirst();

        if(!singleFieldMetadata.isPresent()) {
            throw new ServiceRuntimeException("Cannot find metadata for field " + fieldName + ", entity " + entityType);
        } else {
            return singleFieldMetadata.get();
        }
    }

    public Collection<FieldMetadata> getFields(Entity entityType) {
        if (fieldsCache == null) {
            fieldsCache = new ConcurrentHashMap<>();
            connectionSettingsProvider.addChangeHandler(() -> fieldsCache.clear());
        }

        Octane octane = octaneProvider.getOctane();

        Collection<FieldMetadata> fields;

        if (!fieldsCache.containsKey(entityType)) {
            fields = octane.metadata().fields(entityType.getEntityName()).execute();
            fieldsCache.put(entityType, fields);
        } else {
            fields = fieldsCache.get(entityType);
        }

        return fields;
    }

    public boolean hasFields(Entity entityType, String... fieldNames) {
        Collection<FieldMetadata> responseFields = getFields(entityType);

        Set<String> fields = responseFields.stream().map(FieldMetadata::getName).collect(Collectors.toSet());

        return Arrays.stream(fieldNames)
                .allMatch(fields::contains);
    }

    public Collection<FieldMetadata> getVisibleFields(Entity entityType){
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();
        if (null == httpClient) {
            throw new ServiceRuntimeException("Failed to authenticate with current connection settings");
        }
        OctaneHttpResponse response;
        URIBuilder uriBuilder = OctaneUrlBuilder.buildOctaneUri(connectionSettings, "metadata/fields");
        uriBuilder.setParameters(new BasicNameValuePair("query", createQueryForMultipleValues("entity_name", entityType.getEntityName())));

        OctaneHttpRequest request;
        try {
            request = new OctaneHttpRequest.GetOctaneHttpRequest(uriBuilder.build().toASCIIString());
        } catch (URISyntaxException e) {
            throw new ServiceRuntimeException(e);
        }
        List<FieldMetadata> fields;

        response = httpClient.execute(request);
        fields = (List<FieldMetadata>) getFieldMetadataFromJSON(response.getContent());
        return fields;
    }

    private Collection<FieldMetadata> getFieldMetadataFromJSON(String fieldsJSON){
        JSONTokener tokener = new JSONTokener(fieldsJSON);
        JSONObject jsonObj = new JSONObject(tokener);
        JSONArray jsonDataArr = jsonObj.getJSONArray("data");
        List<FieldMetadata> fieldsMetadata = new ArrayList<>();
        IntStream.range(0, jsonDataArr.length()).forEach((i) -> {
            JSONObject obj = jsonDataArr.getJSONObject(i);
            if(obj.getBoolean("visible_in_ui"))
                fieldsMetadata.add((new Gson()).fromJson(obj.toString(), FieldMetadata.class));
        });
        return fieldsMetadata;
    }

    /**
     * Check if server configured in connection settings has client lock stamp supports
     * It checks if FIELD_CLIENT_LOCK_STAMP exists in the field metadata
     * Used for backwards compatibility
     * @param entityType type of entity to check if lock stamp is supported on
     * @return true if entity has field, false othwerwise
     */
    public boolean hasClientLockStampSupport(Entity entityType) {
        return getFields(entityType)
                .stream()
                .map(fieldMetadata -> fieldMetadata.getName())
                .anyMatch(fieldName -> FIELD_CLIENT_LOCK_STAMP.equals(fieldName));
    }

    public void eagerInit(Entity... entities) {
        if (fieldsCache == null) {
            fieldsCache = new ConcurrentHashMap<>();
            connectionSettingsProvider.addChangeHandler(() -> fieldsCache.clear());
        }

        Octane octane = octaneProvider.getOctane();

        Arrays.stream(entities)
                .parallel()
                .forEach(entityType -> fieldsCache.put(entityType, octane.metadata().fields(entityType.getEntityName()).execute()));
    }

}