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
package com.hpe.adm.octane.ideplugins.services.filtering;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants for sdk query builder use
 */
public enum Entity {

    WORK_ITEM("work_items", "work_item"),
    USER_STORY(Entity.WORK_ITEM, "story"),
    QUALITY_STORY(Entity.WORK_ITEM, "quality_story"),
    DEFECT(Entity.WORK_ITEM, "defect"),
    WORK_ITEM_ROOT(Entity.WORK_ITEM, "work_item_root"),
    EPIC(Entity.WORK_ITEM, "epic"),
    FEATURE(Entity.WORK_ITEM, "feature"),

    TEST("tests", "test"),
    MANUAL_TEST(Entity.TEST, "test_manual"),
    GHERKIN_TEST(Entity.TEST, "gherkin_test"),
    AUTOMATED_TEST(Entity.TEST, "test_automated"),
    TEST_SUITE(Entity.TEST, "test_suite"),

    TASK("tasks", "task"),
    PHASE("phases", "phases"),
    TRANSITION("transitions", "transition"),
    TEST_RUN("runs", "run"),
    MANUAL_TEST_RUN(TEST_RUN, "run_manual"),
    TEST_SUITE_RUN(TEST_RUN, "run_suite"),
    AUTOMATED_TEST_RUN(TEST_RUN, "run_automated"),

    COMMENT("comments", "comment"),

    WORKSPACE_USER("workspace_users", "workspace_user"),
    TEAM("teams", "team"),

    REQUIREMENT_BASE_ENTITY("requirements", "requirement"),
    REQUIREMENT_ROOT(Entity.REQUIREMENT_BASE_ENTITY, "requirement_root"),
    REQUIREMENT(Entity.REQUIREMENT_BASE_ENTITY, "requirement_document"),

    //Entity used to hold items added to my work
    USER_ITEM("user_items","user_item"),
    USER_TAG("user_tags", "user_tag"),
    
    LIST_NODE("list_nodes","list_node"),
    
    RELEASE("releases", "release"),
    
    SPRINT("sprints", "sprint"),
    
    PRODUCT_AREA("product_areas", "product_area"),
    
    TAXONOMY_NODE("taxonomy_nodes", "taxonomy_node"),
    TAXONOMY_ITEM_NODE(TAXONOMY_NODE, "taxonomy_item_node");
    
    //This is the name of the entity passed to the sdk, used for the rest, call, usually plural
    private String apiEntityName;

    //This is the type name that is returned with every entity
    private String typeName;

    //This is a marker that the entity is a subtype of another entity, (probably work_item),
    private Entity subtypeOf;

    //In case this is a subtype, you need to give the value of the subtype field to filter on
    private String subtypeFieldValue;

    Entity(String apiEntityName, String typeName) {
        this.apiEntityName = apiEntityName;
        this.typeName = typeName;
    }

    Entity(Entity subtypeOf, String subtypeName) {
        this.subtypeOf = subtypeOf;
        this.apiEntityName = subtypeOf.getApiEntityName();
        this.subtypeFieldValue = subtypeName;
    }

    public static Entity getEntityType(EntityModel entityModel) {

        if (entityModel.getValue("subtype") != null) {
            String subtype = entityModel.getValue("subtype").getValue().toString();

            //try finding the subtype
            if (subtype != null) {
                for (Entity entity : Entity.values()) {
                    if (entity.isSubtype() && entity.getSubtypeName().equals(subtype)) {
                        return entity;
                    }
                }
            }
        }

        if (entityModel.getValue("type") != null) {
            String type = entityModel.getValue("type").getValue().toString();

            //try finding the subtype
            if (type != null) {
                for (Entity entity : Entity.values()) {
                    if (!entity.isSubtype() && entity.getTypeName().equals(type)) {
                        return entity;
                    }
                }
            }

            //ClientType.HPE_REST_API_TECH_PREVIEW returns a subtype string in the type field, very cool
            //Try searching that way too
            String subtype = entityModel.getValue("type").getValue().toString();
            //try finding the subtype
            if (subtype != null) {
                for (Entity entity : Entity.values()) {
                    if (entity.isSubtype() && entity.getSubtypeName().equals(subtype)) {
                        return entity;
                    }
                }
            }
        }

        return null;
    }


    public static Entity getEntityType(String type) {

        for (Entity entity : Entity.values()) {
            if (!entity.isSubtype() && entity.getTypeName().equals(type)) {
                return entity;
            }
        }

        for (Entity entity : Entity.values()) {
            if (entity.isSubtype() && entity.getSubtypeName().equals(type)) {
                return entity;
            }
        }

        return null;
    }

    public static Set<Entity> getSubtypes(Entity entity) {
        if (entity.isSubtype()) {
            return Collections.emptySet();
        } else {
            Set<Entity> result = new HashSet<>();
            for (Entity subType : Entity.values()) {
                if (entity.equals(subType.getSubtypeOf())) {
                    result.add(subType);
                }
            }
            return result;
        }
    }

    public boolean isSubtype() {
        return subtypeOf != null;
    }

    public Entity getSubtypeOf() {
        return subtypeOf;
    }

    public String getSubtypeName() {
        return subtypeFieldValue;
    }

    public String getTypeName() {
        if (isSubtype()) {
            return subtypeOf.getTypeName();
        } else {
            return typeName;
        }
    }

    public String getEntityName() {
        if (isSubtype()) {
            return subtypeFieldValue;
        } else {
            return typeName;
        }
    }

    public String getApiEntityName() {
        return apiEntityName;
    }

    public Query.QueryBuilder createMatchSubtypeQueryBuilder() {
        if (isSubtype()) {
            return Query.statement("subtype", QueryMethod.EqualTo, getSubtypeName());
        }
        throw new RuntimeException("Entity " + apiEntityName + "is not a subtype");
    }

}