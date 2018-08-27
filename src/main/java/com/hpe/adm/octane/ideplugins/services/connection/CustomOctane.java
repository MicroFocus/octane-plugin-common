package com.hpe.adm.octane.ideplugins.services.connection;

import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;

public class CustomOctane extends Octane {

    public CustomOctane(OctaneHttpClient octaneHttpClient, String domain, String sharedSpaceId, long workId) {
        super(octaneHttpClient, domain, sharedSpaceId, workId);
    }

}