package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.integrationtests.services.*;
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
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CommentServiceITCase.class,
        ConnectionSettingsITCase.class,
        EntityLabelServiceITCase.class,
        EntityServiceITCase.class,
        GherkinTestDownloadITCase.class,
        MetadataServiceITCase.class,
        MyWorkTreeITCase.class,
        RequirementsITCase.class,
        SearchFunctionalityITCase.class
})


public class IntegrationTestSuite {

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

    @Inject
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
    }


    @After
    public void tearDown() {

    }


}
