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
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.OctaneRequest;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.ideplugins.integrationtests.util.PropertyUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.util.User;
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
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.net.www.http.HttpClient;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Enables the use of the {@link Inject} annotation
 */

public abstract class IntegrationTestBase {


    private final Logger logger = LogManager.getLogger(IntegrationTestBase.class.getName().toString());

    protected EntityGenerator entityGenerator;

    ConnectionSettingsProvider connectionSettings;

    ServiceModule serviceModule;


    /**
     * This function will set up a context needed for the tests, the context is derived from the annotations set the
     * implementing class
     */
    @Before
    public void setup() {

        Annotation[] annotations = this.getClass().getDeclaredAnnotations();

        WorkSpace workSpaceAnnotation = getAnnotation(annotations, WorkSpace.class);

        if (workSpaceAnnotation.clean()) {
            connectionSettings = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
            ConnectionSettings cs = connectionSettings.getConnectionSettings();
            cs.setWorkspaceId(createWorkSpace());
            connectionSettings.setConnectionSettings(cs);
        } else {

            connectionSettings = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
            ConnectionSettings cs = connectionSettings.getConnectionSettings();
            long defaultWorkspaceId = getDefaultWorkspaceId();

            if (defaultWorkspaceId > 0)
                cs.setWorkspaceId(defaultWorkspaceId);
            else {
                cs.setWorkspaceId(createWorkSpace());
            }
            connectionSettings.setConnectionSettings(cs);
        }

        if (connectionSettings == null) {
            throw new RuntimeException("Cannot retrieve connection settings from either vm args or prop file, cannot run tests");
        }

        serviceModule = new ServiceModule(connectionSettings);

        User userAnnotation = getAnnotation(annotations, User.class);

        if (userAnnotation.create()) {
            createNewUser();
        }



        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
        entityGenerator = new EntityGenerator(injector.getInstance(OctaneProvider.class));

    }


    /**
     * * This method will look for an annotation in the implementing subclass
     *
     * @param annotations     - annotations of the implementing subclass
     * @param annotationClass - annotation type to look for
     * @param <A>             - generic annotation class
     * @return the instance of that annotation or null in case it isn't found
     */
    public <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                return (A) annotation;
            }
        }
        return null;
    }

    /**
     * This method will create a new workspace
     *
     * @return workspace_id
     */
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
            logger.error("Error while trying to get the response when creating a new workspace!");
            fail(e.toString());
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        octaneHttpClient.signOut();

        return responseJson.getLong("id");
    }

    /**
     * This method will look for workspaces
     *
     * @return the workspace_id of the first workspace obtained, -1 if no workspace is found
     */
    public long getDefaultWorkspaceId() {
        String postUrl = connectionSettings.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettings.getConnectionSettings().getSharedSpaceId() + "/workspaces";

        String urlDomain = connectionSettings.getConnectionSettings().getBaseUrl();

        OctaneHttpRequest getAllWorkspacesRequest = new OctaneHttpRequest.GetOctaneHttpRequest(postUrl);
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettings.getConnectionSettings().getUserName(), connectionSettings.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;

        try {
            response = octaneHttpClient.execute(getAllWorkspacesRequest);
        } catch (Exception e) {
            logger.debug("Exception while trying to get all the workspaces");
            fail(e.toString());
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        JSONArray workspaces = responseJson.getJSONArray("data");
        if (workspaces.length() == 0)
            return -1;

        return ((JSONObject) workspaces.get(0)).getLong("id");
    }

    public void createEntity(Entity entity) {

        OctaneProvider octaneProvider = serviceModule.getOctane();
        EntityGenerator entityGenerator = new EntityGenerator(octaneProvider);

        EntityModel entityModel = entityGenerator.createEntityModel(entity);

        Octane octane = octaneProvider.getOctane();

        octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(entityModel));
    }

    public void deleteEntity(Entity entity){
        OctaneProvider octaneProvider = serviceModule.getOctane();
        EntityGenerator entityGenerator = new EntityGenerator(octaneProvider);

        EntityModel entityModel = entityGenerator.createEntityModel(entity);

        entityGenerator.deleteEntityModel(entityModel);
    }

    public void deleteEntity(EntityModel entityModel) {
        entityGenerator.deleteEntityModel(entityModel);
    }


    public void createNewUser() {
        EntityModel userEntityModel = new EntityModel();
        Set<FieldModel> fields = new HashSet<>();

        fields.add(new StringFieldModel("full_name", "John Doe"));
        fields.add(new StringFieldModel("last_name", "doe"));
        fields.add(new StringFieldModel("type", "user"));
        fields.add(new StringFieldModel("first_name", "john"));
        fields.add(new StringFieldModel("email", "john.doe@hpe.com"));
        userEntityModel.setValues(fields);



        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();

        octane.entityList("users").create().entities(Collections.singletonList(userEntityModel));

    }


}
