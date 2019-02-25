package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.hpe.adm.nga.sdk.entities.OctaneCollection;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.Constants;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.fail;

public class WorkspaceUtils {

    private static String WORKSPACE_NAME = Constants.Workspace.NAME_VALUE + " : " + LocalDateTime.now();

    /**
     * Creates a new workspace and stores the id of it for later use
     *
     * @return the workspace_id of the created workspace
     */
    public static String createWorkSpace(OctaneProvider octaneProvider) {
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
