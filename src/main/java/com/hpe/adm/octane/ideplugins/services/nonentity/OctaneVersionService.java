package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the Octane version.
 */
public class OctaneVersionService {

    private static final Logger logger = LoggerFactory.getLogger(OctaneVersionService.class.getClass());

    public static final OctaneVersion fallbackVersion = OctaneVersion.EVERTON_P3;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private String versionString;
    private final Runnable resetVersionRunnable = () -> versionString = null;

    private static String getVersionString(ConnectionSettings connectionSettings) {
        try {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(getServerVersionUrl(connectionSettings));
            OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
            OctaneHttpResponse response = octaneHttpClient.execute(request);
            String jsonString = response.getContent();
            return new JsonParser().parse(jsonString).getAsJsonObject().get("display_version").getAsString();
        } catch (Exception e) {
            logger.error("Failed to retrieve Octane server version");
            throw new ServiceRuntimeException("Failed to retrieve Octane server version", e);
        }
    }

    public static String getServerVersionUrl(ConnectionSettings connectionSettings){
        return connectionSettings.getBaseUrl() + "/admin/server/version";
    }

    public OctaneVersion getOctaneVersion() {
        if (versionString == null) {
            connectionSettingsProvider.addChangeHandler(resetVersionRunnable);
            versionString = getVersionString(connectionSettingsProvider.getConnectionSettings());
        }
        return new OctaneVersion(versionString);
    }

    public OctaneVersion getOctaneVersion(boolean useFallback) {
        try {
            return getOctaneVersion();
        } catch (Exception ex){
            if(useFallback){
                versionString = fallbackVersion.getVersionString();
                return fallbackVersion;
            } else {
                throw ex;
            }
        }
    }

    public static OctaneVersion getOctaneVersion(ConnectionSettings connectionSettings) {
        String versionString = getVersionString(connectionSettings);
        return new OctaneVersion(versionString);
    }

}
