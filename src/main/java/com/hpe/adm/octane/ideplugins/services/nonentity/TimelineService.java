package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.DateFieldModel;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

public class TimelineService {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME= "name";
    private static final String FIELD_IS_DEFAULT = "is_default";
    private static final String FIELD_START_DATE= "start_date";
    private static final String FIELD_END_DATE = "end_date";
    private static final String FIELD_NUM_WD_LEFT = "number_of_remaining_work_days";

    @Inject
    protected OctaneProvider octaneProvider;

    @Inject
    protected OctaneVersionService versionService;

    /**
     * Returns the current timeline as a string. <br>
     * Consists of the current default release (if it exists). <br>
     * If there's no current default release, it will pick the first current release. <br>
     * If there's no current release than it will return nothing. <br>
     * The current sprint of the current release if it exists. <br>
     * Will return nothing for octane versions lesser than {@link OctaneVersion#JUVENTUS_P3} <br>
     * @return a timeline string that looks like: "[ Release: (release_name) (NOd left) [ | Sprint: (sprint_name) ((NO)wd left) ] ]
     */
    public String getTimelineString() {

        String result = "";

        // Untested further back, don't want to support it
        if(versionService.getOctaneVersion().isLessThan(OctaneVersion.JUVENTUS_P3)) {
            return result;
        }

        EntityModel shownRelease = getReleaseToShowInTimeline();
        result += convertReleaseToString(shownRelease);

        EntityModel shownSprint = getCurrentSprintInRelease(shownRelease);
        result += shownSprint != null ? " | " + convertSprintToString(shownSprint) : "";

        return result;
    }

    private Collection<EntityModel> getCurrentReleases() {

        Query.QueryBuilder queryBuilder = createCurrentDateQueryBuilder();

        return octaneProvider
                .getOctane()
                .entityList(Entity.RELEASE.getApiEntityName())
                .get()
                .query(queryBuilder.build())
                .addFields(FIELD_ID, FIELD_NAME, FIELD_IS_DEFAULT, FIELD_START_DATE, FIELD_END_DATE)
                .execute();
    }

    private EntityModel findDefaultRelease(Collection<EntityModel> releaseEntities) {
        return releaseEntities
                .stream()
                .filter(releaseEntity -> releaseEntity.getValue(FIELD_IS_DEFAULT).getValue() == Boolean.TRUE)
                .findAny()
                .orElse(null);
    }

    private long getDaysLeftInRelease(EntityModel releaseEntityModel) {
        FieldModel fieldModel = releaseEntityModel.getValue(FIELD_END_DATE);

        if (!(fieldModel instanceof DateFieldModel)) {
            return 0;
        }

        DateFieldModel releaseEndDateFieldModel = (DateFieldModel) releaseEntityModel.getValue(FIELD_END_DATE);

        return ChronoUnit.DAYS.between(getNow(), releaseEndDateFieldModel.getValue());
    }

    private static ZonedDateTime getNow() {
        return ZonedDateTime
                .of(LocalDate.now(),
                        LocalTime.of(12, 0, 0),
                        ZoneId.of("Z")
                );
    }

    private static Query.QueryBuilder createCurrentDateQueryBuilder() {
        ZonedDateTime now = getNow();

        return Query.statement(
                FIELD_START_DATE, QueryMethod.LessThanOrEqualTo, now.toString()
        ).and(
                FIELD_END_DATE, QueryMethod.GreaterThanOrEqualTo, now.toString()
        );
    }

    private EntityModel getCurrentSprintInRelease(EntityModel release) {
        if (release == null) {
            return null;
        }

        Query.QueryBuilder queryBuilder = createCurrentDateQueryBuilder();
        queryBuilder = queryBuilder.and(Entity.RELEASE.getTypeName(), QueryMethod.EqualTo,
                Query.statement(FIELD_ID, QueryMethod.EqualTo, release.getId()));

        Collection<EntityModel> sprints = octaneProvider
                .getOctane()
                .entityList(Entity.SPRINT.getApiEntityName())
                .get()
                .query(queryBuilder.build())
                .execute();

        if (sprints.size() > 0) {
            return sprints.iterator().next();
        }
        return null;
    }

    private EntityModel getReleaseToShowInTimeline() {
        Collection<EntityModel> currentReleases = getCurrentReleases();
        if (currentReleases.size() == 0) {
            return null;
        }
        EntityModel defaultRelease = findDefaultRelease(currentReleases);

        return defaultRelease == null ? currentReleases.iterator().next() : defaultRelease;
    }

    private String convertReleaseToString(EntityModel releaseEntityModel) {
        if (releaseEntityModel == null) {
            return "";
        }
        String result = "Release: ";
        result += releaseEntityModel.getValue(FIELD_NAME).getValue().toString();
        result += " (" + getDaysLeftInRelease(releaseEntityModel) + "d left)";
        return result;
    }

    private String convertSprintToString(EntityModel sprintEntityModel) {
        if (sprintEntityModel == null) {
            return "";
        }
        String result = "Sprint: ";
        result += sprintEntityModel.getValue(FIELD_NAME).getValue().toString();
        result += " (" + sprintEntityModel.getValue(FIELD_NUM_WD_LEFT).getValue().toString()  + "wd left)";
        return result;
    }

}