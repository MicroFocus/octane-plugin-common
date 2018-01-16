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
        String RUN = "run";
        String NAME = "test_manual_run";
        String SUBTYPE = "run_manual";
    }


    public interface Requirement {
        String ID = "phase.requirement_document.draft";
        String NAME = "Draft";
        String LOGICAL_NAME = "phase.requirement_document.draft";
        String TYPE = "requirement";
        String DOCUMENT = "requirement_document";
        String FOLDER = "requirement_folder";
    }

    public interface Workspace {
        String NAME_VALUE = "test_workspace1";
        String DESCRIPTION = "Created from intellij";
        String WORKSPACE_ID = "workspace_id";
    }

    public interface Errors {
        String CONNECTION_SETTINGS_RETRIEVE_ERROR = "Cannot retrieve connection settings from either vm args or prop file, cannot run tests";
    }

    public interface NativeStatus {
        String NATIVE_STATUS_TYPE_VALUE = "list_node";
        String NATIVE_STATUS_NEW_ID = "1094";
        String NATIVE_STATUS_OLD_ID = "1091";
    }

    public interface User {
        String FULL_NAME = "full_name";
        String LAST_NAME = "last_name";
        String USER_TYPE = "workspace_user";
        String FIRST_NAME = "first_name";
        String EMAIL = "email";
        String EMAIL_DOMAIN = "@hpe.com";
        String PASSWORD = "password";
        String PASSWORD_VALUE = "Welcome1";
        String PHONE = "phone1";
        String PHONE_NR = "0875432135";
        String USER_ROLES = "user_roles";
    }

    public interface Release {
        String RELEASES = "releases";
        String TYPE = "release";
        String NAME = "test_Release";
        String START_DATE = "start_date";
        String END_DATE = "end_date";
    }

    public interface AgileType {
        String AGILE_TYPE = "agile_type";
        String NEW_ID = "list_node.release_agile_type.scrum";
        String OLD_ID = "1108";
        String NAME = "scrum";
        String TYPE = "list_node";
    }
}
