package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.services.TestService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DependencyInjectionITCase extends IntegrationTestBase {

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Inject
    private TestService testService;

    @Test
    public void testInjection(){
        //Test DI
        assertNotNull(connectionSettingsProvider);
        assertNotNull(testService);
    }

}
