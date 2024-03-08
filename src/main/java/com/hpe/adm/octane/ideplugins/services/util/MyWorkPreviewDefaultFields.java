/*******************************************************************************
 * Copyright 2017-2023 Open Text.
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Used by plugins to determine what fields to display in the MyWork view
 * It is used for ALM Octane servers with version greater than 16.0.208 (Iron Maiden P1)
 */
public class MyWorkPreviewDefaultFields {

    public static final String MYWORK_PREVIEW_DEFAULT_FIELDS_FILE_NAME = "myWorkPreviewDefaultFields.json";

    public static Map<Entity, Set<String>> getDefaultFields() {
        try {
            InputStream input = MyWorkPreviewDefaultFields.class.getResourceAsStream("/" + MYWORK_PREVIEW_DEFAULT_FIELDS_FILE_NAME);
            String jsonString = CharStreams.toString(new InputStreamReader(input, Charsets.UTF_8));

            return convertEntityFieldsFromJsonToObjects(jsonString);
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
    public static Map<Entity, Set<String>> convertEntityFieldsFromJsonToObjects(String jsonString) {
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