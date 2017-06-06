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

package com.hpe.adm.octane.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.octane.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.services.MetadataService;
import com.hpe.adm.octane.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class MetadataServiceITCase extends IntegrationTestBase {

    @Inject
    MetadataService metadataService;

    @Test
    public void testGetFormLayoutsForEntityType() {
        try {
            Assert.assertNotNull(metadataService.getFormLayoutForSpecificEntityType(Entity.USER_STORY));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}

