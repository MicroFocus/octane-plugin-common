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

import static org.junit.Assert.fail;

/**
 * Enables the use of the {@link Inject} annotation
 */
public abstract class IntegrationTestBase {


    private final Logger logger = LogManager.getLogger(IntegrationTestBase.class.getName().toString());

    protected EntityGenerator entityGenerator;

    ConnectionSettingsProvider connectionSettings;


    /**
     * This function will set up a context needed for the tests, the context is derived from the annotations set the
     * implementing class
     */
    @Before
    public void setup() {
        Annotation[] annotations = this.getClass().getDeclaredAnnotations();

        boolean cleanWorkspace = findWorkSpaceAnnotation(annotations);

        if (cleanWorkspace) {
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

        boolean createNewUser = findUserAnnotation(annotations);

        if(createNewUser) {
            createUSer();
        } else {
            //find all users and choose the first one - if there are no users create one
            //TODO
        }

        Injector injector = Guice.createInjector(new ServiceModule(connectionSettings));
        injector.injectMembers(this);
        entityGenerator = new EntityGenerator(injector.getInstance(OctaneProvider.class));
    }

    /**
     * This method will look for the workspace annotation in the implementing subclass
     * @param annotations - of the subclass
     * @return boolean - found or not
     */
    private boolean findWorkSpaceAnnotation(Annotation[] annotations) {

        for (Annotation annotation : annotations) {
            if (annotation instanceof WorkSpace) {
                if (((WorkSpace) annotation).clean()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method will look for the User annotation in the implementing subclass
     * @param annotations - of the subclass
     * @return boolean - found or not
     */
    private boolean findUserAnnotation(Annotation[] annotations) {

        for (Annotation annotation : annotations) {
            if (annotation instanceof User) {
                if (((User) annotation).create()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method will create a new workspace
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

    public EntityModel createEntity() {

        Entity userStory = Entity.USER_STORY;

        EntityModel entityModel = entityGenerator.createEntityModel(userStory);

        EntityService entityService = new EntityService();

        Collection<EntityModel> entities = entityService.findEntities(Entity.USER_STORY);

        try {
            assert entities.contains(entityModel);
        } catch (Exception e) {
            logger.debug("Error the entity could not be created");
            Assert.fail();
        }

        for (EntityModel em : entities) {
            if (em.equals(entityModel))
                return em;
        }
        return null;
    }

    public void deleteEntity(EntityModel entityModel) {
        entityGenerator.deleteEntityModel(entityModel);
    }

    /**
     * This method will create a new user with hardcoded user info -John Doe
     */
    public void createUSer() {
        String postUrl = connectionSettings.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettings.getConnectionSettings().getSharedSpaceId() + "/users";

        String urlDomain = connectionSettings.getConnectionSettings().getBaseUrl();

        JSONObject dataSet = new JSONObject();
        JSONObject userJson = new JSONObject();
        userJson.put("type", "user");
        userJson.put("first_name", "john");
        userJson.put("last_name", "doe");
        userJson.put("email", "john.doe@hpe.com");
        userJson.put("full_name", "John Doe");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(userJson);
        dataSet.put("data", jsonArray);


        OctaneHttpRequest postNewWorkspaceRequest = new OctaneHttpRequest.PostOctaneHttpRequest(postUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, dataSet.toString());
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettings.getConnectionSettings().getUserName(), connectionSettings.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;

        try {
            response = octaneHttpClient.execute(postNewWorkspaceRequest);

        } catch (Exception e) {

            logger.error("Error while trying to get the response when creating a new user!");
            e.printStackTrace();
            fail(e.toString());
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        octaneHttpClient.signOut();

    }


}
