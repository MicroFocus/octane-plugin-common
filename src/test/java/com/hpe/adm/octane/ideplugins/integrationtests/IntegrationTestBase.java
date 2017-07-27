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

package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.api.client.json.Json;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.OctaneRequest;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.ideplugins.integrationtests.util.PropertyUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkSpace;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.net.www.http.HttpClient;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.util.Collection;

/**
 * Enables the use of the {@link Inject} annotation
 */
@Deprecated
public abstract class IntegrationTestBase {

    protected EntityGenerator entityGenerator;
    ConnectionSettingsProvider connectionSettings;

    @Before
    public void setup() {

        connectionSettings = PropertyUtil.readFormVmArgs() != null ?
                PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();

        Annotation[] ants = this.getClass().getDeclaredAnnotations();


        for (Annotation annotation : ants) {
            if (annotation.toString().contains("WorkSpace(clean=")) {
                if (annotation.toString().contains("true")) {
                    //create a new workspace
                    ConnectionSettings cs = connectionSettings.getConnectionSettings();
                    cs.setWorkspaceId(createWorkSpace());
                    connectionSettings.setConnectionSettings(cs);
                    break;
                }
            }
        }


        if (connectionSettings == null) {
            throw new RuntimeException("Cannot retrieve connection settings from either vm args or prop file, cannot run tests");
        }

        Injector injector = Guice.createInjector(new ServiceModule(connectionSettings));
        injector.injectMembers(this);
        entityGenerator = new EntityGenerator(injector.getInstance(OctaneProvider.class));
    }

    public Long createWorkSpace() {

        String postUrl = connectionSettings.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettings.getConnectionSettings().getSharedSpaceId() + "/workspaces";

        String urlDomain = connectionSettings.getConnectionSettings().getBaseUrl();

        JSONObject dataSet = new JSONObject();
        JSONObject credentialsJson = new JSONObject();
        credentialsJson.put("name", "test_workspace1");
        credentialsJson.put("description", "Created from intellij");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(credentialsJson);
        dataSet.put("data", jsonArray);

        System.out.println(dataSet.toString());

        OctaneHttpRequest postNewWorkspaceRequest = new OctaneHttpRequest.PostOctaneHttpRequest(postUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, dataSet.toString());
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettings.getConnectionSettings().getUserName(), connectionSettings.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;

        try {
            response = octaneHttpClient.execute(postNewWorkspaceRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        octaneHttpClient.signOut();

        return responseJson.getLong("id");
    }

    public void createEntity() {
        EntityService entityService = new EntityService();

        //entityGenerator.createEntityModel(new Entity());

    }


}
