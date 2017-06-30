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

package com.hpe.adm.octane.ideplugins.integrationtests.services.mywork;

import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;

/**
 * This is just for temporary development
 * TODO: add proper support for this in the test framework
 */
class HardcodedServerUtil {

    static ConnectionSettings chelsea = new ConnectionSettings();
    static {
        chelsea.setBaseUrl("http://16.60.185.192:63374");
        chelsea.setSharedSpaceId(1001L);
        chelsea.setWorkspaceId(1002L);
        chelsea.setUserName("sa@nga");
        chelsea.setPassword("Welcome1");
    }

    static ConnectionSettings dynamo = new ConnectionSettings();
    static {
        dynamo.setBaseUrl("https://mqast001pngx.saas.hpe.com");
        dynamo.setSharedSpaceId(70058L);
        dynamo.setWorkspaceId(3001L);
        dynamo.setUserName("toth_andras1991@yahoo.com");
        dynamo.setPassword("Welcome1");

    }

    static ConnectionSettings everton21 = new ConnectionSettings();
    static {
        everton21.setBaseUrl("http://myd-vm19723.hpeswlab.net:62575");
        everton21.setSharedSpaceId(1001L);
        everton21.setWorkspaceId(1002L);
        everton21.setUserName("sa@nga");
        everton21.setPassword("Welcome1");
    }

    static ConnectionSettings everton22 = new ConnectionSettings();
    static {
        everton22.setBaseUrl("http://myd-vm10632.hpeswlab.net:8081");
        everton22.setSharedSpaceId(1001L);
        everton22.setWorkspaceId(1002L);
        everton22.setUserName("sa@nga");
        everton22.setPassword("Welcome1");
    }

}