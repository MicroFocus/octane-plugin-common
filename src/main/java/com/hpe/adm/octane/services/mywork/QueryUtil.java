package com.hpe.adm.octane.services.mywork;

import com.hpe.adm.nga.sdk.Query;
import com.hpe.adm.nga.sdk.QueryMethod;
import com.hpe.adm.octane.services.filtering.Entity;

class QueryUtil {

    /**
     * Constructs a metaphase query builder to match "logical_name":"metaphase.entity.phasename",
     *
     * @param entity
     * @param phases
     * @return
     */
    public static Query.QueryBuilder createPhaseQuery(Entity entity, String... phases) {
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
    public static Query.QueryBuilder createNativeStatusQuery(String... logicalNames) {
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

    public static Query.QueryBuilder createUserQuery(String fieldName, Long userId) {
        return  Query.statement(fieldName, QueryMethod.EqualTo,
                Query.statement("id", QueryMethod.EqualTo, userId));
    }

}