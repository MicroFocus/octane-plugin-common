package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import com.hpe.adm.octane.ideplugins.services.util.SdkUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.UUID;

public class ConnectionSettingsUTCase extends IntegrationTestBase {

    private final Logger logger = LogManager.getLogger(IntegrationTestBase.class.getName().toString());

    @Test
    public void validateGoodCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        EntityModel entityModel = getCurrentUser();
        try {
            assert octaneHttpClient.authenticate(new SimpleUserAuthentication(entityModel.getValue("email").getValue().toString(),"Welcome1",ClientType.HPE_MQM_UI.name()));
        } catch (OctaneException e) {
                String errorMessage = SdkUtil.getMessageFromOctaneException(e);
                logger.error(errorMessage);
        }
    }

    @Test
    public void validateGoodUserWrongPasswordCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        String username = getCurrentUser().getValue("email").getValue().toString();
        String password = UUID.randomUUID().toString();
        try {
            assert !octaneHttpClient.authenticate(new SimpleUserAuthentication(username,password,ClientType.HPE_MQM_UI.name()));

        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
        }
    }

    @Test
    public void validateWrongUserGoodPasswordCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        String username = UUID.randomUUID().toString();
        String password = "Welcome1"; //--default password
        try {
            assert !octaneHttpClient.authenticate(new SimpleUserAuthentication(username,password,ClientType.HPE_MQM_UI.name()));

        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
        }
    }

    @Test
    public void validateWrongUserWrongCredentialsCredentials() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient octaneHttpClient = new GoogleHttpClient(connectionSettings.getBaseUrl());
        String dummyUser = UUID.randomUUID().toString();
        String dummyPassword = UUID.randomUUID().toString();
        try {
            assert !octaneHttpClient.authenticate(new SimpleUserAuthentication(dummyUser,dummyPassword,ClientType.HPE_MQM_UI.name()));
        } catch (OctaneException e) {
            String errorMessage = SdkUtil.getMessageFromOctaneException(e);
            logger.error(errorMessage);
        }
    }
}
