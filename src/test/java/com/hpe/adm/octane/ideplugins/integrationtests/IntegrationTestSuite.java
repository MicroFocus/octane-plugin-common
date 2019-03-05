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

}
