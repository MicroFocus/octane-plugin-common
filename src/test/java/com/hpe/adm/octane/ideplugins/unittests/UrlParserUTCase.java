/*
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
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
            connectionSettings = UrlParser.resolveConnectionSettings(octaneUrlWithPort, "username", "password");
        } catch (ServiceException e) {
            fail(e.getMessage());
        }

        assertEquals(connectionSettings.getBaseUrl(), expectedBase);
        assertEquals(connectionSettings.getSharedSpaceId(), expectedSharedSpaceId);
        assertEquals(connectionSettings.getWorkspaceId(), expectedWorkspaceId);

    }

}
