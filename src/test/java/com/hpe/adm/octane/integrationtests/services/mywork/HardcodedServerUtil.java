package com.hpe.adm.octane.integrationtests.services.mywork;

import com.hpe.adm.octane.services.connection.ConnectionSettings;

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