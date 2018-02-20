/*
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.integrationtests.util.*;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * Enables the use of the {@link Inject} annotation
 */
public abstract class IntegrationTestBase {

    @Inject
    private EntitySearchService searchService;

    @Inject
    private OctaneVersionService versionService;

    private EntityGenerator entityGenerator;
    protected ConnectionSettingsProvider connectionSettingsProvider;
    private ServiceModule serviceModule;
    private EntityModel nativeStatus;

    protected enum Octane_Version {OLD_VERSION, MIDDLE_VERSION, NEW_VERSION};

    /**
     * Sets up a context needed for the tests, the context is derived from the annotations set the
     * implementing class
     */
    @Before
    public void setUp() {

        connectionSettingsProvider = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
        if (connectionSettingsProvider == null) {
            throw new RuntimeException(Constants.Errors.CONNECTION_SETTINGS_RETRIEVE_ERROR);
        }

        Annotation[] annotations = this.getClass().getDeclaredAnnotations();

        WorkSpace workSpaceAnnotation = getAnnotation(annotations, WorkSpace.class);

        if (workSpaceAnnotation != null && workSpaceAnnotation.clean()) {
            ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
            connectionSettings.setWorkspaceId(createWorkSpace());
            connectionSettingsProvider.setConnectionSettings(connectionSettings);
        } else {

            ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
            connectionSettings.setWorkspaceId(getDefaultWorkspaceId());
            connectionSettingsProvider.setConnectionSettings(connectionSettings);
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

        nativeStatus = new EntityModel(Constants.TYPE, Constants.NativeStatus.NATIVE_STATUS_TYPE_VALUE);
        switch (getOctaneVersion()){
            case OLD_VERSION: {
                nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_OLD_ID));
            }
            case MIDDLE_VERSION: {
                nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_NEW_ID));
            }
            case NEW_VERSION: {
                nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_RUN_ID));
            }
        }

        createRelease();
    }


    /**
     * Looks for an annotation in the implementing subclass
     *
     * @param annotations     the annotations of the implementing subclass
     * @param annotationClass the annotation type to look for
     * @param <A>             the generic annotation class
     * @return the instance of that annotation, @null in case it isn't found
     */
    private <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                return (A) annotation;
            }
        }
        return null;
    }

    /**
     * Creates a new workspace
     *
     * @return the workspace_id of the newly created workspace
     */
    private Long createWorkSpace() {
        String postUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + Constants.SHARED_SPACE +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + Constants.WORKSPACE;
        String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
        JSONObject dataSet = new JSONObject();
        JSONObject credentials = new JSONObject();
        credentials.put(Constants.NAME, Constants.Workspace.NAME_VALUE);
        credentials.put(Constants.DESCRIPTION, Constants.Workspace.DESCRIPTION);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(credentials);
        dataSet.put(Constants.DATA, jsonArray);
        OctaneHttpRequest postNewWorkspaceRequest = new OctaneHttpRequest.PostOctaneHttpRequest(postUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, dataSet.toString());
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;
        try {
            response = octaneHttpClient.execute(postNewWorkspaceRequest);
        } catch (Exception e) {
            fail(e.toString());
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        JSONArray workspaces = responseJson.getJSONArray(Constants.DATA);

        octaneHttpClient.signOut();
        JSONObject workspace = (JSONObject) workspaces.get(0);
        return workspace.getLong(Constants.Workspace.WORKSPACE_ID);
    }

    /**
     * Returns the first workspace
     *
     * @return the workspace_id of the first workspace obtained, -1 if no workspace is found
     */
    private long getDefaultWorkspaceId() {
        String getUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + Constants.SHARED_SPACE +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + Constants.WORKSPACE;
        String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
        OctaneHttpRequest getAllWorkspacesRequest = new OctaneHttpRequest.GetOctaneHttpRequest(getUrl);
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;
        try {
            response = octaneHttpClient.execute(getAllWorkspacesRequest);
        } catch (Exception e) {
            fail(e.toString());
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        JSONArray workspaces = responseJson.getJSONArray(Constants.DATA);
        if (workspaces.length() == 0)
            return -1;
        return ((JSONObject) workspaces.get(0)).getLong(Constants.ID);
    }

    /**
     * Creates a new entity
     *
     * @param entity - the new entity
     * @return the created entityModel, @null if it could not been created
     */
    protected EntityModel createEntity(Entity entity) {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        EntityModel entityModel = entityGenerator.createEntityModel(entity);
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(entityModel));
        return entityModel;
    }

    /**
     * Removes an entity
     *
     * @param entityModel - the entityModel to be deleted
     */
    public void deleteEntity(EntityModel entityModel) {
        entityGenerator.deleteEntityModel(entityModel);
    }

    /**
     * Creates relationships between users and entities
     *
     * @param user        - the user
     * @param entityModel - the new entity to be related to
     */
    public void createRelations(EntityModel user, EntityModel entityModel) {
        user.setValue(new ReferenceFieldModel("userrel", entityModel));
    }

    /**
     * Creates a new user with default password: Welcome1
     *
     * @return the newly created user entityModel, @null if it could not be created
     */
    protected EntityModel createNewUser(String firstName, String lastName) {

        EntityModel userEntityModel = new EntityModel();
        Set<FieldModel> fields = new HashSet<>();
        List<EntityModel> roles = getRoles();

        if (roles.size() == 0) {
            return null;
        }
        fields.add(new StringFieldModel(Constants.User.FULL_NAME, firstName + lastName));
        fields.add(new StringFieldModel(Constants.User.LAST_NAME, lastName));
        fields.add(new StringFieldModel(Constants.TYPE, Constants.User.USER_TYPE));
        fields.add(new StringFieldModel(Constants.User.FIRST_NAME, firstName));
        fields.add(new StringFieldModel(Constants.User.EMAIL, firstName + "." + lastName + Constants.User.EMAIL_DOMAIN));
        fields.add(new StringFieldModel(Constants.User.PASSWORD, Constants.User.PASSWORD_VALUE));
        fields.add(new MultiReferenceFieldModel(Constants.ROLES, Collections.singletonList(roles.get(0))));

        if (getOctaneVersion()==Octane_Version.MIDDLE_VERSION) {
            fields.add(new StringFieldModel(Constants.User.PHONE, Constants.User.PHONE_NR));
        }

        userEntityModel.setValues(fields);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(Constants.User.USER_TYPE).create().entities(Collections.singletonList(userEntityModel)).execute().iterator().next();
    }

    /**
     * Returns the current user
     *
     * @return the users entityModel if found, @null otherwise
     */
    protected EntityModel getCurrentUser() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        List<EntityModel> users = new ArrayList<>(octane.entityList(Constants.WORKSPACE_ENITY_NAME).get().execute());

        for (EntityModel user : users) {
            if (user.getValue(Constants.User.EMAIL).getValue().toString().equals(connectionSettingsProvider.getConnectionSettings().getUserName())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Retrieves all the possible roles that can be assigned to a user
     *
     * @return - a list of enitityModels representing the possible roles
     */
    private List<EntityModel> getRoles() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList(Constants.User.USER_ROLES).get().execute());
    }

    /**
     * Returns all the workspace users
     *
     * @return - a list of entityModels representing the workspace users
     */
    public List<EntityModel> getUsers() {
        EntityService entityService = serviceModule.getInstance(EntityService.class);
        Set<String> roles = new HashSet<>();
        roles.add(Constants.ROLES);

        return new ArrayList<>(entityService.findEntities(
                Entity.WORKSPACE_USER,
                null,
                roles)
        );

    }

    /**
     * Searches for a user by its id
     *
     * @param id - user id
     * @return @null - if not found, userEntityModel if found
     */
    protected EntityModel getUserById(long id) {
        EntityService entityService = serviceModule.getInstance(EntityService.class);
        try {
            return entityService.findEntity(Entity.WORKSPACE_USER, id);
        } catch (ServiceException e) {
            e.printStackTrace();
            //logger.debug("There was an issue with retrieving the user with id " + id);
        }
        return null;
    }


    /**
     * Returns the first release in the list of releases
     *
     * @return the entityModel of the release
     */
    private EntityModel getRelease() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(Constants.Release.RELEASES).get().execute().iterator().next();
    }

    protected Octane_Version getOctaneVersion() {
        OctaneVersion version = versionService.getOctaneVersion();
        Octane_Version returnVersion =  Octane_Version.OLD_VERSION;;
        if((OctaneVersion.compare(version, OctaneVersion.Operation.HIGHER, OctaneVersion.GENT_P3))){
            returnVersion = Octane_Version.NEW_VERSION;
        }
        if((OctaneVersion.compare(version, OctaneVersion.Operation.HIGHER, OctaneVersion.EVERTON_P3))){
            returnVersion = Octane_Version.MIDDLE_VERSION;
        }
        return returnVersion;
    }

    private void createRelease() {
        String postUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + Constants.SHARED_SPACE +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + Constants.WORKSPACE + "/" +
                connectionSettingsProvider.getConnectionSettings().getWorkspaceId() + Constants.RELEASES;
        String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
        JSONObject dataSet = new JSONObject();
        JSONObject releaseJson = new JSONObject();
        releaseJson.put(Constants.NAME, Constants.Release.NAME + UUID.randomUUID().toString());
        releaseJson.put(Constants.TYPE, Constants.Release.TYPE);
        LocalDateTime localDateTImeNow = LocalDateTime.now();
        releaseJson.put(Constants.Release.START_DATE, localDateTImeNow.toString() + "Z");
        releaseJson.put(Constants.Release.END_DATE, localDateTImeNow.toString() + "Z");
        JSONObject agileTypeJson = new JSONObject();
        if (getOctaneVersion()!=Octane_Version.OLD_VERSION) {
            agileTypeJson.put(Constants.ID, Constants.AgileType.NEW_ID);
        } else {
            agileTypeJson.put(Constants.ID, Constants.AgileType.OLD_ID);
        }
        agileTypeJson.put(Constants.NAME, Constants.AgileType.NAME);
        agileTypeJson.put(Constants.TYPE, Constants.AgileType.TYPE);
        agileTypeJson.put(Constants.LOGICAL_NAME, Constants.AgileType.NEW_ID);
        releaseJson.put(Constants.AgileType.AGILE_TYPE, agileTypeJson);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(releaseJson);
        dataSet.put(Constants.DATA, jsonArray);
        OctaneHttpRequest postNewReleaseRequest = new OctaneHttpRequest.PostOctaneHttpRequest(postUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, dataSet.toString());
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        try {
            octaneHttpClient.execute(postNewReleaseRequest);
        } catch (Exception e) {
            //logger.error("Error while trying to get the response when creating a new release!");
            fail(e.toString());
        }
    }

    @After
    public void deleteRelease(){
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        octane.entityList(Constants.Release.RELEASES)
                .delete()
                .query(
                        Query.statement(Constants.ID, QueryMethod.EqualTo, getRelease().getValue(Constants.ID).getValue().toString()).build())
                .execute();
    }


    /**
     * Creates a Task
     *
     * @param userStory - user story to attach the task to
     * @param taskName  - the name of the task
     * @return the built entityModel
     */
    protected EntityModel createTask(EntityModel userStory, String taskName) {
        EntityModel taskEntityModel = new EntityModel(Constants.TYPE, Constants.TASK);
        taskEntityModel.setValue(new StringFieldModel(Constants.NAME, taskName));
        taskEntityModel.setValue(new ReferenceFieldModel(Constants.STORY, userStory));
        Entity entity = Entity.getEntityType(taskEntityModel);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(taskEntityModel)).execute().iterator().next();
    }

    private List<EntityModel> getRequirements() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList(Constants.REQUIREMENTS).get().execute());
    }


    protected EntityModel createRequirement(String requirementName, EntityModel parent) {
        EntityModel phase = new EntityModel(Constants.TYPE, Constants.PHASE);
        phase.setValue(new StringFieldModel(Constants.ID, Constants.Requirement.ID));
        phase.setValue(new StringFieldModel(Constants.NAME, Constants.Requirement.NAME));
        phase.setValue(new StringFieldModel(Constants.LOGICAL_NAME, Constants.Requirement.LOGICAL_NAME));
        EntityModel requirement = new EntityModel(Constants.TYPE, Constants.Requirement.TYPE);
        requirement.setValue(new StringFieldModel(Constants.NAME, requirementName));
        requirement.setValue(new StringFieldModel(Constants.SUBTYPE, Constants.Requirement.DOCUMENT));
        requirement.setValue(new ReferenceFieldModel(Constants.PARENT, parent));
        requirement.setValue(new ReferenceFieldModel(Constants.PHASE, phase));
        Entity entity = Entity.getEntityType(requirement);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(requirement)).execute().iterator().next();
    }

    protected EntityModel createRequirementFolder(String folderName) {
        EntityModel requirement = new EntityModel(Constants.TYPE, Constants.Requirement.TYPE);
        requirement.setValue(new StringFieldModel(Constants.NAME, folderName));
        requirement.setValue(new StringFieldModel(Constants.SUBTYPE, Constants.Requirement.FOLDER));
        requirement.setValue(new ReferenceFieldModel(Constants.PARENT, getRequirementsRoot()));
        Entity entity = Entity.getEntityType(requirement);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(requirement)).execute().iterator().next();
    }

    protected List<EntityModel> getTasks() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList(Constants.TASKS).get().execute());
    }
    /**
     * Creates a manual test run
     *
     * @param manualTest - the Manual Test to which the test run is planned
     * @param name       - the name of the run
     * @return the entityModel of the run
     */
    protected EntityModel createManualRun(EntityModel manualTest, String name) {
        EntityModel manualRun = new EntityModel(Constants.TYPE, Constants.ManualRun.RUN);
        manualRun.setValue(new StringFieldModel(Constants.NAME, name));
        manualRun.setValue(new StringFieldModel(Constants.SUBTYPE, Constants.ManualRun.SUBTYPE));
        manualRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, nativeStatus));
        manualRun.setValue(new ReferenceFieldModel(Constants.Release.TYPE, getRelease()));
        manualRun.setValue(new ReferenceFieldModel(Constants.TEST, manualTest));
        Entity entity = Entity.getEntityType(manualRun);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        try{
            return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(manualRun)).execute().iterator().next();
        } catch (Exception e) {
            manualRun.removeValue(Constants.NATIVE_STATUS);
            EntityModel newNativeStatus = nativeStatus;
            newNativeStatus.removeValue(Constants.ID);
            newNativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_RUN_ID));
            manualRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, newNativeStatus));
            return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(manualRun)).execute().iterator().next();
        }
    }

    /**
     * Creates a Test Suite
     *
     * @param name the name of the test suite
     * @return the entityModel of the test suite, @null if not created
     */
    protected EntityModel createTestSuite(String name) {
        EntityModel testSuite = new EntityModel(Constants.TYPE, Constants.TEST);
        testSuite.setValue(new StringFieldModel(Constants.NAME, name));
        testSuite.setValue(new StringFieldModel(Constants.SUBTYPE, Constants.TEST_SUITE));
        Entity entity = Entity.getEntityType(testSuite);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();

        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuite)).execute().iterator().next();
    }

    /**
     * Creates a test suite run
     *
     * @param testSuite        the test suite which is used to create the test suite run
     * @param testSuiteRunName the name of the suite run
     * @return the created entityModel of the suite run
     */
    protected EntityModel createTestSuiteRun(EntityModel testSuite, String testSuiteRunName) {
        EntityModel testSuiteRun = new EntityModel(Constants.TYPE, Constants.ManualRun.RUN);
        testSuiteRun.setValue(new StringFieldModel(Constants.NAME, testSuiteRunName));
        testSuiteRun.setValue(new StringFieldModel(Constants.SUBTYPE, Constants.RUN_SUITE));
        testSuiteRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, nativeStatus));
        testSuiteRun.setValue(new ReferenceFieldModel(Constants.Release.TYPE, getRelease()));
        testSuiteRun.setValue(new ReferenceFieldModel(Constants.TEST, testSuite));
        Entity entity = Entity.getEntityType(testSuiteRun);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuiteRun)).execute().iterator().next();
    }


    /**
     * Creates an automated test
     *
     * @param testName - the name of the new automated test
     * @return the newly created automated test entityModel
     */
    protected EntityModel createAutomatedTest(String testName) {
        EntityModel automatedTest = new EntityModel(Constants.TYPE, Constants.TEST);
        automatedTest.setValue(new StringFieldModel(Constants.SUBTYPE, Constants.TEST_AUTOMATED));
        automatedTest.setValue(new StringFieldModel(Constants.NAME, testName));
        Entity entity = Entity.getEntityType(automatedTest);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(automatedTest)).execute().iterator().next();
    }


    /**
     * Adds an entity into the my work section
     *
     * @param entityModel - the entity to be added
     */
    protected void addToMyWork(EntityModel entityModel) {
        MyWorkService myWorkService = serviceModule.getInstance(MyWorkService.class);

        try {
            myWorkService.addToMyWork(entityModel);
        } catch (OctaneException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a user to be the owner of another entity
     *
     * @param backlogItem - the backlog item
     * @param owner       - the user
     */
    protected void setOwner(EntityModel backlogItem, EntityModel owner) {
        EntityModel updatedEntityModel = new EntityModel();
        updatedEntityModel.setValue(backlogItem.getValue(Constants.ID));
        updatedEntityModel.setValue(backlogItem.getValue(Constants.TYPE));
        updatedEntityModel.setValue(new ReferenceFieldModel(Constants.OWNER, owner));
        Entity entity = Entity.getEntityType(updatedEntityModel);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).update().entities(Collections.singleton(updatedEntityModel)).execute();
    }

    /**
     * Sets the description of an entity
     *
     * @param backlogItem the backlog item
     * @param description the description string
     */
    protected void setDescription(EntityModel backlogItem, String description) {
        EntityModel updatedEntityModel = new EntityModel();
        updatedEntityModel.setValue(backlogItem.getValue(Constants.ID));
        updatedEntityModel.setValue(backlogItem.getValue(Constants.TYPE));
        updatedEntityModel.setValue(new StringFieldModel(Constants.DESCRIPTION, description));
        Entity entity = Entity.getEntityType(updatedEntityModel);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        octane.entityList(entity.getApiEntityName()).update().entities(Collections.singleton(updatedEntityModel)).execute();
    }

    /**
     * Retrieves the items in My Work
     *
     * @return a list of entities representing the items in my work
     */
    protected List<EntityModel> getMyWorkItems() {
        MyWorkService myWorkService = serviceModule.getMyWorkService();

        return new ArrayList<>(myWorkService.getMyWork());
    }

    /**
     * Retrieves the backlog items: tests and work items
     *
     * @return a list of the work items and lists
     */
    private List<EntityModel> retrieveBacklog() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        List<EntityModel> workItems = new ArrayList<>(octane.entityList(Constants.WORK_ITEMS).get().query(Query.not(Constants.SUBTYPE, QueryMethod.EqualTo, Constants.WORK_ITEM_ROOT).build()).execute());
        List<EntityModel> tests = new ArrayList<>(octane.entityList(Constants.TESTS).get().execute());
        return Stream.concat(workItems.stream(), tests.stream()).collect(Collectors.toList());

    }

    /**
     * Deletes the backlog items
     */
    protected void deleteBacklogItems() {
        List<EntityModel> workspaceEntities = retrieveBacklog();
        Query.QueryBuilder workItemsQuery = null;
        Query.QueryBuilder testItemsQuery = null;
        for (EntityModel entityModel : workspaceEntities) {
            String entityType = entityModel.getValue(Constants.TYPE).getValue().toString();
            if (Constants.WORK_ITEM.equals(entityType)) {
                if (workItemsQuery != null) {
                    workItemsQuery = workItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    workItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
            if (Constants.TEST.equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    testItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
            if (Constants.ManualRun.RUN.equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    testItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
        }
        if (workspaceEntities.size() > 0) {

            Octane.Builder octaneBuilder = new Octane.Builder(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
            octaneBuilder.sharedSpace(connectionSettingsProvider.getConnectionSettings().getSharedSpaceId());
            octaneBuilder.workSpace(connectionSettingsProvider.getConnectionSettings().getWorkspaceId());
            Octane octane = octaneBuilder.Server(connectionSettingsProvider.getConnectionSettings().getBaseUrl()).build();
            if (testItemsQuery != null)
                octane.entityList(Constants.TESTS).delete().query(testItemsQuery.build()).execute();
            if (workItemsQuery != null)
                octane.entityList(Constants.WORK_ITEMS).delete().query(workItemsQuery.build()).execute();
        }
    }

    protected EntityModel search(String searchField, String query) {
        List<EntityModel> entityModels = searchService.searchGlobal(query, 1000, Entity.WORK_ITEM, Entity.MANUAL_TEST, Entity.GHERKIN_TEST, Entity.TASK, Entity.REQUIREMENT).stream().collect(Collectors.toList());

        for (EntityModel entityModel : entityModels) {
            if (removeTags(entityModel.getValue(searchField).getValue().toString()).contains(query)) {
                return entityModel;
            }
        }
        return null;
    }

    private String removeTags(String s) {
        String result;
        if (s.contains("<em>")) {
            result = s.replaceAll("<em>", "");
            result = result.replaceAll("</em>", "");
            return result;
        }
        return s;
    }

    protected boolean compareEntities(EntityModel entity1, EntityModel entity2) {
        return (entity1.getValue("id").getValue().toString().equals(entity2.getValue(Constants.ID).getValue().toString()));
    }

    protected EntityModel findRequirementById(long id) {
        List<EntityModel> requirements = getRequirements();
        for (EntityModel entityModel : requirements) {
            if (Long.parseLong(entityModel.getValue(Constants.ID).getValue().toString()) == id) {
                return entityModel;
            }
        }
        return null;
    }

    private EntityModel getRequirementsRoot() {
        List<EntityModel> requirements = getRequirements();
        for (EntityModel entityModel : requirements) {
            if (Constants.REQUIREMENT_ROOT.equals(entityModel.getValue(Constants.SUBTYPE).getValue().toString())) {
                return entityModel;
            }
        }
        return null;
    }
}
