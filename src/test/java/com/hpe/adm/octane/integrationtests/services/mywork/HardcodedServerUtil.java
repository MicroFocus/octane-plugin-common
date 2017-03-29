package com.hpe.adm.octane.integrationtests.services.mywork;

import com.hpe.adm.octane.services.connection.ConnectionSettings;

/**
 * This is just for temporary development
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

    static ConnectionSettings everton = new ConnectionSettings();
    static {
        everton.setBaseUrl("http://myd-vm19852.hpeswlab.net:8080");
        everton.setSharedSpaceId(1001L);
        everton.setWorkspaceId(1002L);
        everton.setUserName("sa@nga");
        everton.setPassword("Welcome1");
    }

}
