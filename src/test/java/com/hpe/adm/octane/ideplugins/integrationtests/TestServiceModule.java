/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.integrationtests.util.PropertyUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.util.WorkspaceUtils;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import org.junit.Assert;

public class TestServiceModule {

    private static ServiceModule serviceModule;

    static {
        initTestServiceModuleIfNeeded();
    }

    private static void initTestServiceModuleIfNeeded() {
        if(TestServiceModule.getServiceModule() != null ){
            return;
        }

        ConnectionSettingsProvider connectionSettingsProvider;
        try{
            connectionSettingsProvider = PropertyUtil.readFormVmArgs() != null ? PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();
            if (connectionSettingsProvider == null) {
                Assert.fail(Constants.Errors.CONNECTION_SETTINGS_RETRIEVE_ERROR);
            }
        } catch (Exception ex) {
            Assert.fail(Constants.Errors.CONNECTION_SETTINGS_RETRIEVE_ERROR);
            return;
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

    public static void setServiceModule(ServiceModule serviceModule) {
        TestServiceModule.serviceModule = serviceModule;
    }

    public static ServiceModule getServiceModule() {
        return serviceModule;
    }
}
