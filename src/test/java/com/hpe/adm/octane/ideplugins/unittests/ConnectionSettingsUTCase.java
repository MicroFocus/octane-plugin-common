package com.hpe.adm.octane.ideplugins.unittests;

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
import com.hpe.adm.octane.ideplugins.services.util.SdkUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.UUID;

public class ConnectionSettingsUTCase extends IntegrationTestBase {

    private final Logger logger = LogManager.getLogger(IntegrationTestBase.class.getName().toString());

    private TestService testService = new TestService();


    private boolean validateCredentials(String username, String password, String baseUrl) {

        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(baseUrl);
        try {
            return octaneHttpClient.authenticate(new SimpleUserAuthentication(username, password, ClientType.HPE_MQM_UI.name()));
        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
            return false;
        }
    }

    @Test
    public void testValidateCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        String baseUrl = connectionSettings.getBaseUrl();
        //correct credentials
        assert validateCredentials(getCurrentUser().getValue("email").getValue().toString(), "Welcome1", baseUrl);
        //correct username and incorrect password
        EntityModel newUser = createNewUser("Abe", "Defoe");
        assert !validateCredentials(getUserById((Long.parseLong(newUser.getValue("id").getValue().toString()))).getValue("email").getValue().toString(), UUID.randomUUID().toString(), baseUrl);
        //incorrect username and correct password
        assert !validateCredentials(UUID.randomUUID().toString(), "Welcome1", baseUrl);
        //incorrect username and incorrect password
        assert !validateCredentials(UUID.randomUUID().toString(), UUID.randomUUID().toString(), baseUrl);

    }


    private boolean validateWorkspaceAndSharedSpace(long workspaceId, long sharedSpaceId, ConnectionSettings connectionSettings) {

        connectionSettings.setWorkspaceId(workspaceId);
        connectionSettings.setSharedSpaceId(sharedSpaceId);

        try {
            testService.getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().execute();
            return true;
        } catch (OctaneException e) {
            String message = SdkUtil.getMessageFromOctaneException(e);
            logger.error(message);
            return false;
        }
    }

    @Test
    public void testValidateEnvironment() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        long correctWorkspaceId = connectionSettings.getWorkspaceId();
        long correctSharedSpaceId = connectionSettings.getSharedSpaceId();
        //correct workspace id and sharedspace id
        assert validateWorkspaceAndSharedSpace(correctWorkspaceId, correctSharedSpaceId, connectionSettings);
        //correct workspace id and incorrect sharedspace id
        assert !validateWorkspaceAndSharedSpace(correctWorkspaceId, 900l, connectionSettings);
        //incorrect workspace id and correct sharedspace id
        assert !validateWorkspaceAndSharedSpace(900l, correctSharedSpaceId, connectionSettings);
        //incorrect workspace id and incorrect sharedspace id
        assert !validateWorkspaceAndSharedSpace(900l, 901l, connectionSettings);
    }


}