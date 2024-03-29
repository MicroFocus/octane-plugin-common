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
package com.hpe.adm.octane.ideplugins.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.UserService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.util.HashMap;
import java.util.Map;

import static com.hpe.adm.octane.ideplugins.services.filtering.Entity.*;
import static com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil.*;

public class DynamoMyWorkFilterCriteria {

    @Inject
    private UserService userService;

    public Map<Entity, Query.QueryBuilder> getStaticFilterCriteria(){

        Map<Entity, Query.QueryBuilder> filterCriteria = new HashMap<>();

        filterCriteria.put(GHERKIN_TEST,
                createUserQuery("owner", userService.getCurrentUserId())
                        .and(GHERKIN_TEST.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(TEST, "new", "indesign"))
        );
        filterCriteria.put(MANUAL_TEST,
                createUserQuery("owner", userService.getCurrentUserId())
                        .and(MANUAL_TEST.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(TEST, "new", "indesign"))

        );
        filterCriteria.put(DEFECT,
                createUserQuery("owner", userService.getCurrentUserId())
                        .and(DEFECT.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(DEFECT, "new", "inprogress", "intesting"))
        );
        filterCriteria.put(USER_STORY,
                createUserQuery("owner", userService.getCurrentUserId())
                        .and(USER_STORY.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(USER_STORY, "new", "inprogress", "intesting"))
        );
        filterCriteria.put(QUALITY_STORY,
                createUserQuery("owner", userService.getCurrentUserId())
                        .and(QUALITY_STORY.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(QUALITY_STORY, "new", "inprogress"))
        );
        filterCriteria.put(TASK,
                createUserQuery("owner", userService.getCurrentUserId())
                        .and(createPhaseQuery(TASK, "new", "inprogress"))
        );
        filterCriteria.put(MANUAL_TEST_RUN,
                createUserQuery("run_by", userService.getCurrentUserId())
                        .and(MANUAL_TEST_RUN.createMatchSubtypeQueryBuilder())
                        .and(Query.statement("parent_suite", QueryMethod.EqualTo, null))
                        .and(createNativeStatusQuery("list_node.run_native_status.blocked", "list_node.run_native_status.not_completed"))
        );
        filterCriteria.put(TEST_SUITE_RUN,
                createUserQuery("run_by", userService.getCurrentUserId())
                        .and(TEST_SUITE_RUN.createMatchSubtypeQueryBuilder())
                        .and(createNativeStatusQuery("list_node.run_native_status.blocked", "list_node.run_native_status.not_completed", "list_node.run_native_status.planned")
                                .and( Query.statement("parent_suite", QueryMethod.EqualTo, null)))
        );

        filterCriteria.put(COMMENT, createUserQuery("mention_user", userService.getCurrentUserId()));

        return filterCriteria;
    }

}