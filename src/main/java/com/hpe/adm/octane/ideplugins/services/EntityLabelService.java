package com.hpe.adm.octane.ideplugins.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EntityLabelService {

    @Inject
    private HttpClientProvider httpClientProvider;

    @Inject
    private OctaneProvider octaneProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    public Map<String, String> getEntityLabelDetails(){
        String getUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + "/workspaces/" +
                connectionSettingsProvider.getConnectionSettings().getWorkspaceId() + "/entity_labels";

        String urlDomain = connectionSettingsProvider.getConnectionSettings().getBaseUrl();

        OctaneHttpRequest getOctaneHttpRequest = new OctaneHttpRequest.GetOctaneHttpRequest(getUrl);

        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(urlDomain);
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(),
                connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;
        try {
            response = octaneHttpClient.execute(getOctaneHttpRequest);

        } catch (Exception e) {
            System.out.println("Failed");
        }

        return new HashMap<>(getEntityMapFromJSON(response.getContent()));
    }

    private Map<String, String> getEntityMapFromJSON(String jsonString){
        Map<String, String> map = new HashMap<>();

        JSONObject shellObject = new JSONObject(jsonString);
        JSONArray entityLabelJSONObjects = shellObject.getJSONArray("data");
        for(Object entityLabelObject : entityLabelJSONObjects){
            if(entityLabelObject instanceof JSONObject) {
                if(((JSONObject) entityLabelObject).get("language").equals("lang.en")) {
                    map.put(((JSONObject) entityLabelObject).getString("entity_type"),
                            ((JSONObject) entityLabelObject).getString("name"));
                }
            }
        }

        return map;
    }


}
