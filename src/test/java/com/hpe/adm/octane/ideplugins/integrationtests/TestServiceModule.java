package com.hpe.adm.octane.ideplugins.integrationtests;

import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;

public class TestServiceModule {

    private static ServiceModule ServiceModule;

    public static void setServiceModule(ServiceModule serviceModule) {
        ServiceModule = serviceModule;
    }

    public static ServiceModule getServiceModule() {
        return ServiceModule;
    }
}
