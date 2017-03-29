package com.hpe.adm.octane.services.di;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.services.connection.HttpClientProvider;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.mywork.MyWorkServiceProxyFactory;
import com.hpe.adm.octane.services.mywork.MyWorkService;
import com.hpe.adm.octane.services.util.ClientType;

public class ServiceModule extends AbstractModule {

    private ConnectionSettingsProvider connectionSettingsProvider;

    protected final Supplier<Injector> injectorSupplier;

    private Octane                     octane;
    private ConnectionSettings         octaneProviderPreviousConnectionSettings = new ConnectionSettings();

    private OctaneHttpClient           octaneHttpClient;
    private ConnectionSettings         httpClientPreviousConnectionSettings     = new ConnectionSettings();

    public ServiceModule(ConnectionSettingsProvider connectionSettingsProvider) {
        this.connectionSettingsProvider = connectionSettingsProvider;
        injectorSupplier = Suppliers.memoize(() -> Guice.createInjector(this));
    }

    @Override
    protected void configure() {
        bind(ConnectionSettingsProvider.class).toProvider(() -> connectionSettingsProvider);

        //bind(OctaneVersionService.class).asEagerSingleton();
        // Rest of services are trivial bindings
    }

    public <T> T getInstance(Class<T> type) {
        return injectorSupplier.get().getInstance(type);
    }

    @Provides
    public MyWorkService getMyWorkService() {
        MyWorkServiceProxyFactory backwardsCompatibleMyWorkServiceProvider
                = new MyWorkServiceProxyFactory();
        injectorSupplier.get().injectMembers(backwardsCompatibleMyWorkServiceProvider);

        return backwardsCompatibleMyWorkServiceProvider.getMyWorkService();
    }

    @Provides
    public OctaneProvider getOctane() {
        return () -> {
            ConnectionSettings currentConnectionSettings = connectionSettingsProvider.getConnectionSettings();
            if (!currentConnectionSettings.equals(octaneProviderPreviousConnectionSettings) || octane == null) {
                octane = new Octane.Builder(new SimpleUserAuthentication(currentConnectionSettings.getUserName(),
                        currentConnectionSettings.getPassword(), ClientType.HPE_MQM_UI.name()))
                                .Server(currentConnectionSettings.getBaseUrl())
                                .sharedSpace(currentConnectionSettings.getSharedSpaceId())
                                .workSpace(currentConnectionSettings.getWorkspaceId())
                                .build();

                octaneProviderPreviousConnectionSettings = currentConnectionSettings;
            }
            return octane;
        };
    }

    @Provides
    public HttpClientProvider geOctaneHttpClient() {
        return () -> {
            ConnectionSettings currentConnectionSettings = connectionSettingsProvider.getConnectionSettings();
            if (!currentConnectionSettings.equals(httpClientPreviousConnectionSettings) || null == octaneHttpClient) {
                octaneHttpClient = new GoogleHttpClient(currentConnectionSettings.getBaseUrl());
                httpClientPreviousConnectionSettings = currentConnectionSettings;
            }
            SimpleUserAuthentication userAuthentication = new SimpleUserAuthentication(currentConnectionSettings.getUserName(),
                    currentConnectionSettings.getPassword(), ClientType.HPE_MQM_UI.name());
            octaneHttpClient.authenticate(userAuthentication);

            return octaneHttpClient;
        };
    }

}