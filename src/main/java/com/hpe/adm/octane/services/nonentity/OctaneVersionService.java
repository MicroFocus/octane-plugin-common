package com.hpe.adm.octane.services.nonentity;

import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.services.connection.HttpClientProvider;
import com.hpe.adm.octane.services.util.OctaneVersion;
import org.apache.commons.lang.StringUtils;

/**
 * Retrieves the Octane version.
 */
public class OctaneVersionService {

    @Inject
    private HttpClientProvider httpClientProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    public String versionString;

    private void loadOctaneVersionString() {

        if(versionString == null){
            connectionSettingsProvider.addChangeHandler(()-> versionString = null);

            ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
            OctaneHttpClient httpClient = httpClientProvider.geOctaneHttpClient();
            if (httpClient != null) {
                try {
                    OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(connectionSettings.getBaseUrl() + "/admin/server/version");
                    OctaneHttpResponse response = httpClient.execute(request);
                    String jsonString = response.getContent();
                    versionString = new JsonParser().parse(jsonString).getAsJsonObject().get("display_version").getAsString();
                }
                catch (Exception e) {
                }
            }
        }

    }

    /**
     * If the server version cannot be fetched it assumes it's the latest version
     * @return
     */
    public OctaneVersion getOctaneVersion() {
        loadOctaneVersionString();
        return StringUtils.isEmpty(versionString) ? OctaneVersion.EVERTON_P1 : new OctaneVersion(versionString);
    }
}
