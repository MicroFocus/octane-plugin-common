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

package com.hpe.adm.octane.ideplugins.services.util;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Used by plugins to determine what fields to display in the detail view
 * Provides util methods for configuring and saving detail view fields config
 */
public class DefaultEntityFieldsUtil {

    /**
     * Used for versioning entity fields json
     */
    public static final long CURRENT_ENTITY_FIELDS_JSON_VERSION = 1;

    public static Map<Entity, Set<String>> getDefaultFields() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("defaultFields.json");
        JSONTokener tokener = new JSONTokener(input);
        JSONObject data = new JSONObject(tokener);

        return entityFieldsFromJson(data);
    }

    /**
     * Util method for converting an entity fields json to a java object
     * Reads based on version tag in json object, current is CURRENT_ENTITY_FIELDS_JSON_VERSION
     *
     * @param jsonObject json containing fields for entities
     * @return map containing {@link Entity} to field {@link Set}
     */
    public static Map<Entity, Set<String>> entityFieldsFromJson(JSONObject jsonObject) {

        if (CURRENT_ENTITY_FIELDS_JSON_VERSION == 1) {
            Map<Entity, Set<String>> fieldsMap = new HashMap<>();

            JSONArray entityArray = (JSONArray) jsonObject.get("data");
            for (Object obj : entityArray) {
                JSONObject entity = (JSONObject) obj;
                JSONArray fields = (JSONArray) entity.get("fields");
                Set<String> fieldsSet = new TreeSet<>();
                for (Object obj1 : fields) {
                    String field = (String) obj1;
                    fieldsSet.add(field);
                }
                fieldsMap.put(Entity.valueOf(entity.get("type").toString()), fieldsSet);
            }
            return fieldsMap;
        }
        return null;
    }

    /**
     * Util method for converting a map containing {@link Entity} to field {@link Set} to JSON
     * Adds a version tag to the json object, current is CURRENT_ENTITY_FIELDS_JSON_VERSION
     *
     * @param map
     * @return
     */
    public static JSONObject entityFieldsToJson(Map<Entity, Set<String>> map) {

        if (CURRENT_ENTITY_FIELDS_JSON_VERSION == 1) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version",1);
            JSONArray jsonArray = new JSONArray();

            for(Entity entity : map.keySet()){
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("type",entity);
                JSONArray fieldsArray = new JSONArray();
                for(String field: map.get(entity)){
                    fieldsArray.put(field);
                }
                jsonObject1.put("fields",fieldsArray);
                jsonArray.put(jsonObject1);
            }
            jsonObject.put("data",jsonArray);

            return jsonObject;
        }
        return null;
    }

}