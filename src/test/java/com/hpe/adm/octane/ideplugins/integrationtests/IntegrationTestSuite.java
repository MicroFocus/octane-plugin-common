package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.integrationtests.services.*;
import com.hpe.adm.octane.ideplugins.integrationtests.services.noentity.EntitySearchServiceITCase;
import com.hpe.adm.octane.ideplugins.integrationtests.services.noentity.OctaneVersionServiceITCase;
import com.hpe.adm.octane.ideplugins.integrationtests.util.PropertyUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkspaceUtils;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        EntitySearchServiceITCase.class,
        OctaneVersionServiceITCase.class,
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

    /**
     * Sets up a context needed for the tests, the context is derived from the
     * annotations set on the implementing class
     */
    @BeforeClass
    public static void setUp() {
        ConnectionSettingsProvider connectionSettingsProvider = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
        if (connectionSettingsProvider == null) {
            throw new RuntimeException(Constants.Errors.CONNECTION_SETTINGS_RETRIEVE_ERROR);
        }
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();

        ServiceModule serviceModule = new ServiceModule(connectionSettingsProvider);
        Injector injector = Guice.createInjector(serviceModule);

        // set up service module to be used by all tests
        TestServiceModule.setServiceModule(serviceModule);

        WorkspaceUtils workspaceUtils = injector.getInstance(WorkspaceUtils.class);
        if (connectionSettings.getWorkspaceId() == null) {
            try {
                connectionSettings.setWorkspaceId(Long.parseLong(workspaceUtils.createWorkSpace()));
                connectionSettingsProvider.setConnectionSettings(connectionSettings);
            } catch (Exception e) {
                Assert.fail("Failed to set up new workspace, aborting test suite...");
            }

        }
    }

    @After
    public void tearDown() {

    }


}
