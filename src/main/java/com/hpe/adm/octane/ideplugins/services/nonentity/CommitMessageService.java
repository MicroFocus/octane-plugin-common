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
package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommitMessageService {

    private static final Logger logger = LoggerFactory.getLogger(CommitMessageService.class);
    private static final JsonParser JSON_PARSER = new JsonParser();

    @Inject
    protected ConnectionSettingsProvider connectionSettingsProvider;
    @Inject
    protected HttpClientProvider httpClientProvider;
    @Inject
    private EntityService entityService;

    public boolean validateCommitMessage(String commitMessage, Entity entityType, long entityId) {

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();

        if (null != httpClient) {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(
                    connectionSettings.getBaseUrl() +
                            "/internal-api/shared_spaces/" + connectionSettings.getSharedSpaceId() +
                            "/workspaces/" + connectionSettings.getWorkspaceId() +
                            "/ali/validateCommitPattern?comment=" + UrlParser.urlEncodeQueryParamValue(commitMessage)
            );

            OctaneHttpResponse response = httpClient.execute(request);
            String jsonString = response.getContent();

            try {
                JsonArray matchedIdsArray =
                        JSON_PARSER
                                .parse(jsonString)
                                .getAsJsonObject()
                                .get(entityType.getSubtypeName())
                                .getAsJsonArray();

                for (JsonElement element : matchedIdsArray) {
                    if (element.getAsLong() == entityId) {
                        return true;
                    }
                }
            } catch (JsonSyntaxException ex) {
                logger.error("Failed to parse response json: " + ex);
                return false;
            }
        }
        return false;
    }

    public List<String> getCommitPatternsForStoryType(Entity entityType) {
        String type;
        switch (entityType) {
            case DEFECT:
                type = "Defect";
                break;
            case USER_STORY:
                type = "User story";
                break;
            case QUALITY_STORY:
                type = "Quality story";
                break;
            default:
                return null;
        }

        List<String> commitPatterns = new ArrayList<>();

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.getOctaneHttpClient();

        if (null != httpClient) {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(
                    connectionSettings.getBaseUrl() +
                            "/api/shared_spaces/" + connectionSettings.getSharedSpaceId() +
                            "/workspaces/" + connectionSettings.getWorkspaceId() + "/scm_commit_patterns");

            OctaneHttpResponse response = httpClient.execute(request);
            String jsonString = response.getContent();

            JsonArray dataArray = JSON_PARSER.parse(jsonString)
                    .getAsJsonObject()
                    .get("data")
                    .getAsJsonArray();

            for (JsonElement elem : dataArray) {
                String name = elem.getAsJsonObject().get("entity_type").getAsJsonObject().get("name").getAsString();
                if (name.equals(type)) {
                    commitPatterns.add(elem.getAsJsonObject().get("pattern").getAsString());
                }
            }

            return commitPatterns;
        }

        return null;
    }

    public String generateLocalCommitMessage(EntityModel entityModel) {

        String taskString = "";

        if (Entity.getEntityType(entityModel) == Entity.TASK) {
            taskString = ": task #" + entityModel.getId();

            entityModel = addReferenceFieldIfNeeded(entityModel);
            entityModel = (EntityModel) entityModel.getValue("story").getValue();
        }

        StringBuilder messageBuilder = new StringBuilder();

        String id = entityModel.getId();
        Entity type = Entity.getEntityType(entityModel);

        switch (type) {
            case USER_STORY:
                messageBuilder.append("user story #");
                break;
            case QUALITY_STORY:
                messageBuilder.append("quality story #");
                break;
            case DEFECT:
                messageBuilder.append("defect #");
                break;
        }

        messageBuilder.append(id);
        messageBuilder.append(taskString);

        return messageBuilder.toString();
    }

    private EntityModel addReferenceFieldIfNeeded(EntityModel entityModel) {
        if (Entity.getEntityType(entityModel) == Entity.TASK && entityModel.getValue("story") == null) {
            EntityModel taskParent = getTaskParent(entityModel.getId());
            entityModel.setValue(new ReferenceFieldModel("story", taskParent));
        }
        return entityModel;
    }

    private EntityModel getTaskParent(String id) {
        Set<String> storyField = new HashSet<>(Collections.singletonList("story"));
        Query.QueryBuilder idQuery = Query.statement("id", QueryMethod.EqualTo, id);
        Collection<EntityModel> results = entityService.findEntities(Entity.TASK, idQuery, storyField);

        if (results.size() == 1) {
            return (EntityModel) results.iterator().next().getValue("story").getValue();
        } else {
            return null;
        }
    }

}