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

public class MyWorkFilterCriteria {

    @Inject
    private UserService userService;

    public Map<Entity, Query.QueryBuilder> getStaticFilterCriteria(){

        Map<Entity, Query.QueryBuilder> filterCriteria = new HashMap<>();

        filterCriteria.put(GHERKIN_TEST,
                createCurrentUserQuery("owner")
                        .and(GHERKIN_TEST.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(TEST, "new", "indesign"))
        );
        filterCriteria.put(MANUAL_TEST,
                createCurrentUserQuery("owner")
                        .and(MANUAL_TEST.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(TEST, "new", "indesign"))

        );
        filterCriteria.put(DEFECT,
                createCurrentUserQuery("owner")
                        .and(DEFECT.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(DEFECT, "new", "inprogress", "intesting"))
        );
        filterCriteria.put(USER_STORY,
                createCurrentUserQuery("owner")
                        .and(USER_STORY.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(USER_STORY, "new", "inprogress", "intesting"))
        );
        filterCriteria.put(QUALITY_STORY,
                createCurrentUserQuery("owner")
                        .and(QUALITY_STORY.createMatchSubtypeQueryBuilder())
                        .and(createPhaseQuery(QUALITY_STORY, "new", "inprogress"))
        );
        filterCriteria.put(TASK,
                createCurrentUserQuery("owner")
                        .and(createPhaseQuery(TASK, "new", "inprogress"))
        );
        filterCriteria.put(MANUAL_TEST_RUN,
                createCurrentUserQuery("run_by")
                        .and(MANUAL_TEST_RUN.createMatchSubtypeQueryBuilder())
                        .and(Query.statement("parent_suite", QueryMethod.EqualTo, null))
                        .and(createNativeStatusQuery("list_node.run_native_status.blocked", "list_node.run_native_status.not_completed"))
        );
        filterCriteria.put(TEST_SUITE_RUN,
                createCurrentUserQuery("run_by")
                        .and(TEST_SUITE_RUN.createMatchSubtypeQueryBuilder())
                        .and(createNativeStatusQuery("list_node.run_native_status.blocked", "list_node.run_native_status.not_completed")
                                .and( Query.statement("parent_suite", QueryMethod.EqualTo, null)))
        );

        filterCriteria.put(COMMENT, createCurrentUserQuery("mention_user"));

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

        filterCriteria.put(COMMENT, createCurrentUserQuery("mention_user"));

        return filterCriteria;
    }

    /**
     * Constructs a metaphase query builder to match "logical_name":"metaphase.entity.phasename",
     *
     * @param entity
     * @param phases
     * @return
     */
    private Query.QueryBuilder createPhaseQuery(Entity entity, String... phases) {
        Query.QueryBuilder phaseQueryBuilder = null;
        for (String phaseName : phases) {
            String phaseLogicalName = "metaphase." + entity.getTypeName() + "." + phaseName;
            Query.QueryBuilder currentPhaseQueryBuilder =
                    Query.statement("metaphase", QueryMethod.EqualTo,
                            Query.statement("logical_name", QueryMethod.EqualTo, phaseLogicalName)
                    );
            if (phaseQueryBuilder == null) {
                phaseQueryBuilder = currentPhaseQueryBuilder;
            } else {
                phaseQueryBuilder = phaseQueryBuilder.or(currentPhaseQueryBuilder);
            }
        }

        return Query.statement("phase", QueryMethod.EqualTo, phaseQueryBuilder);
    }

    /**
     * @param logicalNames
     * @return
     */
    private Query.QueryBuilder createNativeStatusQuery(String... logicalNames) {
        Query.QueryBuilder nativeStatusQueryBuilder = null;
        for (String logicalName : logicalNames) {
            Query.QueryBuilder currentNativeStatusQueryBuilder =
                    Query.statement("logical_name", QueryMethod.EqualTo, logicalName);
            if (nativeStatusQueryBuilder == null) {
                nativeStatusQueryBuilder = currentNativeStatusQueryBuilder;
            } else {
                nativeStatusQueryBuilder = nativeStatusQueryBuilder.or(currentNativeStatusQueryBuilder);
            }
        }
        return  Query.statement("native_status", QueryMethod.EqualTo, nativeStatusQueryBuilder);
    }

    private Query.QueryBuilder createCurrentUserQuery(String fieldName) {
        return  Query.statement(fieldName, QueryMethod.EqualTo,
                Query.statement("id", QueryMethod.EqualTo, userService.getCurrentUserId()));
    }

}