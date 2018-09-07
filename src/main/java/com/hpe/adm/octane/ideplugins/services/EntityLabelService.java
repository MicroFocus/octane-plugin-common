package com.hpe.adm.octane.ideplugins.services;

import com.google.api.client.util.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EntityLabelService {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private static final String ENTITY_TYPE = "entity_type";
    private static final String ENTITY_NAME = "name";
    private static final String ENTITY_INITIALS = "initials";
    private static final String ENTITY_NAME_PLURAL_CAPITALIZED = "plural_capitalized";

    private static final long CURRENT_ENTITY_LABELS_JSON_VERSION = 1;

    private static final String DEFAULT_ENTITY_LABELS_FILE_NAME = "defaultEntityLabels.json";

    private String[] usefulEntityLabelsFromServer = new String[]{"defect", "story", "quality_story", "feature", "epic", "requirement_root"};

    private Map<String, EntityModel> defaultEntityLabels;

    @Inject
    private HttpClientProvider httpClientProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    public Map<String, EntityModel> getEntityLabelDetails() {
        String getUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl() + "/api/shared_spaces/" +
                connectionSettingsProvider.getConnectionSettings().getSharedSpaceId() + "/workspaces/" +
                connectionSettingsProvider.getConnectionSettings().getWorkspaceId() + "/entity_labels";

        OctaneHttpRequest getOctaneHttpRequest = new OctaneHttpRequest.GetOctaneHttpRequest(getUrl);

        OctaneHttpClient octaneHttpClient = httpClientProvider.geOctaneHttpClient();
        octaneHttpClient.authenticate(new SimpleUserAuthentication(connectionSettingsProvider.getConnectionSettings().getUserName(),
                connectionSettingsProvider.getConnectionSettings().getPassword(), ClientType.HPE_MQM_UI.name()));
        OctaneHttpResponse response = null;
        Map<String, EntityModel> entityLabelMetadatas = getDefaultEntityLabels();
        try {
            response = octaneHttpClient.execute(getOctaneHttpRequest);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        Map<String, EntityModel> entityMetadataFromServer = getEntityMetadataFromJSON(response.getContent());
        Arrays.stream(usefulEntityLabelsFromServer).forEach(s -> {
            EntityModel em = entityMetadataFromServer.get(s);
            // hardcoded translation because of mismatch between Entity.Requirements and entity type given by the response
            if (s.equals("requirement_root")) {
                s = "requirement";
            }
            if (em != null) {
                entityLabelMetadatas.remove(s);
                entityLabelMetadatas.put(s, em);
            }
        });

        return entityLabelMetadatas;
    }

    private Map<String, EntityModel> getDefaultEntityLabels() {
        if (defaultEntityLabels == null) {
            try {
                ClasspathResourceLoader cprl = new ClasspathResourceLoader();
                InputStream input = cprl.getResourceStream(DEFAULT_ENTITY_LABELS_FILE_NAME);
                String jsonString = CharStreams.toString(new InputStreamReader(input, Charsets.UTF_8));
                return entityLabelsFromJson(jsonString);
            } catch (IOException e) {
                throw new ServiceRuntimeException("Failed to parse " + DEFAULT_ENTITY_LABELS_FILE_NAME + " file ", e);
            }
        } else {
            return defaultEntityLabels;
        }
    }

    /**
     * Util method for converting an entity labels json to a java object Reads
     * based on version tag in json object, current is
     * CURRENT_ENTITY_LABELS_JSON_VERSION
     *
     * @param jsonString json containing label metadata for entities
     * @return Collection containing {@link EntityModel}s
     */
    public Map<String, EntityModel> entityLabelsFromJson(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        long jsonVersion = jsonObject.getLong("version");

        if (CURRENT_ENTITY_LABELS_JSON_VERSION == jsonVersion) {
            return getEntityMetadataFromJSON(jsonString);
        } else {
            throw new ServiceRuntimeException("Fields json version usupported, cannot parse");
        }
    }

    private Map<String, EntityModel> getEntityMetadataFromJSON(String jsonString) {
        Map<String, EntityModel> entityLabelMetadataMap = new HashMap<>();

        JSONObject shellObject = new JSONObject(jsonString);
        JSONArray entityLabelJSONObjects = shellObject.getJSONArray("data");
        for (Object entityLabelObject : entityLabelJSONObjects) {
            if (entityLabelObject instanceof JSONObject) {
                //we are supporting only english
                if (((JSONObject) entityLabelObject).get("language").equals("lang.en")) {
                    EntityModel em = new EntityModel();
                    em.setValue(new StringFieldModel(ENTITY_TYPE, ((JSONObject) entityLabelObject).getString(ENTITY_TYPE)));
                    em.setValue(new StringFieldModel(ENTITY_NAME, ((JSONObject) entityLabelObject).getString(ENTITY_NAME)));
                    em.setValue((new StringFieldModel(ENTITY_INITIALS, ((JSONObject) entityLabelObject).getString(ENTITY_INITIALS))));
                    em.setValue((new StringFieldModel(ENTITY_NAME_PLURAL_CAPITALIZED, ((JSONObject) entityLabelObject).getString(ENTITY_NAME_PLURAL_CAPITALIZED))));
                    entityLabelMetadataMap.put(((JSONObject) entityLabelObject).getString(ENTITY_TYPE), em);
                }
            }
        }
        return entityLabelMetadataMap;
    }


}
