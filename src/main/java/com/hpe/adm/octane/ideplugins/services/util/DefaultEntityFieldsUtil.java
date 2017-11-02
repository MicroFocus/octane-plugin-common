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
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/**
 * Used by plugins to determine what fields to display in the detail view
 * Provides util methods for configuring and saving detail view fields config
 */
public class DefaultEntityFieldsUtil {

    /**
     * Used for versioning entity fields json
     */
    public static final long CURRENT_ENTITY_FIELDS_JSON_VERSION = 1;

    public static Map<Entity, Set<String>> getDefaultFields(){
        //TODO
        //USE entityFieldsFromJson method below
        return null;
    }

    /**
     * Util method for converting an entity fields json to a java object
     * Reads based on version tag in json object, current is CURRENT_ENTITY_FIELDS_JSON_VERSION
     * @param jsonObject json containing fields for entities
     * @return map containing {@link Entity} to field {@link Set}
     */
    public static Map<Entity, Set<String>> entityFieldsFromJson(JSONObject jsonObject){
        //TODO
        //USE CURRENT_ENTITY_FIELDS_JSON_VERSION when parsing
        return null;
    }

    /**
     * Util method for converting a map containing {@link Entity} to field {@link Set} to JSON
     * Adds a version tag to the json object, current is CURRENT_ENTITY_FIELDS_JSON_VERSION
     * @param map
     * @return
     */
    public static JSONObject entityFieldsToJson(Map<Entity, Set<String>> map){
        //TODO
        //USE CURRENT_ENTITY_FIELDS_JSON_VERSION when creating
        return null;
    }

}