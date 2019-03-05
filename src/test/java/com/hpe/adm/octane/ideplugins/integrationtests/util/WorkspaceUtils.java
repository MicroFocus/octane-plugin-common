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
package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.entities.OctaneCollection;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.fail;

public class WorkspaceUtils {

    @Inject
    private OctaneProvider octaneProvider;

    private static String WORKSPACE_NAME = Constants.Workspace.NAME_VALUE + " : " + LocalDateTime.now();

    /**
     * Creates a new workspace and stores the id of it for later use
     *
     * @return the workspace_id of the created workspace
     */
    public String createWorkSpace() {
        EntityModel workspace = new EntityModel();
        workspace.setValue(new StringFieldModel(Constants.NAME, WORKSPACE_NAME));
        workspace.setValue(new StringFieldModel(Constants.DESCRIPTION, Constants.Workspace.DESCRIPTION));

        OctaneCollection<EntityModel> createdWorkspace;
        try {
            createdWorkspace = octaneProvider.getOctane().entityList(Constants.WORKSPACES).create().entities(Collections.singletonList(workspace)).execute();
            return createdWorkspace.iterator().next().getValue(Constants.Workspace.WORKSPACE_ID).toString();
        } catch (Exception e) {
            fail(e.toString());
            return null;
        }
    }
}
