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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hpe.adm.octane.ideplugins.services.util.Util.createQueryForMultipleValues;

public class MetadataService {

    @Inject
    protected HttpClientProvider httpClientProvider;
    @Inject
    private OctaneProvider octaneProvider;
    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;


    private Map<Entity, Collection<FieldMetadata>> cache;

    public Collection<FieldMetadata> getFields(Entity entityType) {
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
        return fields;
    }

    public Collection<FieldMetadata> getVisibleFields(Entity entityType){
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            init();
        }
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.geOctaneHttpClient();
        if (null == httpClient) {
            throw new ServiceRuntimeException("Failed to authenticate with current connection settings");
        }
        OctaneHttpResponse response;
        URIBuilder uriBuilder = OctaneUrlBuilder.buildOctaneUri(connectionSettings, "metadata/fields");
        try {
            uriBuilder.setParameters(
                    new BasicNameValuePair("query",
                            createQueryForMultipleValues("entity_name", entityType.getEntityName())));
        } catch (UnsupportedEncodingException e) {
            throw new ServiceRuntimeException(e);
        }

        OctaneHttpRequest request = null;
        try {
            request = new OctaneHttpRequest.GetOctaneHttpRequest(uriBuilder.build().toASCIIString());
        } catch (URISyntaxException e) {
            throw new ServiceRuntimeException(e);
        }
        response = httpClient.execute(request);
        List<FieldMetadata> fields = new ArrayList<>();
        if (!cache.containsKey(entityType)) {
            fields = (List<FieldMetadata>) getFieldMetadataFromJSON(response.getContent());
            cache.put(entityType, fields);
        } else {
            fields = (List<FieldMetadata>) cache.get(entityType);
        }
        return fields;
    }
    
    private Collection<FieldMetadata> getFieldMetadataFromJSON(String fieldsJSON){
        JSONTokener tokener = new JSONTokener(fieldsJSON);
        JSONObject jsonObj = new JSONObject(tokener);
        JSONArray jsonDataArr = jsonObj.getJSONArray("data");
        Collection<FieldMetadata> fieldsMetadata = new ArrayList();
        IntStream.range(0, jsonDataArr.length()).forEach((i) -> {
            JSONObject obj = jsonDataArr.getJSONObject(i);
            if(obj.getBoolean("visible_in_ui"))
                fieldsMetadata.add((new Gson()).fromJson(obj.toString(), FieldMetadata.class));
        });
        return fieldsMetadata;
    }
    public boolean hasFields(Entity entityType, String... fieldNames) {
        Collection<FieldMetadata> responseFields = getFields(entityType);

        Set<String> fields = responseFields.stream().map(FieldMetadata::getName).collect(Collectors.toSet());

        return Arrays.stream(fieldNames)
                .allMatch(fields::contains);
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

}