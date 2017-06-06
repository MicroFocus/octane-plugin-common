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

package com.hpe.adm.octane.integrationtests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.octane.integrationtests.util.ConfigurationUtil;
import com.hpe.adm.octane.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.di.ServiceModule;
import org.junit.Before;

/**
 * Enables the use of the {@link Inject} annotation
 */
public abstract class IntegrationTestBase {

    private Injector injector;

    protected EntityGenerator entityGenerator;

    @Before
    public void setup () {
        injector = Guice.createInjector(new ServiceModule(readConnectionSettingsFromFile()));
        injector.injectMembers(this);
        entityGenerator = new EntityGenerator(injector.getInstance(OctaneProvider.class));
    }

    private ConnectionSettingsProvider readConnectionSettingsFromFile(){
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setBaseUrl(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.URL));
        connectionSettings.setSharedSpaceId(ConfigurationUtil.getLong(ConfigurationUtil.PropertyKeys.SHAREDSPACE));
        connectionSettings.setWorkspaceId(ConfigurationUtil.getLong(ConfigurationUtil.PropertyKeys.WORKSPACE));
        connectionSettings.setUserName(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.USERNAME));
        connectionSettings.setPassword(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.PASSWORD));
        return new BasicConnectionSettingProvider(connectionSettings);
    }

}
