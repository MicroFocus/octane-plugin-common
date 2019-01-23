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
    private EvertonP2MyWorkService evertonP2MyWorkService; //v >= 12.53.22

    private MyWorkService myWorkProxy;

    private void init(){
        if(myWorkProxy != null) return;

        BooleanSupplier isBeforeOrDynamo = () -> compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO);
        BooleanSupplier isEvertonP1 = () -> compareServerVersion(OctaneVersion.Operation.EQ, OctaneVersion.EVERTON_P1);
        BooleanSupplier isEvertonP2OrHigher = () -> compareServerVersion(OctaneVersion.Operation.HIGHER_EQ, OctaneVersion.EVERTON_P2);

        ServiceProxyFactory<MyWorkService> myWorkProxy = new ServiceProxyFactory<MyWorkService>(MyWorkService.class);

        myWorkProxy.addService(isBeforeOrDynamo, dynamoMyWorkService);
        myWorkProxy.addService(isEvertonP1, evertonP1MyWorkService);
        myWorkProxy.addService(isEvertonP2OrHigher, evertonP2MyWorkService);

        this.myWorkProxy = myWorkProxy.getServiceProxy();
    }

    public MyWorkService getMyWorkService(){
        init();
        return myWorkProxy;
    }

    private boolean compareServerVersion(OctaneVersion.Operation operation, OctaneVersion otherVersion){
        OctaneVersion version = octaneVersionService.getOctaneVersion(true);
        version.discardBuildNumber();
        return OctaneVersion.compare(version, operation, otherVersion);
    }

}