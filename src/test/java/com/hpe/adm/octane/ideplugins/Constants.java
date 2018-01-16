package com.hpe.adm.octane.ideplugins;

import sun.security.provider.PolicySpiFile;

public class Constants {

    public static final String SHARED_SPACE = "/api/shared_spaces/";
    public static final String WORKSPACE = "/workspaces";
    public static final String DATA = "data";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String ROLES = "roles";
    public static final String WORKSPACE_ENITY_NAME = "workspace_users";
    public static final String RELEASES = "/releases";
    public static final String TASK = "task";
    public static final String TASKS = "tasks";
    public static final String STORY = "story";
    public static final String REQUIREMENTS = "requirements";
    public static final String REQUIREMENT_ROOT = "requirement_root";
    public static final String PHASE = "phase";
    public static final String LOGICAL_NAME = "logical_name";
    public static final String SUBTYPE = "subtype";
    public static final String PARENT = "parent";
    public static final String NATIVE_STATUS = "native_status";
    public static final String TEST = "test";
    public static final String TESTS = "tests";
    public static final String TEST_SUITE = "test_suite";
    public static final String RUN_SUITE = "run_suite";
    public static final String TEST_AUTOMATED = "test_automated";
    public static final String OWNER = "owner";
    public static final String WORK_ITEMS = "work_items";
    public static final String WORK_ITEM_ROOT = "work_item_root";

    public interface ManualRun {
        public static final String RUN = "run";
        public static final String NAME = "test_manual_run";
        public static final String SUBTYPE = "run_manual";
    }


    public interface Requirement {
        public static final String ID = "phase.requirement_document.draft";
        public static final String NAME = "Draft";
        public static final String LOGICAL_NAME = "phase.requirement_document.draft";
        public static final String TYPE = "requirement";
        public static final String DOCUMENT = "requirement_document";
        public static final String FOLDER = "requirement_folder";
    }

    public interface Workspace {
        public static final String NAME_VALUE = "test_workspace1";
        public static final String DESCRIPTION = "Created from intellij";
        public static final String WORKSPACE_ID = "workspace_id";
    }

    public interface Errors {
        public static final String CONNECTION_SETTINGS_RETRIEVE_ERROR = "Cannot retrieve connection settings from either vm args or prop file, cannot run tests";
    }

    public interface NativeStatus {
        public static final String NATIVE_STATUS_TYPE_VALUE = "list_node";
        public static final String NATIVE_STATUS_NEW_ID = "1094";
        public static final String NATIVE_STATUS_OLD_ID = "1091";
    }

    public interface User {
        public static final String FULL_NAME = "full_name";
        public static final String LAST_NAME = "last_name";
        public static final String USER_TYPE = "workspace_user";
        public static final String FIRST_NAME = "first_name";
        public static final String EMAIL = "email";
        public static final String EMAIL_DOMAIN = "@hpe.com";
        public static final String PASSWORD = "password";
        public static final String PASSWORD_VALUE = "Welcome1";
        public static final String PHONE = "phone1";
        public static final String PHONE_NR = "0875432135";
        public static final String USER_ROLES = "user_roles";
    }

    public interface Release {
        public static final String RELEASES = "releases";
        public static final String TYPE = "release";
        public static final String NAME = "test_Release";
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
    }

    public interface AgileType {
        public static final String AGILE_TYPE = "agile_type";
        public static final String NEW_ID = "list_node.release_agile_type.scrum";
        public static final String OLD_ID = "1108";
        public static final String NAME = "scrum";
        public static final String TYPE = "list_node";
    }
}
