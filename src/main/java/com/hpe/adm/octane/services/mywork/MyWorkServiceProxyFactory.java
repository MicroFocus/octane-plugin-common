package com.hpe.adm.octane.services.mywork;

import com.google.inject.Inject;
import com.hpe.adm.octane.services.ServiceProxyFactory;
import com.hpe.adm.octane.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.services.util.OctaneVersion;

import java.util.function.BooleanSupplier;

public class MyWorkServiceProxyFactory {

    @Inject
    private OctaneVersionService octaneVersionService;
    @Inject
    private PreDynamoMyWorkService preDynamoMyWorkService;
    @Inject
    private PostDynamoMyWorkService postDynamoMyWorkService;

    private MyWorkService myWorkProxy;

    private void init(){
        if(myWorkProxy != null) return;

        BooleanSupplier beforeOrDynamo = () -> compareServerVersion(OctaneVersion.Operation.LOWER_EQ, OctaneVersion.DYNAMO);
        BooleanSupplier afterDynamo = () -> compareServerVersion(OctaneVersion.Operation.HIGHER, OctaneVersion.DYNAMO);

        ServiceProxyFactory<MyWorkService> myWorkProxy = new ServiceProxyFactory(MyWorkService.class);

        myWorkProxy.addService(beforeOrDynamo, preDynamoMyWorkService);
        myWorkProxy.addService(afterDynamo, postDynamoMyWorkService);

        this.myWorkProxy = myWorkProxy.getServiceProxy();
    }

    public MyWorkService getMyWorkService(){
        init();
        return myWorkProxy;
    }

    private boolean compareServerVersion(OctaneVersion.Operation operation, OctaneVersion otherVersion){
        OctaneVersion version = octaneVersionService.getOctaneVersion();
        version.discardBuildNumber();
        return OctaneVersion.compare(version, operation, otherVersion);
    }

}