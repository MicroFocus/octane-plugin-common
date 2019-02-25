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
package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.ideplugins.integrationtests.util.PropertyUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.util.UserUtils;
import com.hpe.adm.octane.ideplugins.services.UserService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * Enables the use of the {@link Inject} annotation
 */
public class IntegrationTestBase {

    @Inject
    private EntitySearchService searchService;

    @Inject
    protected OctaneVersionService versionService;

    @Inject
    protected HttpClientProvider httpClientProvider;

    @Inject
    protected OctaneProvider octaneProvider;

    @Inject
    protected EntityGenerator entityGenerator;

    @Inject
    protected UserService userService;

    protected UserUtils userUtils;

    static final Set<Entity> searchEntityTypes = new LinkedHashSet<>(Arrays.asList(
            Entity.EPIC,
            Entity.FEATURE,
            Entity.USER_STORY,
            Entity.QUALITY_STORY,
            Entity.DEFECT,
            Entity.TASK,
            Entity.TEST_SUITE,
            Entity.MANUAL_TEST,
            Entity.AUTOMATED_TEST,
            Entity.GHERKIN_TEST,
            Entity.REQUIREMENT));

    protected ConnectionSettingsProvider connectionSettingsProvider;
    private ServiceModule serviceModule;
    private EntityModel nativeStatus;

    private static String WORKSPACE_NAME = Constants.Workspace.NAME_VALUE + " : " + LocalDateTime.now();
    private static Long CreatedWorkspaceId = null;
    /**
     * Sets up a context needed for the tests, the context is derived from the
     * annotations set on the implementing class
     */
    @Before
    public void setUp() {
        connectionSettingsProvider = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
        if (connectionSettingsProvider == null) {
            throw new RuntimeException(Constants.Errors.CONNECTION_SETTINGS_RETRIEVE_ERROR);
        }
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();

        if (connectionSettings.getWorkspaceId() == null) {
            connectionSettings.setWorkspaceId(createWorkSpace());
            connectionSettingsProvider.setConnectionSettings(connectionSettings);
        } else {
            // todo clean up workspace
        }

        serviceModule = new ServiceModule(connectionSettingsProvider);
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);

