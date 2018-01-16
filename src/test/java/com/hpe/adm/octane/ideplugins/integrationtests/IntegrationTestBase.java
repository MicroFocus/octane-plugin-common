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

    /**
     * Sets up a context needed for the tests, the context is derived from the annotations set the
     * implementing class
     */
    @Before
    public void setUp() {

        connectionSettingsProvider = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
        if (connectionSettingsProvider == null) {
            throw new RuntimeException("Cannot retrieve connection settings from either vm args or prop file, cannot run tests");
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

        nativeStatus = new EntityModel("type", "list_node");
        if (isNewerOctane())
            nativeStatus.setValue(new StringFieldModel("id", "1094"));
        else
            nativeStatus.setValue(new StringFieldModel("id", "1091"));

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
        OctaneHttpRequest postNewWorkspaceRequest = new OctaneHttpRequest.PostOctaneHttpRequest(postUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, dataSet.toString());
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;
        try {
            response = octaneHttpClient.execute(postNewWorkspaceRequest);
        } catch (Exception e) {
            //logger.error("Error while trying to get the response when creating a new workspace!");
            fail(e.toString());
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        JSONArray workspaces = responseJson.getJSONArray("data");

        octaneHttpClient.signOut();
        JSONObject workspace = (JSONObject) workspaces.get(0);
        return workspace.getLong("workspace_id");
    }

    /**
     * Returns the first workspace
     *
     * @return the workspace_id of the first workspace obtained, -1 if no workspace is found
     */
    private long getDefaultWorkspaceId() {
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
            //logger.debug("Exception while trying to get all the workspaces");
            fail(e.toString());
        }
        JSONObject responseJson = new JSONObject(response.getContent());
        JSONArray workspaces = responseJson.getJSONArray("data");
        if (workspaces.length() == 0)
            return -1;
        return ((JSONObject) workspaces.get(0)).getLong("id");
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

        if (roles == null) {
            //logger.debug("failed to obtain the roles in the environment");
            return null;
        }
        fields.add(new StringFieldModel("full_name", firstName + lastName));
        fields.add(new StringFieldModel("last_name", lastName));
        fields.add(new StringFieldModel("type", "workspace_user"));
        fields.add(new StringFieldModel("first_name", firstName));
        fields.add(new StringFieldModel("email", firstName + "." + lastName + "@hpe.com"));
        fields.add(new StringFieldModel("password", "Welcome1"));
        fields.add(new MultiReferenceFieldModel("roles", Collections.singletonList(roles.get(0))));

        if (!isNewerOctane()) {
            fields.add(new StringFieldModel("phone1", "0875432135"));
        }

        userEntityModel.setValues(fields);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList("workspace_users").create().entities(Collections.singletonList(userEntityModel)).execute().iterator().next();
    }

    /**
     * Returns the current user
     *
     * @return the users entityModel if found, @null otherwise
     */
    protected EntityModel getCurrentUser() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        List<EntityModel> users = new ArrayList<>(octane.entityList("workspace_users").get().execute());

        for (EntityModel user : users) {
            if (user.getValue("email").getValue().toString().equals(connectionSettingsProvider.getConnectionSettings().getUserName())) {
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
        return new ArrayList<>(octane.entityList("user_roles").get().execute());
    }

    /**
     * Returns all the workspace users
     *
     * @return - a list of entityModels representing the workspace users
     */
    public List<EntityModel> getUsers() {
        EntityService entityService = serviceModule.getInstance(EntityService.class);
        Set<String> roles = new HashSet<>();
        roles.add("roles");

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
        return octane.entityList("releases").get().execute().iterator().next();
    }

    protected boolean isNewerOctane() {
        OctaneVersion version = versionService.getOctaneVersion();

        return (OctaneVersion.compare(version, OctaneVersion.Operation.HIGHER, OctaneVersion.EVERTON_P3));
    }

    private void createRelease() {
        String postUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + "/workspaces/" +
                connectionSettingsProvider.getConnectionSettings().getWorkspaceId() + "/releases";
        String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
        JSONObject dataSet = new JSONObject();
        JSONObject releaseJson = new JSONObject();
        releaseJson.put("name", "test_Release" + UUID.randomUUID().toString());
        releaseJson.put("type", "release");
        LocalDateTime localDateTImeNow = LocalDateTime.now();
        releaseJson.put("start_date", localDateTImeNow.toString() + "Z");
        releaseJson.put("end_date", localDateTImeNow.toString() + "Z");
        JSONObject agileTypeJson = new JSONObject();
        if (isNewerOctane()) {
            agileTypeJson.put("id", "list_node.release_agile_type.scrum");
        } else {
            agileTypeJson.put("id", "1108");
        }
        agileTypeJson.put("name", "scrum");
        agileTypeJson.put("type", "list_node");
        agileTypeJson.put("logical_name", "list_node.release_agile_type.scrum");
        releaseJson.put("agile_type", agileTypeJson);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(releaseJson);
        dataSet.put("data", jsonArray);
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

    /**
     * Creates a Task
     *
     * @param userStory - user story to attach the task to
     * @param taskName  - the name of the task
     * @return the built entityModel
     */
    protected EntityModel createTask(EntityModel userStory, String taskName) {
        EntityModel taskEntityModel = new EntityModel("type", "task");
        taskEntityModel.setValue(new StringFieldModel("name", taskName));
        taskEntityModel.setValue(new ReferenceFieldModel("story", userStory));
        Entity entity = Entity.getEntityType(taskEntityModel);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(taskEntityModel)).execute().iterator().next();
    }

    private List<EntityModel> getRequirements() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList("requirements").get().execute());
    }


    protected EntityModel createRequirement(String requirementName, EntityModel parent) {
        EntityModel phase = new EntityModel("type", "phase");
        phase.setValue(new StringFieldModel("id", "phase.requirement_document.draft"));
        phase.setValue(new StringFieldModel("name", "Draft"));
        phase.setValue(new StringFieldModel("logical_name", "phase.requirement_document.draft"));
        EntityModel requirement = new EntityModel("type", "requirement");
        requirement.setValue(new StringFieldModel("name", requirementName));
        requirement.setValue(new StringFieldModel("subtype", "requirement_document"));
        requirement.setValue(new ReferenceFieldModel("parent", parent));
        requirement.setValue(new ReferenceFieldModel("phase", phase));
        Entity entity = Entity.getEntityType(requirement);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(requirement)).execute().iterator().next();
    }

    protected EntityModel createRequirementFolder(String folderName) {
        EntityModel requirement = new EntityModel("type", "requirement");
        requirement.setValue(new StringFieldModel("name", folderName));
        requirement.setValue(new StringFieldModel("subtype", "requirement_folder"));
        requirement.setValue(new ReferenceFieldModel("parent", getRequirementsRoot()));
        Entity entity = Entity.getEntityType(requirement);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(requirement)).execute().iterator().next();
    }

    protected List<EntityModel> getTasks() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return new ArrayList<>(octane.entityList("tasks").get().execute());
    }

    /**
     * Creates a manual test run
     *
     * @param manualTest - the Manual Test to which the test run is planned
     * @param name       - the name of the run
     * @return the entityModel of the run
     */
    protected EntityModel createManualRun(EntityModel manualTest, String name) {

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
     * Creates a Test Suite
     *
     * @param name the name of the test suite
     * @return the entityModel of the test suite, @null if not created
     */
    protected EntityModel createTestSuite(String name) {
        EntityModel testSuite = new EntityModel("type", "test");
        testSuite.setValue(new StringFieldModel("name", name));
        testSuite.setValue(new StringFieldModel("subtype", "test_suite"));
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
     * Creates an automated test
     *
     * @param testName - the name of the new automated test
     * @return the newly created automated test entityModel
     */
    protected EntityModel createAutomatedTest(String testName) {
        EntityModel automatedTest = new EntityModel("type", "test");
        automatedTest.setValue(new StringFieldModel("subtype", "test_automated"));
        automatedTest.setValue(new StringFieldModel("name", testName));
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
        updatedEntityModel.setValue(backlogItem.getValue("id"));
        updatedEntityModel.setValue(backlogItem.getValue("type"));
        updatedEntityModel.setValue(new ReferenceFieldModel("owner", owner));
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
        updatedEntityModel.setValue(backlogItem.getValue("id"));
        updatedEntityModel.setValue(backlogItem.getValue("type"));
        updatedEntityModel.setValue(new StringFieldModel("description", description));
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
        List<EntityModel> workItems = octane.entityList("work_items").get().query(Query.not("subtype", QueryMethod.EqualTo, "work_item_root").build()).execute().stream().collect(Collectors.toList());
        List<EntityModel> tests = new ArrayList<>(octane.entityList("tests").get().execute());
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
            String entityType = entityModel.getValue("type").getValue().toString();
            if ("work_item".equals(entityType)) {
                if (workItemsQuery != null) {
                    workItemsQuery = workItemsQuery.or("id", QueryMethod.EqualTo, entityModel.getValue("id").getValue().toString());
                } else {
                    workItemsQuery = Query.statement("id", QueryMethod.EqualTo, entityModel.getValue("id").getValue().toString());
                }
            }
            if ("test".equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or("id", QueryMethod.EqualTo, entityModel.getValue("id").getValue().toString());
                } else {
                    testItemsQuery = Query.statement("id", QueryMethod.EqualTo, entityModel.getValue("id").getValue().toString());
                }
            }
            if ("run".equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or("id", QueryMethod.EqualTo, entityModel.getValue("id").getValue().toString());
                } else {
                    testItemsQuery = Query.statement("id", QueryMethod.EqualTo, entityModel.getValue("id").getValue().toString());
                }
            }
        }
        if (workspaceEntities.size() > 0) {

            Octane.Builder octaneBuilder = new Octane.Builder(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(), connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
            octaneBuilder.sharedSpace(connectionSettingsProvider.getConnectionSettings().getSharedSpaceId());
            octaneBuilder.workSpace(connectionSettingsProvider.getConnectionSettings().getWorkspaceId());
            Octane octane = octaneBuilder.Server(connectionSettingsProvider.getConnectionSettings().getBaseUrl()).build();
            if (testItemsQuery != null)
                octane.entityList("tests").delete().query(testItemsQuery.build()).execute();
            if (workItemsQuery != null)
                octane.entityList("work_items").delete().query(workItemsQuery.build()).execute();
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
        return (entity1.getValue("id").getValue().toString().equals(entity2.getValue("id").getValue().toString()));
    }

    protected EntityModel findRequirementById(long id) {
        List<EntityModel> requirements = getRequirements();
        for (EntityModel entityModel : requirements) {
            if (Long.parseLong(entityModel.getValue("id").getValue().toString()) == id) {
                return entityModel;
            }
        }
        return null;
    }

    private EntityModel getRequirementsRoot() {
        List<EntityModel> requirements = getRequirements();
        for (EntityModel entityModel : requirements) {
            if ("requirement_root".equals(entityModel.getValue("subtype").getValue().toString())) {
                return entityModel;
            }
        }
        return null;
    }
}
