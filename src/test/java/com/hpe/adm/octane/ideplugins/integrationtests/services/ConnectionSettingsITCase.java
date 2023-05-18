/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.integrationtests.services;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.authentication.JSONAuthentication;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.TestServiceModule;
import com.hpe.adm.octane.ideplugins.integrationtests.util.UserUtils;
import com.hpe.adm.octane.ideplugins.services.TestService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.IdePluginsOctaneHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.UserAuthentication;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class ConnectionSettingsITCase {

    @Inject
    private UserUtils userUtils;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private ServiceModule serviceModule;
    private TestService testService;

    private ConnectionSettings connectionSettings;
    private long correctWorkspaceId;
    private long correctSharedSpaceId;
    private String baseUrl;
    private String newUserEmail;

    @Before
    public void setup() {
        serviceModule = TestServiceModule.getServiceModule();
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);

        connectionSettings = connectionSettingsProvider.getConnectionSettings();
        correctWorkspaceId = connectionSettings.getWorkspaceId();
        correctSharedSpaceId = connectionSettings.getSharedSpaceId();
        baseUrl = connectionSettings.getBaseUrl();
        testService = new TestService();
    }

    private boolean validateCredentials(JSONAuthentication authentication) {
        IdePluginsOctaneHttpClient octaneHttpClient = new IdePluginsOctaneHttpClient(baseUrl, ClientType.OCTANE_IDE_PLUGIN);
        try {
            octaneHttpClient.setLastUsedAuthentication(authentication);
            return octaneHttpClient.authenticate();
        } catch (Exception e) {
            return false;
        }
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
    public void testCorrectCredentials() {
        // try the credentials provided in the property file/ vm args
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        assert validateCredentials(connectionSettings.getAuthentication());
    }

    @Test
    public void testCorrectUsernameAndIncorrectPassword() {
        try {
            EntityModel newUser = userUtils.createNewUser("User", UUID.randomUUID().toString());
            newUserEmail = userUtils.getUserById((Long.parseLong(newUser.getValue("id").getValue().toString())))
                    .getValue("email").getValue().toString();
        } catch (Exception ex) {
            Assert.fail("Failed to create new user: " + ex.getMessage());
        }
        assert !validateCredentials(new UserAuthentication(newUserEmail, UUID.randomUUID().toString()));
    }

    @Test
    public void testIncorrectUsernameAndCorrectPassword() {
        assert !validateCredentials(new UserAuthentication(UUID.randomUUID().toString(), "Welcome1"));
    }

    @Test
    public void testIncorrectUsernameAndIncorrectPassword() {
        assert !validateCredentials(new UserAuthentication(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    @Test
    public void testCorrectWorkspaceAndSharedSpace() {
        assert validateWorkspaceAndSharedSpace(correctWorkspaceId, correctSharedSpaceId, connectionSettings);
    }

    @Test
    public void testCorrectWorkspaceAndIncorrectSharedSpace() {
        assert !validateWorkspaceAndSharedSpace(correctWorkspaceId, 900L, connectionSettings);
    }

    @Test
    public void testIncorrectWorkspaceAndCorrectSharedSpace() {
        assert !validateWorkspaceAndSharedSpace(900L, correctSharedSpaceId, connectionSettings);
    }

    @Test
    public void testIncorrectWorkspaceAndIncorrectSharedSpace() {
        assert !validateWorkspaceAndSharedSpace(901L, 900L, connectionSettings);
    }

}