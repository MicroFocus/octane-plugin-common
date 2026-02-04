/*******************************************************************************
 * Copyright 2017-2026 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.LongFieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.filtering.PredefinedEntityComparator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntitySearchService {

    private static final Logger logger = LoggerFactory.getLogger(EntitySearchService.class.getName());

    private static final String JSON_DATA_NAME = "data";
    private static final String GLOBAL_TEXT_SEARCH_RESULT_TAG = "global_text_search_result";

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Inject
    private HttpClientProvider httpClientProvider;

    public Collection<EntityModel> searchGlobal(String queryString, int limit, Entity... entity) {
        queryString = StringEscapeUtils.escapeJavaScript(queryString);
        //javascript style escapes quote ' with \\' we need to reverse this side effect
        String escapedQueryString = queryString.replace("\\'", "'");
        Map<Entity, Collection<EntityModel>> result = new ConcurrentHashMap<>();

        // Doing the httpClientProvider.getOctaneHttpClient() will make the login execute outside of the parallel threads
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();
        Arrays
                .stream(entity)
                .parallel()
                .forEach(entityType -> result.put(entityType, searchGlobal(escapedQueryString, limit, entityType, httpClient)));

        return result
                .keySet()
                .stream()
                .sorted(PredefinedEntityComparator.instance)
                .flatMap(key -> result.get(key).stream())
                .collect(Collectors.toList());

    }


    public Collection<EntityModel> searchGlobal(String queryString, int limit, Entity entity) {
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();
        return searchGlobal(queryString, limit, entity, httpClient);
    }

    private Collection<EntityModel> searchGlobal(String queryString, int limit, Entity entity, OctaneHttpClient httpClient) {

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        URIBuilder uriBuilder = new URIBuilder();
        URL baseUrl;
        try {
            baseUrl = new URL(connectionSettings.getBaseUrl());
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException("Cannot parse base url from connection settings: " + e);
        }

        uriBuilder.setScheme(baseUrl.getProtocol());
        uriBuilder.setHost(baseUrl.getHost());
        uriBuilder.setPort(baseUrl.getPort());

        String uriBuilderPath = "";
        if (!baseUrl.getPath().equals("/"))
            uriBuilderPath += baseUrl.getPath();

        uriBuilderPath += "/api"
                + "/shared_spaces/" + connectionSettings.getSharedSpaceId()
                + "/workspaces/" + connectionSettings.getWorkspaceId()
                + "/" + entity.getApiEntityName();

        uriBuilder.setPath(uriBuilderPath);

        uriBuilder.setParameter("text_search", "{\"type\":\"global\",\"text\":\""+queryString+"\"}");
        uriBuilder.setParameter("limit", limit + "");
        uriBuilder.setParameter("order_by","id");

        if(entity.isSubtype()) {
            uriBuilder.setParameter("query", "\"((subtype='"+entity.getSubtypeName()+"'))\"");
        }

        try {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest( uriBuilder.build().toASCIIString());
            OctaneHttpResponse response = httpClient.execute(request);
            String responseString = response.getContent();

            if(response.isSuccessStatusCode() && StringUtils.isNotBlank(responseString)){
                return searchResponseToEntityModels(responseString);
            } else {
                throw new ServiceRuntimeException("Failed to get search response JSON");
            }

        } catch (Exception ex) {
            //Team edition makes some of the entity types unreadable
            //todo: metadata based approach currently doesn't seem possible, should be done in the future
            if(ex instanceof OctaneException) {
                OctaneException octaneException = (OctaneException) ex;
                LongFieldModel httpErrorCode = (LongFieldModel) octaneException.getError().getValue("http_status_code");

                if(httpErrorCode != null && httpErrorCode.getValue() == 403L){
                    logger.warn("403 when searching " + entity + ", but exception was ignored: " + ex);
                    throw octaneException;
                }
            }

            throw new ServiceRuntimeException(ex);
        }
    }

    /**
     * Convert it to standard entity model for re-using exiting UI,
     * only has: id, name, type/subtype, description
     * @param responseString
     * @return
     */
    private Collection<EntityModel> searchResponseToEntityModels(String responseString){

        Collection<EntityModel> result = new ArrayList<>();

        JSONObject json = new JSONObject(responseString);
        JSONArray data = json.getJSONArray(JSON_DATA_NAME);

        data.forEach(jsonObj -> {
            JSONObject jsonObject = (JSONObject) jsonObj;

            //Create an entity model from the json, the json format is fixed
            String name = getStringOrBlank(jsonObject.getJSONObject(GLOBAL_TEXT_SEARCH_RESULT_TAG), "name" );
            String description = getStringOrBlank(jsonObject.getJSONObject(GLOBAL_TEXT_SEARCH_RESULT_TAG), "description" );
            String id = getStringOrBlank(jsonObject, "id" );
            String type = getStringOrBlank(jsonObject, "type" );
            String subtype = getStringOrBlank(jsonObject, "subtype" );

            EntityModel entityModel = new EntityModel();
            entityModel.setValue(new StringFieldModel("name", name));
            entityModel.setValue(new StringFieldModel("description", description));
            entityModel.setValue(new StringFieldModel("id", id));
            entityModel.setValue(new StringFieldModel("type", type));
            entityModel.setValue(new StringFieldModel("subtype", subtype));

            result.add(entityModel);
        });

        return result;
    }

    private static String getStringOrBlank(JSONObject jsonObject, String key){
        if(jsonObject.has(key) && !jsonObject.isNull(key)){
            return jsonObject.getString(key);
        } else {
            return "";
        }
    }

}