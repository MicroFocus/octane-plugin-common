/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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

    private static String octaneUrlWithPort = "http://myd-vm10632.hpeswlab.net:8081/?p=1001/1002";
    private static String hash = "#/release-quality/hierarchy/epic/lines?contentFilter=%7B%7D&navFilter=%7B%7D&filter=%7B%22lExpression%22:%7B%22operator%22:%22PROPERTY%22,%22value%22:%22release%22%7D,%22operator%22:%22IN%22,%22rExpression%22:%5B%7B%22operator%22:%22LITERAL%22,%22value%22:%7B%22$uid%22:%221002-release%22,%22$fetchConfiguration%22:null,%22$entityType%22:%22release%22,%22id%22:%221002%22,%22$inFilter%22:true,%22name%22:%22368.2%22%7D%7D%5D%7D";
    private static String octaneUrlWithPortAndHash = octaneUrlWithPort + hash;

    private static String expectedBase = "http://myd-vm10632.hpeswlab.net:8081";
    private static Long   expectedSharedSpaceId = 1001L;
    private static Long   expectedWorkspaceId = 1002L;

    private static String username = "username";
    private static String password = "password";

    @Test
    public void testUrlParser(){

        //Test parsing
        ConnectionSettings connectionSettings = null;
        try {
            connectionSettings = UrlParser.resolveConnectionSettings(octaneUrlWithPortAndHash, "username", "password");
        } catch (ServiceException e) {
            fail(e.getMessage());
        }

        assertEquals(connectionSettings.getBaseUrl(), expectedBase);
        assertEquals(connectionSettings.getSharedSpaceId(), expectedSharedSpaceId);
        assertEquals(connectionSettings.getWorkspaceId(), expectedWorkspaceId);
        assertEquals(connectionSettings.getUserName(), username);
        assertEquals(connectionSettings.getPassword(), password);

        //Test removing the hash
        String urlWithoutHash = UrlParser.removeHash(octaneUrlWithPortAndHash);
        assertEquals(octaneUrlWithPort, urlWithoutHash);

        //Test rebuilding the url from the base, reuse the parsed connection settings
        String rebuiltUrl = UrlParser.createUrlFromConnectionSettings(connectionSettings);
        assertEquals(octaneUrlWithPort, rebuiltUrl);


    }

}
