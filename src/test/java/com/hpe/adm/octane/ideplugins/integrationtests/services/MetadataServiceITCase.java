/*
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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

package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.MetadataService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.DefaultEntityFieldsUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Test
    public void testMethodForVisibleFields(){
        Set<String> defaultFields = DefaultEntityFieldsUtil.getDefaultFields().get(Entity.USER_STORY);
        List<FieldMetadata> fieldMetadata =(List) metadataService.getVisibleFields(Entity.USER_STORY);
        Set<String> fields = fieldMetadata.stream().map(FieldMetadata::getName).collect(Collectors.toSet());
        boolean flag = true;
        for(String fieldName : defaultFields){
            if(!fields.contains(fieldName)){
                flag = false;
                break;
            }
        }
        assert flag;
    }

    @Test
    public void testGetFields(){
        Set<String> defaultFields = DefaultEntityFieldsUtil.getDefaultFields().get(Entity.USER_STORY);
        List<FieldMetadata> fieldMetadata =(List) metadataService.getFields(Entity.USER_STORY);
        Set<String> fields = fieldMetadata.stream().map(FieldMetadata::getName).collect(Collectors.toSet());
        boolean flag = true;
        for(String fieldName : defaultFields){
            if(!fields.contains(fieldName)){
                flag = false;
                break;
            }
        }
        assert flag;
    }

}

