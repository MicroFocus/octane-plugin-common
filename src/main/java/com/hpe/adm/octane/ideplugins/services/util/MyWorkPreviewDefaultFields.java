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
package com.hpe.adm.octane.ideplugins.services.util;

import com.google.api.client.util.Charsets;
import com.google.common.io.CharStreams;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Used by plugins to determine what fields to display in the MyWork view
 * It is used for ALM Octane servers with version greater than 16.0.208 (Iron Maiden P1)
 */
public class MyWorkPreviewDefaultFields {

    public static final String MYWORK_PREVIEW_DEFAULT_FIELDS_FILE_NAME = "myWorkPreviewDefaultFields.json";

    public static Map<Entity, Set<String>> getDefaultFields() {
        try {
            ClasspathResourceLoader cprl = new ClasspathResourceLoader();
            InputStream input = cprl.getResourceStream(MYWORK_PREVIEW_DEFAULT_FIELDS_FILE_NAME);
            String jsonString = CharStreams.toString(new InputStreamReader(input, Charsets.UTF_8));

            return entityFieldsFromJson(jsonString);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to parse " + MYWORK_PREVIEW_DEFAULT_FIELDS_FILE_NAME + " file ", e);
        }
    }

    /**
     * Util method for converting an entity fields json to a java object
     *
     * @param jsonString json containing fields for entities
     * @return map containing {@link Entity} to field {@link Set}
     */
    public static Map<Entity, Set<String>> entityFieldsFromJson(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        Map<Entity, Set<String>> fieldsMap = new LinkedHashMap<>();

        JSONArray entityArray = jsonObject.getJSONArray("data");

        for (int i = 0; i < entityArray.length(); i++) {
            Set<String> fieldsSet = new LinkedHashSet<>();

            JSONObject entity = entityArray.getJSONObject(i);
            JSONArray fields = entity.getJSONArray("fields");

            fields.forEach(field -> {
                fieldsSet.add(field.toString());
            });

            fieldsMap.put(Entity.valueOf(entity.getString("type")), fieldsSet);
        }

        return fieldsMap;
    }
}