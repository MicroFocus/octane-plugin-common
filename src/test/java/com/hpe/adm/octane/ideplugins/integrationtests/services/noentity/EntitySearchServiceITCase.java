/*
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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
package com.hpe.adm.octane.ideplugins.integrationtests.services.noentity;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.octane.ideplugins.TestUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.TestServiceModule;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import org.junit.Before;
import org.junit.Test;

public class EntitySearchServiceITCase {

    @Inject
    private EntitySearchService searchService;

    @Before
    public void setUp() {
        ServiceModule serviceModule = TestServiceModule.getServiceModule();
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
    }

    @Test
    public void testGlobalSearch() {
        //Just make sure this doesn't blow up for now
        TestUtil.printEntities(searchService.searchGlobal("req", 25, Entity.REQUIREMENT));
    }
}
