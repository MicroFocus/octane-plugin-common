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

    @Test
    public void validateCorrectCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        EntityModel entityModel = getCurrentUser();
        try {
            assert octaneHttpClient.authenticate(new SimpleUserAuthentication(entityModel.getValue("email").getValue().toString(), "Welcome1", ClientType.HPE_MQM_UI.name()));
        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
        }
    }

    @Test
    public void validateCorrectUserIncorrectPasswordCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        String username = getCurrentUser().getValue("email").getValue().toString();
        String password = UUID.randomUUID().toString();
        try {
            assert !octaneHttpClient.authenticate(new SimpleUserAuthentication(username, password, ClientType.HPE_MQM_UI.name()));

        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
        }
    }

    @Test
    public void validateIncorrectUserCorrectPasswordCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        String username = UUID.randomUUID().toString();
        String password = "Welcome1"; //--default password
        try {
            assert !octaneHttpClient.authenticate(new SimpleUserAuthentication(username, password, ClientType.HPE_MQM_UI.name()));

        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
        }
    }

    @Test
    public void validateIncorrectUserIncorrectCredentialsCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        String dummyUser = UUID.randomUUID().toString();
        String dummyPassword = UUID.randomUUID().toString();
        try {
            assert !octaneHttpClient.authenticate(new SimpleUserAuthentication(dummyUser, dummyPassword, ClientType.HPE_MQM_UI.name()));
        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
        }
    }


    @Test
    public void validateCorrectWorkspaceCorrectSharedSpace() {
        TestService testService = new TestService();
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        try {
            testService.getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().execute();
            assert true;
        } catch (OctaneException e) {
            String message = SdkUtil.getMessageFromOctaneException(e);
            logger.error(message);
            assert false;
        }
    }

    @Test
    public void validateCorrectWorkspaceIncorrectSharedSpace() {
        TestService testService = new TestService();
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        connectionSettings.setSharedSpaceId(5000l);//--wrong shared space id, doesn't exist
        try {
            testService.getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().execute();
        } catch (OctaneException e) {
            String message = SdkUtil.getMessageFromOctaneException(e);
            logger.error(message);
            assert message.equals("The sharedspace or the workspace is incorrect.");
        }
    }

    @Test
    public void validateIncorrectWorkspaceCorrectSharedSpace() {
        TestService testService = new TestService();
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        connectionSettings.setWorkspaceId(5000l);//--wrong workspace id, doesn't exist
        try {
            testService.getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().execute();
        } catch (OctaneException e) {
            String message = SdkUtil.getMessageFromOctaneException(e);
            logger.error(message);
            assert message.equals("The sharedspace or the workspace is incorrect.");
        }
    }

    @Test
    public void validateIncorrectWorkspaceIncorrectSharedSpace() {
        TestService testService = new TestService();
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        connectionSettings.setWorkspaceId(5000l);//--wrong workspace id, doesn't exist
        connectionSettings.setSharedSpaceId(5001l);//--wrong sharedspace id, doesn't exist
        try {
            testService.getOctane(connectionSettings).entityList(Entity.WORK_ITEM_ROOT.getApiEntityName()).get().execute();
        } catch (OctaneException e) {
            String message = SdkUtil.getMessageFromOctaneException(e);
            logger.error(message);
            assert message.equals("The sharedspace or the workspace is incorrect.");
        }
    }
}


