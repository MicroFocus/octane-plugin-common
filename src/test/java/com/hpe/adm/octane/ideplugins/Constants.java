/*
 * Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
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
package com.hpe.adm.octane.ideplugins;

public class Constants {

    public static final String SHARED_SPACE = "/api/shared_spaces/";
    public static final String WORKSPACES = "/workspaces";
    public static final String DATA = "data";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String ROLES = "roles";
    public static final String WORKSPACE_ENITY_NAME = "workspace_users";
    public static final String RELEASES = "/releases";
    public static final String REQUIREMENTS = "requirements";
    public static final String REQUIREMENT_ROOT = "requirement_root";
    public static final String PHASE = "phase";
    public static final String LOGICAL_NAME = "logical_name";
    public static final String SUBTYPE = "subtype";
    public static final String PARENT = "parent";
    public static final String NATIVE_STATUS = "native_status";
    public static final String OWNER = "owner";
    public static final String WORK_ITEM_ROOT = "work_item_root";




    public interface Requirement {
        String ID = "phase.requirement_document.draft";
        String NAME = "Draft";
        String LOGICAL_NAME = "phase.requirement_document.draft";
        String FOLDER = "requirement_folder";

    }

    public interface Workspace {
        String NAME_VALUE = "test_ws";
        String DESCRIPTION = "Created from intellij";
        String WORKSPACE_ID = "id";
    }

    public interface Errors {
        String CONNECTION_SETTINGS_RETRIEVE_ERROR = "Cannot retrieve connection settings from either vm args or prop file, cannot run tests";
    }

    public interface NativeStatus {
        String NATIVE_STATUS_TYPE_VALUE = "list_node";
        String NATIVE_STATUS_NEW_ID = "1094";
        String NATIVE_STATUS_OLD_ID = "1091";
        String NATIVE_STATUS_RUN_ID = "list_node.run_native_status.not_completed";
    }

    public interface User {
        String FULL_NAME = "full_name";
        String LAST_NAME = "last_name";
        String USER_TYPE = "workspace_users";
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
