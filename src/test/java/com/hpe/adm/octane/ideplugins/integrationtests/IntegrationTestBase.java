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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.integrationtests.util.*;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

/**
 * Enables the use of the {@link Inject} annotation
 */

public abstract class IntegrationTestBase {


    private final Logger logger = LogManager.getLogger(IntegrationTestBase.class.getName().toString());

    private EntityGenerator entityGenerator;

    protected ConnectionSettingsProvider connectionSettingsProvider;

    private ServiceModule serviceModule;

    private EntityModel nativeStatus;


    /**
     * This function will set up a context needed for the tests, the context is derived from the annotations set the
     * implementing class
     */
    @Before
    public void setUp() {
        Annotation[] annotations = this.getClass().getDeclaredAnnotations();
        WorkSpace workSpaceAnnotation = getAnnotation(annotations, WorkSpace.class);
        if (workSpaceAnnotation != null && workSpaceAnnotation.clean()) {
            connectionSettingsProvider = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
            ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
            connectionSettings.setWorkspaceId(createWorkSpace());
            connectionSettingsProvider.setConnectionSettings(connectionSettings);
        } else {
            connectionSettingsProvider = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
            ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
            long defaultWorkspaceId = getDefaultWorkspaceId();
            if (defaultWorkspaceId > 0)
                connectionSettings.setWorkspaceId(defaultWorkspaceId);
            else {
                connectionSettings.setWorkspaceId(createWorkSpace());
            }
            connectionSettingsProvider.setConnectionSettings(connectionSettings);
        }
        if (connectionSettingsProvider == null) {
            throw new RuntimeException("Cannot retrieve connection settings from either vm args or prop file, cannot run tests");
        }
        serviceModule = new ServiceModule(connectionSettingsProvider);
        User userAnnotation = getAnnotation(annotations, User.class);
        if (userAnnotation != null && userAnnotation.create()) {
            createNewUser(userAnnotation.firstName(), userAnnotation.lastName());
        }
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
        entityGenerator = new EntityGenerator(injector.getInstance(OctaneProvider.class));
        Entities entities = getAnnotation(annotations, Entities.class);
        if (entities != null) {
            Entity[] entitiesArray = entities.requiredEntities();
            for (Entity newEntity : entitiesArray) {
                System.out.println(newEntity.getEntityName());
                createEntity(newEntity);
            }
        }
        nativeStatus = new EntityModel("type", "list_node");
        nativeStatus.setValue(new StringFieldModel("id", "1094"));
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
     * @return workspace_id of the newly created workspace
     */
    public Long createWorkSpace() {
        String postUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + "/workspaces";
        String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
        JSONObject dataSet = new JSONObject();
        JSONObject credentials = new JSONObject();
        credentials.put("name", "test_workspace1");
        credentials.put("description", "Created from intellij");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(credentials);
        dataSet.put("data", jsonArray);
        System.out.println(dataSet.toString());
        OctaneHttpRequest postNewWorkspaceRequest = new OctaneHttpRequest.PostOctaneHttpRequest(postUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, dataSet.toString());
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
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
     * This method will return the first workspace
     *
     * @return the workspace_id of the first workspace obtained, -1 if no workspace is found
     */
    public long getDefaultWorkspaceId() {
        String getUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + "/workspaces";
        String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
        OctaneHttpRequest getAllWorkspacesRequest = new OctaneHttpRequest.GetOctaneHttpRequest(getUrl);
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
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

    /**
     * This method will create a new entity
     *
     * @param entity - a new entity
     * @return the created entityModel or null if it could not been created
     */
    public EntityModel createEntity(Entity entity) {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        EntityModel entityModel = entityGenerator.createEntityModel(entity);
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(entityModel));
        return entityModel;
    }

    /**
     * This method will delete an entity
     *
     * @param entityModel - the entityModel to be deleted
     */
    public void deleteEntity(EntityModel entityModel) {
        entityGenerator.deleteEntityModel(entityModel);
    }

    /**
     * This method will create relationships between users and entities
     *
     * @param user        - the user
     * @param entityModel - the new entity to be related to
     */
    public void createRelations(EntityModel user, EntityModel entityModel) {
        user.setValue(new ReferenceFieldModel("userrel", entityModel));
    }

    /**
     * This method creates a new user with default password: Welcome1
     */
    public void createNewUser(String firstName, String lastName) {
        EntityModel userEntityModel = new EntityModel();
        Set<FieldModel> fields = new HashSet<>();
        List<EntityModel> roles = getRoles();
        if (roles == null) {
            logger.debug("failed to obtain the roles in the environment");
            return;
        }
        fields.add(new StringFieldModel("full_name", firstName + lastName));
        fields.add(new StringFieldModel("last_name", lastName));
        fields.add(new StringFieldModel("type", "workspace_user"));
        fields.add(new StringFieldModel("first_name", firstName));
        fields.add(new StringFieldModel("email", firstName + "." + lastName + "@hpe.com"));
        fields.add(new StringFieldModel("password", "Welcome1"));
        fields.add(new MultiReferenceFieldModel("roles", Collections.singletonList(roles.get(0))));
        userEntityModel.setValues(fields);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        octane.entityList("workspace_users").create().entities(Collections.singletonList(userEntityModel)).execute();
    }

    /**
     * This method will return the current user
     *
     * @return the user entityModel if found, null otterwise
     */
    public EntityModel getCurrentUser() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        List<EntityModel> users = octane.entityList("workspace_users").get().execute().stream().collect(Collectors.toList());

        for (EntityModel user : users) {
            if (user.getValue("email").getValue().toString().equals(connectionSettingsProvider.getConnectionSettings().getUserName().toString())) {
                return user;
            }
        }
        return null;
    }

    /**
     * This method will retrieve all the possible roles that can be assigned to a user
     *
     * @return - a list of enitityModels representing the possible roles
     */
    public List<EntityModel> getRoles() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList("user_roles").get().execute().stream().collect(Collectors.toList());
    }

