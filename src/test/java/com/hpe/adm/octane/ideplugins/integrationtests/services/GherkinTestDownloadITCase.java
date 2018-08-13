package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.json.JSONObject;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.fail;

public class GherkinTestDownloadITCase extends IntegrationTestBase {

    private EntityModel createGherkinTestWithScript(UUID uuid) {
        EntityModel gherkinTest = createEntity(Entity.GHERKIN_TEST);

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();

        String putUrl = connectionSettings.getBaseUrl() + "/api/shared_spaces/" +
                connectionSettings.getSharedSpaceId() + "/workspaces/" +
                connectionSettings.getWorkspaceId() + "/tests/" +
                gherkinTest.getValue("id").getValue() + "/script";

        JSONObject script = new JSONObject();
        script.put("comment","");
        script.put("revision_type","Minor");
        script.put("script", uuid);

        OctaneHttpRequest updateScriptRequest = new OctaneHttpRequest.PutOctaneHttpRequest(putUrl, OctaneHttpRequest.JSON_CONTENT_TYPE, script.toString());
        try {
            httpClient.execute(updateScriptRequest);
        } catch (Exception e) {
            fail(e.toString());
        }

        return gherkinTest;
    }

    private String getGherkinScript(EntityModel gherkinTest){

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        String getUrl = connectionSettings.getBaseUrl() + "/api/shared_spaces/" +
                connectionSettings.getSharedSpaceId() + "/workspaces/" +
                connectionSettings.getWorkspaceId() + "/tests/" +
                gherkinTest.getValue("id").getValue() + "/script";

        OctaneHttpRequest updateScriptRequest = new OctaneHttpRequest.GetOctaneHttpRequest(getUrl);

        OctaneHttpResponse response = null;
        try {
            response = httpClient.execute(updateScriptRequest);
        } catch (Exception e) {
            fail(e.toString());
        }

        JSONObject responseJson = new JSONObject(response.getContent());
        return responseJson.get("script").toString();
    }

    @Test
    public void testGherkinTestScriptDownload(){
        UUID uuid =UUID.randomUUID();
        EntityModel gherkinTest = createGherkinTestWithScript(uuid);
        String script = getGherkinScript(gherkinTest);
        assert script.contains(uuid.toString());
    }
}
