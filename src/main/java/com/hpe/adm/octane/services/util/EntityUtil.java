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

package com.hpe.adm.octane.services.util;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.services.filtering.Entity;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityUtil {

    public static boolean areEqual(EntityModel leftSide, PartialEntity rightSide) {
        if (!ObjectUtils.equals(Entity.getEntityType(leftSide), rightSide.getEntityType())) {
            return false;
        }

        String em1Id = "";
        if(leftSide.getValue("id") != null){
            em1Id = leftSide.getValue("id").getValue().toString();
        }

        String em2Id = "";
        if(rightSide.getEntityId() != null){
            em2Id = rightSide.getEntityId().toString();
        }

        if(!em1Id.equals(em2Id)){
            return false;
        }

        return true;
    }

    public static boolean areEqual(EntityModel leftSide, EntityModel rightSide){
        if(!ObjectUtils.equals(Entity.getEntityType(leftSide), Entity.getEntityType(rightSide))){
            return false;
        }

        String em1Id = "";
        if(leftSide.getValue("id") != null){
            em1Id = leftSide.getValue("id").getValue().toString();
        }

        String em2Id = "";
        if(rightSide.getValue("id") != null){
            em2Id = rightSide.getValue("id").getValue().toString();
        }

        if(!em1Id.equals(em2Id)){
            return false;
        }

        return true;
    }

    public static boolean containsEntityModel(Collection<EntityModel> collection, EntityModel entityModel){
        for(EntityModel em : collection){
            if(areEqual(em, entityModel)){
                return true;
            }
        }
        return false;
    }

    public static boolean removeEntityModel(Collection<EntityModel> collection, EntityModel entityModel){
        List<EntityModel> toBeRemoved = new ArrayList<>();
        for(EntityModel em : collection){
            if(areEqual(em, entityModel)){
                toBeRemoved.add(em);
            }
        }

        if(toBeRemoved.size()==0){
            return false;
        } else {
            collection.removeAll(toBeRemoved);
            return true;
        }
    }



}