    /**
     * This method will return all the users
     *
     * @return - a list of users
     */
    public List<EntityModel> getUsers() {
        EntityService entityService = serviceModule.getInstance(EntityService.class);
        Set<String> mySet = new HashSet<>();
        mySet.add("roles");

        List<EntityModel> entities = new ArrayList<>(entityService.findEntities(
                Entity.WORKSPACE_USER,
                null,
                mySet)
        );
        System.out.println(entities);
        return entities;
    }

    /**
     * This method will search for a user by its id
     *
     * @param id - user id
     * @return null - if not found, userEntityModel if found
     */
    public EntityModel getUserById(long id) {
        EntityService entityService = serviceModule.getInstance(EntityService.class);
        try {
            EntityModel entity = entityService.findEntity(Entity.WORKSPACE_USER, id);
            System.out.println(entity);
            return entity;
        } catch (ServiceException e) {
            e.printStackTrace();
            logger.debug("There was an issue with retrieving the user with id " + id);
        }
        return null;
    }


    /**
     * This method will return the first release in the list of releases
     *
     * @return the entityModel of the release
     */
    public EntityModel getRelease() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList("releases").get().execute().iterator().next();
    }

    /**
     * This method is going to create a Task
     *
     * @param userStory - user story to attach the task to
     * @param taskName  - the name of the task
     * @return the built entityModel
     */
    public EntityModel createTask(EntityModel userStory, String taskName) {
        EntityModel taskEntityModel = new EntityModel("type", "task");
        taskEntityModel.setValue(new StringFieldModel("name", taskName));
        taskEntityModel.setValue(new ReferenceFieldModel("story", userStory));
        Entity entity = Entity.getEntityType(taskEntityModel);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(taskEntityModel)).execute().iterator().next();
    }

    /**
     * This method will create a manual test run
     *
     * @param manualTest - the Manual Test to which the test run is planned
     * @param name       - the name of the run
     * @return the entityModel of the run
     */
    public EntityModel createManualRun(EntityModel manualTest, String name) {

        EntityModel manualRun = new EntityModel("type", "run");
        manualRun.setValue(new StringFieldModel("name", "a1"));
        manualRun.setValue(new StringFieldModel("subtype", "run_manual"));
        manualRun.setValue(new ReferenceFieldModel("native_status", nativeStatus));
        manualRun.setValue(new ReferenceFieldModel("release", getRelease()));
        manualRun.setValue(new ReferenceFieldModel("test", manualTest));
        Entity entity = Entity.getEntityType(manualRun);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(manualRun)).execute().iterator().next();
    }

    /**
     * This method will create a Test Suite
     *
     * @param name the name of the test suite
     * @return the entityModel of the test suite, null if not created
     */
    public EntityModel createTestSuite(String name) {
        EntityModel testSuite = new EntityModel("type", "test");
        testSuite.setValue(new StringFieldModel("name", name));
        testSuite.setValue(new StringFieldModel("subtype", "test_suite"));
        Entity entity = Entity.getEntityType(testSuite);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();

        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuite)).execute().iterator().next();
    }

    /**
     * this method will create a test suite run
     *
     * @param testSuite        the test suite which is used to create the test suite run
     * @param testSuiteRunName the name of the suite run
     * @return the created entityModel of the suite run
     */
    public EntityModel createTestSuiteRun(EntityModel testSuite, String testSuiteRunName) {
        EntityModel testSuiteRun = new EntityModel("type", "run");
        testSuiteRun.setValue(new StringFieldModel("name", testSuiteRunName));
        testSuiteRun.setValue(new StringFieldModel("subtype", "run_suite"));
        testSuiteRun.setValue(new ReferenceFieldModel("native_status", nativeStatus));
        testSuiteRun.setValue(new ReferenceFieldModel("release", getRelease()));
        testSuiteRun.setValue(new ReferenceFieldModel("test", testSuite));
        Entity entity = Entity.getEntityType(testSuiteRun);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuiteRun)).execute().iterator().next();
    }


    /**
     * This method creates an automated test
     *
     * @param testName - the name of the new automated test
     * @return the newly created automated test entityModel
     */
    public EntityModel createAutomatedTest(String testName) {
        EntityModel automatedTest = new EntityModel("type", "test");
        automatedTest.setValue(new StringFieldModel("subtype", "test_automated"));
        automatedTest.setValue(new StringFieldModel("name", testName));
        Entity entity = Entity.getEntityType(automatedTest);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(automatedTest)).execute().iterator().next();
    }


    /**
     * This method will add an entity into the my work section
     *
     * @param entityModel - the entity to be added
     */
    public void addToMyWork(EntityModel entityModel) {
        MyWorkService myWorkService = serviceModule.getInstance(MyWorkService.class);

        try {
            myWorkService.addToMyWork(entityModel);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will set a user to be the owner of another entity
     *
     * @param backlogItem - the backlog item
     * @param owner       - the user
     */
    public void setOwner(EntityModel backlogItem, EntityModel owner) {
        EntityModel updatedEntityModel = new EntityModel();
        updatedEntityModel.setValue(backlogItem.getValue("id"));
        updatedEntityModel.setValue(backlogItem.getValue("type"));
        updatedEntityModel.setValue(new ReferenceFieldModel("owner", owner));
        Entity entity = Entity.getEntityType(updatedEntityModel);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).update().entities(Collections.singleton(updatedEntityModel)).execute();
    }

    /**
     * This method will retrieve the items in My Work
     *
     * @return a list of entities representing the items in my work
     */
    public List<EntityModel> getMyWorkItems() {
        MyWorkService myWorkService = serviceModule.getMyWorkService();

        return myWorkService.getMyWork().stream().collect(Collectors.toList());
    }
}
