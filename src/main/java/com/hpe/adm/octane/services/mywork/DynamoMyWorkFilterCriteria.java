package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.services.UserService;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.HashMap;
import java.util.Map;

import static com.hpe.adm.octane.services.filtering.Entity.*;
import static com.hpe.adm.octane.services.mywork.MyWorkUtil.*;

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