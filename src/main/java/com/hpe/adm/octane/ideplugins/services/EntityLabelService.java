package com.hpe.adm.octane.ideplugins.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
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

import java.util.*;

public class EntityLabelService {

    @Inject
    private HttpClientProvider httpClientProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    public Collection<EntityModel> getEntityLabelDetails(){
        String getUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + "/workspaces/" +
                connectionSettingsProvider.getConnectionSettings().getWorkspaceId() + "/entity_labels";

        OctaneHttpRequest getOctaneHttpRequest = new OctaneHttpRequest.GetOctaneHttpRequest(getUrl);

        OctaneHttpClient octaneHttpClient = httpClientProvider.geOctaneHttpClient();
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(),
                connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;
        try {
            response = octaneHttpClient.execute(getOctaneHttpRequest);

        } catch (Exception e) {
            System.out.println("Failed");
        }

        return getEntityMapFromJSON(response.getContent());
    }

    private Collection<EntityModel> getEntityMapFromJSON(String jsonString){
        List<EntityModel> entityLabelList = new ArrayList<>();

        JSONObject shellObject = new JSONObject(jsonString);
        JSONArray entityLabelJSONObjects = shellObject.getJSONArray("data");
        for(Object entityLabelObject : entityLabelJSONObjects){
            if(entityLabelObject instanceof JSONObject) {
                if(((JSONObject) entityLabelObject).get("language").equals("lang.en")) {
                    EntityModel em = new EntityModel();
                    em.setValue(new StringFieldModel("entity_type", ((JSONObject) entityLabelObject).getString("entity_type")));
                    em.setValue(new StringFieldModel("name", ((JSONObject) entityLabelObject).getString("name")));
                    em.setValue((new StringFieldModel("initials", ((JSONObject) entityLabelObject).getString("initials"))));
                    entityLabelList.add(em);
                }
            }
        }
        return entityLabelList;
    }


}
