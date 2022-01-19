package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;

public class OctaneUtils {

    @Inject
    private OctaneVersionService versionService;

    public boolean isIronMaidenP1VersionOrHigher() {
        return OctaneVersion.compare(versionService.getOctaneVersion(), OctaneVersion.Operation.HIGHER_EQ, OctaneVersion.IRONMAIDEN_P1);
    }
}
