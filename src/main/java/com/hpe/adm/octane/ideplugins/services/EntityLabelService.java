package com.hpe.adm.octane.ideplugins.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityLabelService {
    private final String ENTITY_TYPE = "entity_type";
    private final String ENTITY_NAME = "name";
    private final String ENTITY_INITIALS = "initials";

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
                //we are supporting only english
                if(((JSONObject) entityLabelObject).get("language").equals("lang.en")) {
                    EntityModel em = new EntityModel();
                    em.setValue(new StringFieldModel(ENTITY_TYPE, ((JSONObject) entityLabelObject).getString(ENTITY_TYPE)));
                    em.setValue(new StringFieldModel(ENTITY_NAME, ((JSONObject) entityLabelObject).getString(ENTITY_NAME)));
                    em.setValue((new StringFieldModel(ENTITY_INITIALS, ((JSONObject) entityLabelObject).getString(ENTITY_INITIALS))));
                    entityLabelList.add(em);
                }
            }
        }
        return entityLabelList;
    }


}
