package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.TestService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class ConnectionSettingsITCase extends IntegrationTestBase {

    private TestService testService = new TestService();

    private ConnectionSettings connectionSettings;
    private long correctWorkspaceId;
    private long correctSharedSpaceId;
    private String baseUrl;

    @Before
    public void setup() {
        connectionSettings = connectionSettingsProvider.getConnectionSettings();
        correctWorkspaceId = connectionSettings.getWorkspaceId();
        correctSharedSpaceId = connectionSettings.getSharedSpaceId();
        baseUrl = connectionSettings.getBaseUrl();
    }

    private boolean validateCredentials(String username, String password, String baseUrl) {

        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(baseUrl);
        try {
            return octaneHttpClient.authenticate(new SimpleUserAuthentication(username, password, ClientType.HPE_REST_API_TECH_PREVIEW.name()));
        } catch (OctaneException e) {
            return false;
        }
    }

    @Test
    public void testCorrectCredentials() {
        assert validateCredentials(getCurrentUser().getValue("email").getValue().toString(), "Welcome1", baseUrl);
    }

    @Test
    public void testCorrectUsernameAndIncorrectPassword() {
        EntityModel newUser = createNewUser("Anda", UUID.randomUUID().toString());
        assert !validateCredentials(getUserById((Long.parseLong(newUser.getValue("id").getValue().toString()))).getValue("email").getValue().toString(), UUID.randomUUID().toString(), baseUrl);
    }

    @Test
    public void testIncorrectUsernameAndCorrectPassword() {
        assert !validateCredentials(UUID.randomUUID().toString(), "Welcome1", baseUrl);
    }

    @Test
    public void testIncorrectUsernameAndIncorrectPassword() {
        assert !validateCredentials(UUID.randomUUID().toString(), UUID.randomUUID().toString(), baseUrl);
    }

    private boolean validateWorkspaceAndSharedSpace(long workspaceId, long sharedSpaceId, ConnectionSettings connectionSettings) {
        connectionSettings.setWorkspaceId(workspaceId);
        connectionSettings.setSharedSpaceId(sharedSpaceId);
        try {
            testService.getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().execute();
            return true;
        } catch (OctaneException e) {
            return false;
        }
    }

    @Test
    public void testCorrectWorkspaceAndSharedSpace() {
        assert validateWorkspaceAndSharedSpace(correctWorkspaceId, correctSharedSpaceId, connectionSettings);
    }

    @Test
    public void testCorrectWorkspaceAndIncorrectSharedSpace() {
        assert !validateWorkspaceAndSharedSpace(correctWorkspaceId, 900l, connectionSettings);
    }

    @Test
    public void testIncorrectWorkspaceAndCorrectSharedSpace() {
        assert !validateWorkspaceAndSharedSpace(900l, correctSharedSpaceId, connectionSettings);
    }

    @Test
    public void testIncorrectWorkspaceAndIncorrectSharedSpace() {
        assert !validateWorkspaceAndSharedSpace(901l, 900l, connectionSettings);
    }

}