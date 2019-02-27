package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.integrationtests.services.*;
import com.hpe.adm.octane.ideplugins.integrationtests.services.noentity.EntitySearchServiceITCase;
import com.hpe.adm.octane.ideplugins.integrationtests.services.noentity.OctaneVersionServiceITCase;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityUtils;
import com.hpe.adm.octane.ideplugins.integrationtests.util.PropertyUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.util.UserUtils;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkspaceUtils;
import com.hpe.adm.octane.ideplugins.services.UserService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.junit.After;
import org.junit.Before;
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

        WorkspaceUtils workspaceUtils = injector.getInstance(WorkspaceUtils.class);

        if (connectionSettings.getWorkspaceId() == null) {
            // todo try catch the parse
            connectionSettings.setWorkspaceId(Long.parseLong(workspaceUtils.createWorkSpace()));
            connectionSettingsProvider.setConnectionSettings(connectionSettings);
        }
    }


    @After
    public void tearDown() {

    }


}
