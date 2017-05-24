package com.hpe.adm.octane.integrationtests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.octane.integrationtests.util.ConfigurationUtil;
import com.hpe.adm.octane.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.di.ServiceModule;
import org.junit.Before;

/**
 * Enables the use of the {@link Inject} annotation
 */
public abstract class IntegrationTestBase {

    private Injector injector;

    protected EntityGenerator entityGenerator;

    @Before
    public void setup () {
        injector = Guice.createInjector(new ServiceModule(readConnectionSettingsFromFile()));
        injector.injectMembers(this);
        entityGenerator = new EntityGenerator(injector.getInstance(OctaneProvider.class));
    }

    private ConnectionSettingsProvider readConnectionSettingsFromFile(){
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setBaseUrl(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.URL));
        connectionSettings.setSharedSpaceId(ConfigurationUtil.getLong(ConfigurationUtil.PropertyKeys.SHAREDSPACE));
        connectionSettings.setWorkspaceId(ConfigurationUtil.getLong(ConfigurationUtil.PropertyKeys.WORKSPACE));
        connectionSettings.setUserName(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.USERNAME));
        connectionSettings.setPassword(ConfigurationUtil.getString(ConfigurationUtil.PropertyKeys.PASSWORD));
        return new BasicConnectionSettingProvider(connectionSettings);
    }

}