        nativeStatus = new EntityModel(Constants.TYPE, Constants.NativeStatus.NATIVE_STATUS_TYPE_VALUE);

        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.HIGHER, OctaneVersion.GENT_P3)) {
            nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_RUN_ID));
        }
        if (OctaneVersion.isBetween(versionService.getOctaneVersion(), OctaneVersion.EVERTON_P3, OctaneVersion.GENT_P3, false)) {
            nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_NEW_ID));
        }
        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.LOWER_EQ, OctaneVersion.EVERTON_P3)) {
            nativeStatus.setValue(new StringFieldModel(Constants.ID, Constants.NativeStatus.NATIVE_STATUS_OLD_ID));
        }

        //createRelease();
    }

    /**
     * Creates a new workspace and stores the id of it for later use
     *
     * @return the workspace_id of the created workspace
     */
    private Long createWorkSpace() {
        if(CreatedWorkspaceId == null) {
            String postUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + Constants.SHARED_SPACE +
                    connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + Constants.WORKSPACES;
            String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
            JSONObject dataSet = new JSONObject();
            JSONObject credentials = new JSONObject();
            credentials.put(Constants.NAME, WORKSPACE_NAME);
            credentials.put(Constants.DESCRIPTION, Constants.Workspace.DESCRIPTION);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(credentials);
            dataSet.put(Constants.DATA, jsonArray);
            OctaneHttpRequest postNewWorkspaceRequest = new OctaneHttpRequest.PostOctaneHttpRequest(postUrl, OctaneHttpRequest.JSON_CONTENT_TYPE,
                    dataSet.toString());
            OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
            octaneHttpClient.authenticate(connectionSettingsProvider.getConnectionSettings().getAuthentication());
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
            CreatedWorkspaceId = workspace.getLong(Constants.Workspace.WORKSPACE_ID);
        }
        return CreatedWorkspaceId;
    }



    /**
     * Removes an entity
     *
     * @param entityModel
     *            - the entityModel to be deleted
     */
    public void deleteEntity(EntityModel entityModel) {
        entityGenerator.deleteEntityModel(entityModel);
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

    private void createRelease() {
        String postUrl =
                connectionSettingsProvider.getConnectionSettings().getBaseUrl() + Constants.SHARED_SPACE +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + Constants.WORKSPACES + "/" +
                connectionSettingsProvider.getConnectionSettings().getWorkspaceId() + Constants.RELEASES;

        JSONObject dataSet = new JSONObject();
        JSONObject releaseJson = new JSONObject();
        releaseJson.put(Constants.NAME, Constants.Release.NAME + UUID.randomUUID().toString());
        releaseJson.put(Constants.TYPE, Constants.Release.TYPE);
        LocalDateTime localDateTImeNow = LocalDateTime.now();
        releaseJson.put(Constants.Release.START_DATE, localDateTImeNow.toString() + "Z");
        releaseJson.put(Constants.Release.END_DATE, localDateTImeNow.toString() + "Z");
        JSONObject agileTypeJson = new JSONObject();

        if (OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.HIGHER_EQ, OctaneVersion.EVERTON_P3)) {
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

        OctaneHttpRequest postNewReleaseRequest = new OctaneHttpRequest.PostOctaneHttpRequest(
                postUrl,
                OctaneHttpRequest.JSON_CONTENT_TYPE,
                dataSet.toString());

        try {
            httpClientProvider.getOctaneHttpClient().execute(postNewReleaseRequest);
        } catch (Exception e) {
            fail(e.toString());
        }
    }


    public void deleteRelease() {
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        octane.entityList(Constants.Release.RELEASES)
                .delete()
                .query(Query.statement(Constants.ID, QueryMethod.EqualTo, getRelease().getValue(Constants.ID).getValue().toString()).build())
                .execute();
    }

    /**
     * Creates a Task
     *
     * @param userStory
     *            - user story to attach the task to
     * @param taskName
     *            - the name of the task
     * @return the built entityModel
     */
    protected EntityModel createTask(EntityModel userStory, String taskName) {
        EntityModel taskEntityModel = new EntityModel(Constants.TYPE, Entity.TASK.getEntityName());
        taskEntityModel.setValue(new StringFieldModel(Constants.NAME, taskName));
        taskEntityModel.setValue(new ReferenceFieldModel(Entity.USER_STORY.getSubtypeName(), userStory));
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
        EntityModel requirement = new EntityModel(Constants.TYPE, Entity.REQUIREMENT_BASE_ENTITY.getEntityName());
        requirement.setValue(new StringFieldModel(Constants.NAME, requirementName));
        requirement.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.REQUIREMENT.getEntityName()));
        requirement.setValue(new ReferenceFieldModel(Constants.PARENT, parent));
        requirement.setValue(new ReferenceFieldModel(Constants.PHASE, phase));
        Entity entity = Entity.getEntityType(requirement);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(requirement)).execute().iterator().next();
    }

    protected EntityModel createRequirementFolder(String folderName) {
        EntityModel requirement = new EntityModel(Constants.TYPE, Entity.REQUIREMENT_BASE_ENTITY.getEntityName());
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
        return new ArrayList<>(octane.entityList(Entity.TASK.getApiEntityName()).get().execute());
    }

    /**
     * Creates a manual test run
     *
     * @param manualTest
     *            - the Manual Test to which the test run is planned
     * @param name
     *            - the name of the run
     * @return the entityModel of the run
     */
    protected EntityModel createManualRun(EntityModel manualTest, String name) {
        EntityModel manualRun = new EntityModel(Constants.TYPE, Entity.TEST_RUN.getEntityName());
        manualRun.setValue(new StringFieldModel(Constants.NAME, name));
        manualRun.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.MANUAL_TEST_RUN.getEntityName()));
        manualRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, nativeStatus));
        manualRun.setValue(new ReferenceFieldModel(Constants.Release.TYPE, getRelease()));
        manualRun.setValue(new ReferenceFieldModel(Entity.TEST.getEntityName(), manualTest));
        Entity entity = Entity.getEntityType(manualRun);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        try {
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
     * @param name
     *            the name of the test suite
     * @return the entityModel of the test suite, @null if not created
     */
    protected EntityModel createTestSuite(String name) {
        EntityModel testSuite = new EntityModel(Constants.TYPE, Entity.TEST.getSubtypeName());
        testSuite.setValue(new StringFieldModel(Constants.NAME, name));
        testSuite.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.TEST_SUITE.getSubtypeName()));
        Entity entity = Entity.getEntityType(testSuite);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();

        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuite)).execute().iterator().next();
    }

    /**
     * Creates a test suite run
     *
     * @param testSuite
     *            the test suite which is used to create the test suite run
     * @param testSuiteRunName
     *            the name of the suite run
     * @return the created entityModel of the suite run
     */
    protected EntityModel createTestSuiteRun(EntityModel testSuite, String testSuiteRunName) {
        EntityModel testSuiteRun = new EntityModel(Constants.TYPE, Entity.TEST_RUN.getEntityName());
        testSuiteRun.setValue(new StringFieldModel(Constants.NAME, testSuiteRunName));
        testSuiteRun.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.TEST_SUITE_RUN.getEntityName()));
        testSuiteRun.setValue(new ReferenceFieldModel(Constants.NATIVE_STATUS, nativeStatus));
        testSuiteRun.setValue(new ReferenceFieldModel(Constants.Release.TYPE, getRelease()));
        testSuiteRun.setValue(new ReferenceFieldModel(Entity.TEST.getEntityName(), testSuite));
        Entity entity = Entity.getEntityType(testSuiteRun);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(testSuiteRun)).execute().iterator().next();
    }

    /**
     * Creates an automated test
     *
     * @param testName
     *            - the name of the new automated test
     * @return the newly created automated test entityModel
     */
    protected EntityModel createAutomatedTest(String testName) {
        EntityModel automatedTest = new EntityModel(Constants.TYPE, Entity.TEST.getEntityName());
        automatedTest.setValue(new StringFieldModel(Constants.SUBTYPE, Entity.AUTOMATED_TEST.getEntityName()));
        automatedTest.setValue(new StringFieldModel(Constants.NAME, testName));
        Entity entity = Entity.getEntityType(automatedTest);
        OctaneProvider octaneProvider = serviceModule.getOctane();
        Octane octane = octaneProvider.getOctane();
        return octane.entityList(entity.getApiEntityName()).create().entities(Collections.singletonList(automatedTest)).execute().iterator().next();
    }

    /**
     * Adds an entity into the my work section
     *
     * @param entityModel
     *            - the entity to be added
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
     * Sets the description of an entity
     *
     * @param backlogItem
     *            the backlog item
     * @param description
     *            the description string
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
        List<EntityModel> workItems = new ArrayList<>(octane.entityList(Entity.WORK_ITEM.getApiEntityName()).get()
                .query(Query.not(Constants.SUBTYPE, QueryMethod.EqualTo, Constants.WORK_ITEM_ROOT).build()).execute());
        List<EntityModel> tests = new ArrayList<>(octane.entityList(Entity.TEST.getApiEntityName()).get().execute());
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
            if (Entity.WORK_ITEM.getEntityName().equals(entityType)) {
                if (workItemsQuery != null) {
                    workItemsQuery = workItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    workItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
            if (Entity.TEST.getEntityName().equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    testItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
            if (Entity.TEST_RUN.getEntityName().equals(entityType)) {
                if (testItemsQuery != null) {
                    testItemsQuery = testItemsQuery.or(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                } else {
                    testItemsQuery = Query.statement(Constants.ID, QueryMethod.EqualTo, entityModel.getValue(Constants.ID).getValue().toString());
                }
            }
        }
        if (workspaceEntities.size() > 0) {
            if (testItemsQuery != null)
                octaneProvider.getOctane().entityList(Entity.TEST.getApiEntityName()).delete().query(testItemsQuery.build()).execute();
            if (workItemsQuery != null)
                octaneProvider.getOctane().entityList(Entity.WORK_ITEM.getApiEntityName()).delete().query(workItemsQuery.build()).execute();
        }
    }

    protected EntityModel search(String searchField, String query) {
        Collection<EntityModel> searchResults = searchService.searchGlobal(
                query,
                20,
                searchEntityTypes.toArray(new Entity[] {}));

        for (EntityModel entityModel : searchResults) {
            if (removeTags(entityModel.getValue(searchField).getValue().toString()).contains(query)) {
                return entityModel;
            }
        }

        return null;
    }

    private String removeTags(String s) {
        String result;
        if (s.contains("<em>")) {
            s = StringEscapeUtils.unescapeHtml(s);
            result = s.replaceAll("<em>", "");
            result = result.replaceAll("</em>", "");
            return result;
        }
        return s;
    }

    protected boolean compareEntities(EntityModel entity1, EntityModel entity2) {
        if((entity1.getValue("id").getValue().toString().equals(entity2.getValue(Constants.ID).getValue().toString())) 
                && entity1.getValue("type").getValue().toString().equals(entity2.getValue(Constants.TYPE).getValue().toString())) {
            return true;
        }
        return false;
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
