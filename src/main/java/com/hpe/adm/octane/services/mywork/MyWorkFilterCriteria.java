package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.octane.services.UserService;
import com.hpe.adm.octane.services.filtering.Entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.hpe.adm.octane.services.filtering.Entity.*;
import static com.hpe.adm.octane.services.mywork.MyWorkUtil.*;

public class MyWorkFilterCriteria {

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
                        .and(createNativeStatusQuery("list_node.run_native_status.blocked", "list_node.run_native_status.not_completed", "list_node.run_native_status.planned"))
                        .and(
                            Query.statement("parent_suite", QueryMethod.EqualTo, null)
                            .or(
                            Query.statement("parent_suite", QueryMethod.EqualTo, Query.statement("run_by", QueryMethod.EqualTo, null))
                            )
                        )
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

    public Map<Entity, Query.QueryBuilder> getServersideFilterCriteria(){

        Map<Entity, Query.QueryBuilder> filterCriteria = new HashMap<>();

        Entity[] simpleEntities = new Entity[]{
                GHERKIN_TEST, MANUAL_TEST,
                MANUAL_TEST_RUN, TEST_SUITE_RUN,
                WORK_ITEM,
                TASK };

        Query.QueryBuilder query =
                Query.statement("user_item", QueryMethod.EqualTo,
                    Query.statement("user", QueryMethod.EqualTo,
                        Query.statement("id", QueryMethod.EqualTo, userService.getCurrentUserId())
                    )
                );

        Arrays
                .stream(simpleEntities)
                .forEach(entity -> filterCriteria.put(entity, query));

        filterCriteria.put(COMMENT, createUserQuery("mention_user", userService.getCurrentUserId()));

        return filterCriteria;
    }

}