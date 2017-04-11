package com.hpe.adm.octane.services.nonentity;

import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.services.util.OctaneVersion;

/**
 * Retrieves the Octane version.
 */
public class OctaneVersionService {

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private String versionString;
    private final Runnable resetVersionRunnable = () -> versionString = null;

    private static String getVersionString(ConnectionSettings connectionSettings) {
        try {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(connectionSettings.getBaseUrl() + "/admin/server/version");
            OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
            OctaneHttpResponse response = octaneHttpClient.execute(request);
            String jsonString = response.getContent();
            return new JsonParser().parse(jsonString).getAsJsonObject().get("display_version").getAsString();
        } catch (Exception e) {
            //TODO: logging
            throw new ServiceRuntimeException("Failed to retrieve Octane server version", e);
        }
    }

    /**
     * If the server version cannot be fetched it assumes it's the latest version
     *
     * @return OctaneVersion of the current connection settings
     */
    public OctaneVersion getOctaneVersion() {
        if (versionString == null) {
            connectionSettingsProvider.addChangeHandler(resetVersionRunnable);
            versionString = getVersionString(connectionSettingsProvider.getConnectionSettings());
        }
        return new OctaneVersion(versionString);
    }

    public static OctaneVersion getOctaneVersion(ConnectionSettings connectionSettings) {
        String versionString = getVersionString(connectionSettings);
        return new OctaneVersion(versionString);
    }

}
