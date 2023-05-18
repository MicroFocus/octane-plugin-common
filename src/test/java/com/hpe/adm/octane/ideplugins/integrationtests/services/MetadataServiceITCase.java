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
package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.octane.ideplugins.integrationtests.TestServiceModule;
import com.hpe.adm.octane.ideplugins.services.MetadataService;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.DefaultEntityFieldsUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MetadataServiceITCase {

    @Inject
    MetadataService metadataService;

    @Before
    public void setUp() {
        ServiceModule serviceModule = TestServiceModule.getServiceModule();
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
    }

    @Test
    public void testMethodForVisibleFields() throws UnsupportedEncodingException {
        Collection<String> defaultFields = DefaultEntityFieldsUtil.getDefaultFields().get(Entity.USER_STORY);
        Collection<String> returnedFields = metadataService.getVisibleFields(Entity.USER_STORY).stream().map(FieldMetadata::getName).collect(Collectors.toCollection(HashSet::new));
        assert defaultFields.stream().allMatch(returnedFields::contains);
    }

    @Test
    public void testGetFields(){
        Collection<String> defaultFields = DefaultEntityFieldsUtil.getDefaultFields().get(Entity.USER_STORY);
        Collection<String> returnedFields = metadataService.getFields(Entity.USER_STORY).stream().map(FieldMetadata::getName).collect(Collectors.toCollection(HashSet::new));
        assert defaultFields.stream().allMatch(returnedFields::contains);
    }
    
    @Test
    public void testGetVisibleFieldParallel(){   
        Arrays.stream(Entity.values())
            .parallel()
            .forEach(entity -> {
                try {
                    metadataService.getVisibleFields(entity);
                } catch (Exception ex) {
                    Assert.fail("getVisibleFields failed for entity: " + entity + ": " + ex);
                }
            });
    }

}