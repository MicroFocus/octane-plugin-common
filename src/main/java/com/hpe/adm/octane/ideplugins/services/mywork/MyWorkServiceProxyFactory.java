/*******************************************************************************
 * Copyright 2017-2026 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.services.ServiceProxyFactory;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;

import java.util.function.BooleanSupplier;

public class MyWorkServiceProxyFactory {

    @Inject
    private OctaneVersionService octaneVersionService;
    @Inject
    private DynamoMyWorkService dynamoMyWorkService; //v <= 12.53.20
    @Inject
    private EvertonP1MyWorkService evertonP1MyWorkService; //v == 12.53.21
    @Inject
    private EvertonP2MyWorkService evertonP2MyWorkService; //v >= 12.53.22 && < 16.0.208
    @Inject
    private IronMaidenP1MyWorkService ironMaidenP1MyWorkService; //v >= 16.0.208

    private MyWorkService myWorkProxy;

    private void init() {
        if (myWorkProxy != null) return;

        BooleanSupplier isBeforeOrDynamo = () -> compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO);
        BooleanSupplier isEvertonP1 = () -> compareServerVersion(OctaneVersion.Operation.EQ, OctaneVersion.EVERTON_P1);
        BooleanSupplier isEvertonP2OrHigherAndBeforeIronMaidenP1 = () -> compareServerVersion(OctaneVersion.Operation.HIGHER_EQ, OctaneVersion.EVERTON_P2) &&
                compareServerVersion(OctaneVersion.Operation.LOWER, OctaneVersion.IRONMAIDEN_P1);
        BooleanSupplier isIronMaidenP1OrHigher = () -> compareServerVersion(OctaneVersion.Operation.HIGHER_EQ, OctaneVersion.IRONMAIDEN_P1);

        ServiceProxyFactory<MyWorkService> myWorkProxy = new ServiceProxyFactory<MyWorkService>(MyWorkService.class);

        myWorkProxy.addService(isBeforeOrDynamo, dynamoMyWorkService);
        myWorkProxy.addService(isEvertonP1, evertonP1MyWorkService);
        myWorkProxy.addService(isEvertonP2OrHigherAndBeforeIronMaidenP1, evertonP2MyWorkService);
        myWorkProxy.addService(isIronMaidenP1OrHigher, ironMaidenP1MyWorkService);

        this.myWorkProxy = myWorkProxy.getServiceProxy();
    }

    public MyWorkService getMyWorkService() {
        init();
        return myWorkProxy;
    }

    private boolean compareServerVersion(OctaneVersion.Operation operation, OctaneVersion otherVersion) {
        OctaneVersion version = octaneVersionService.getOctaneVersion(true);
        version.discardBuildNumber();
        return OctaneVersion.compare(version, operation, otherVersion);
    }

}