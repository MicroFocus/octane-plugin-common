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
package com.hpe.adm.octane.ideplugins.services.util;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Used by plugins to determine what fields to display in the detail view
 * Provides util methods for configuring and saving detail view fields config
 */
public class DefaultEntityFieldsUtil {

    /**
     * Used for versioning entity fields json
     */
    public static final long CURRENT_ENTITY_FIELDS_JSON_VERSION = 1;

    public static final String DEFAULT_FIELDS_FILE_NAME = "defaultFields.json";

    public static Map<Entity, Set<String>> getDefaultFields() {
        try (InputStream input = DefaultEntityFieldsUtil.class.getResourceAsStream("/" + DEFAULT_FIELDS_FILE_NAME)) {

            String jsonString = CharStreams.toString(new InputStreamReader(input, Charsets.UTF_8));

            return entityFieldsFromJson(jsonString);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to parse " + DEFAULT_FIELDS_FILE_NAME + " file ", e);
        }
    }

    /**
     * Util method for converting an entity fields json to a java object Reads
     * based on version tag in json object, current is
     * CURRENT_ENTITY_FIELDS_JSON_VERSION
     *
     * @param jsonString json containing fields for entities
     * @return map containing {@link Entity} to field {@link Set}
     */
    public static Map<Entity, Set<String>> entityFieldsFromJson(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        Long jsonVersion = jsonObject.getLong("version");

        if (CURRENT_ENTITY_FIELDS_JSON_VERSION == jsonVersion) {
            Map<Entity, Set<String>> fieldsMap = new LinkedHashMap<>();

            JSONArray entityArray = jsonObject.getJSONArray("data");

            for (int i = 0; i < entityArray.length(); i++) {

                Set<String> fieldsSet = new LinkedHashSet<>();

                JSONObject entity = entityArray.getJSONObject(i);
                JSONArray fields = entity.getJSONArray("fields");

                fields.forEach(field -> {
                    fieldsSet.add(((JSONObject) field).getString("name"));
                });

                fieldsMap.put(Entity.valueOf(entity.getString("type")), fieldsSet);
            }

            return fieldsMap;
        } else {
            throw new ServiceRuntimeException("Fields json version usupported, cannot parse");
        }
    }

    /**
     * Util method for converting a map containing {@link Entity} to field
     * {@link Set} to JSON Adds a version tag to the json object, current is
     * CURRENT_ENTITY_FIELDS_JSON_VERSION
     *
     * @param map of fields
     * @return json object from fields
     */
    public static String entityFieldsToJson(Map<Entity, Set<String>> map) {

        JSONObject jsonObjectRoot = new JSONObject();
        jsonObjectRoot.put("version", CURRENT_ENTITY_FIELDS_JSON_VERSION);
        JSONArray jsonArrayEntities = new JSONArray();

        for (Entity entity : map.keySet()) {

            JSONObject jsonObjectFields = new JSONObject();
            jsonObjectFields.put("type", entity);

            List<JSONObject> fieldsArray = new ArrayList<>();
            map.get(entity).forEach(fieldname -> fieldsArray.add(new JSONObject("{\"name\":\"" + fieldname + "\"}")));
            jsonObjectFields.put("fields", fieldsArray);

            jsonArrayEntities.put(jsonObjectFields);
        }

        jsonObjectRoot.put("data", jsonArrayEntities);
        return jsonObjectRoot.toString();
    }

}