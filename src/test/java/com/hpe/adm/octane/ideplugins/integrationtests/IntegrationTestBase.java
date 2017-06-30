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
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.ideplugins.integrationtests.util.PropertyUtil;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import org.junit.Before;

/**
 * Enables the use of the {@link Inject} annotation
 */
public abstract class IntegrationTestBase {

    protected EntityGenerator entityGenerator;

    @Before
    public void setup () {

        ConnectionSettingsProvider connectionSettings = PropertyUtil.readFormVmArgs() != null ?
                PropertyUtil.readFormVmArgs() : PropertyUtil.readFromPropFile();

        if(connectionSettings == null){
            throw new RuntimeException("Cannot retrieve connection settings from either vm args or prop file, cannot run tests");
        }

        Injector injector = Guice.createInjector(new ServiceModule(connectionSettings));
        injector.injectMembers(this);
        entityGenerator = new EntityGenerator(injector.getInstance(OctaneProvider.class));
    }

}
