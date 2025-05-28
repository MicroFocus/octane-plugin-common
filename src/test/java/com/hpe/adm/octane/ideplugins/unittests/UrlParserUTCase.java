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
package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.nga.sdk.authentication.JSONAuthentication;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.UserAuthentication;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UrlParserUTCase {

    private static String octaneUrlWithPort = "https://center.almoctane.com:3213/ui/?p=1001%2F1002&TENANTID=1#/my-items/my-works";

    private static String expectedBase = "https://center.almoctane.com:3213";
    private static Long   expectedSharedSpaceId = 1001L;
    private static Long   expectedWorkspaceId = 1002L;

    @Test
    public void testUrlParser(){

        //Test parsing
        ConnectionSettings connectionSettings = null;
        try {
            JSONAuthentication jsonAuthentication = new UserAuthentication("username", "password");
            connectionSettings = UrlParser.resolveConnectionSettings(octaneUrlWithPort, jsonAuthentication);
        } catch (ServiceException e) {
            fail(e.getMessage());
        }

        assertEquals(connectionSettings.getBaseUrl(), expectedBase);
        assertEquals(connectionSettings.getSharedSpaceId(), expectedSharedSpaceId);
        assertEquals(connectionSettings.getWorkspaceId(), expectedWorkspaceId);

    }

}
