package com.hpe.adm.octane.services.filtering;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PredefinedEntityComparator implements Comparator<Entity> {

    public static PredefinedEntityComparator instance = new PredefinedEntityComparator();

    private static final List<Entity> predefinedOrder = Arrays.asList(Entity.USER_STORY,
            Entity.QUALITY_STORY,
            Entity.DEFECT,
            Entity.TASK,
            Entity.MANUAL_TEST,
            Entity.GHERKIN_TEST,
            Entity.TEST_SUITE_RUN,
            Entity.MANUAL_TEST_RUN,
            Entity.COMMENT);

    @Override
    public int compare(Entity entityLeft, Entity entityRight) {
        int indexOfLeft = predefinedOrder.indexOf(entityLeft);
        int indexOfRight = predefinedOrder.indexOf(entityRight);

        if (indexOfLeft < 0 && indexOfRight < 0) {
            return entityLeft.name().compareTo(entityRight.name());
        }
        return Integer.compare(indexOfLeft, indexOfRight);
    }

}