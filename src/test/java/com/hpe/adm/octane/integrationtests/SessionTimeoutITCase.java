package com.hpe.adm.octane.integrationtests;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.filtering.Entity;
import org.junit.Test;

/**
 * This is just an example for now
 */
public class SessionTimeoutITCase extends IntegrationTestBase {

    @Inject
    private OctaneProvider octaneProvider;

    @Test
    public void test() throws InterruptedException {
        Octane octane = octaneProvider.getOctane();

        octane.entityList(Entity.USER_STORY.getApiEntityName()).get().execute();

        for(int i = 70 ; i > 0; i--){
            Thread.sleep(1000);
            System.out.println(i);
        }

        octane.entityList(Entity.USER_STORY.getApiEntityName()).get().execute();

    }

}
