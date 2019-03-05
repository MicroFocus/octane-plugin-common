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
package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.integrationtests.TestServiceModule;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityUtils;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class GherkinTestDownloadITCase {

    @Inject
    private EntityUtils entityUtils;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Inject
    protected HttpClientProvider httpClientProvider;

    @Before
    public void setUp() {
        ServiceModule serviceModule = TestServiceModule.getServiceModule();
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
    }

    private EntityModel createGherkinTestWithScript(UUID uuid) {
        EntityModel gherkinTest = entityUtils.createEntity(Entity.GHERKIN_TEST);

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();

        String putUrl = connectionSettings.getBaseUrl() + "/api/shared_spaces/" +
                connectionSettings.getSharedSpaceId() + "/workspaces/" +
                connectionSettings.getWorkspaceId() + "/tests/" +
                gherkinTest.getValue("id").getValue() + "/script";

        JSONObject script = new JSONObject();
        script.put("comment", "");
        script.put("revision_type", "Minor");
        script.put("script", uuid);

        OctaneHttpRequest updateScriptRequest = new OctaneHttpRequest.PutOctaneHttpRequest(putUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, script.toString());
        try {
            httpClientProvider.getOctaneHttpClient().execute(updateScriptRequest);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }

        return gherkinTest;
    }

    private String getGherkinScript(EntityModel gherkinTest) {

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        String getUrl = connectionSettings.getBaseUrl() + "/api/shared_spaces/" +
                connectionSettings.getSharedSpaceId() + "/workspaces/" +
                connectionSettings.getWorkspaceId() + "/tests/" +
                gherkinTest.getValue("id").getValue() + "/script";

        OctaneHttpRequest updateScriptRequest = new OctaneHttpRequest.GetOctaneHttpRequest(getUrl);

        OctaneHttpResponse response = null;
        try {
            response = httpClientProvider.getOctaneHttpClient().execute(updateScriptRequest);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }

        JSONObject responseJson = new JSONObject(response.getContent());
        return responseJson.get("script").toString();
    }

    @Test
    public void testGherkinTestScriptDownload() {
        UUID uuid = UUID.randomUUID();
        EntityModel gherkinTest = createGherkinTestWithScript(uuid);
        String script = getGherkinScript(gherkinTest);
        assert script.contains(uuid.toString());
    }
}
