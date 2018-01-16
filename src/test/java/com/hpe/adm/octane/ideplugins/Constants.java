package com.hpe.adm.octane.ideplugins;

public class Constants {

    public final String SHARED_SPACE =  "/api/shared_spaces/";
    public final String WORKSPACE = "/workspaces";
    public final String DATA = "data";

    public interface Workspace{
        public final String NAME = "name";
        public final String NAME_VALUE = "test_workspace1";
    }

    public interface Errors{
        public final String CONNECTION_SETTINGS_RETRIEVE_ERROR = "Cannot retrieve connection settings from either vm args or prop file, cannot run tests";

    }

    public interface NativeStatus{
        public final String NATIVE_STATUS_TYPE = "type";
        public final String NATIVE_STATUS_TYPE_VALUE = "list_node";
        public final String NATIVE_STATUS_NEW_ID = "1094";
        public final String NATIVE_STATUS_OLD_ID = "1091";
    }

}
