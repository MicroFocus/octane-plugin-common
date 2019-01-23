package com.hpe.adm.octane.ideplugins.services.connection;

import com.hpe.adm.nga.sdk.network.OctaneHttpClient;

public interface HttpClientProvider {
    OctaneHttpClient getOctaneHttpClient();
}