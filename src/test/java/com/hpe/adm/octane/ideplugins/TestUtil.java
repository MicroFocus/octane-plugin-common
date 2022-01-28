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
package com.hpe.adm.octane.ideplugins;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;

import java.util.Collection;

public class TestUtil {
    public static void printEntities(Collection<EntityModel> entities) {
        System.out.println("Entities size: " + entities.size());
        if (entities.size() != 0) {
            entities
                    .stream()
                    .map(entityModel -> {
                        if(Entity.USER_ITEM == Entity.getEntityType(entityModel)){
                            return MyWorkUtil.getEntityModelFromUserItem(entityModel);
                        }
                        return entityModel;
                    })
                    .map(em -> {
                        if (em.getValue("name") != null) {
                            return em.getValue("name").getValue().toString();
                        } else if (Entity.COMMENT == Entity.getEntityType(em)) {
                            return "(Comment)";
                        }
                        return "{???}";
                    })
                    .forEach(System.out::println);
        }
    }
}