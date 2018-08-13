/*
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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

package com.hpe.adm.octane.ideplugins.services.di;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.extension.OctaneExtensionUtil;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkServiceProxyFactory;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;

public class ServiceModule extends AbstractModule {

    /**
     * Client type header to be used by all rest calls by the SDK
     * Changing the client type might restrict/permit access to certain server API
     */
    public static final ClientType CLIENT_TYPE = ClientType.HPE_REST_API_TECH_PREVIEW;

    private ConnectionSettingsProvider connectionSettingsProvider;

    protected final Supplier<Injector> injectorSupplier;

    private Octane octane;
    private ConnectionSettings octaneProviderPreviousConnectionSettings = new ConnectionSettings();

    private OctaneHttpClient octaneHttpClient;
    private ConnectionSettings httpClientPreviousConnectionSettings = new ConnectionSettings();

    public ServiceModule(ConnectionSettingsProvider connectionSettingsProvider) {
        this.connectionSettingsProvider = connectionSettingsProvider;
        injectorSupplier = Suppliers.memoize(() -> Guice.createInjector(this));

        OctaneExtensionUtil.enable();

        //Reset in case of connection settings change
        connectionSettingsProvider.addChangeHandler(()->{
            octane = null;
            octaneHttpClient = null;
        });
    }

    @Override
    protected void configure() {
        bind(ConnectionSettingsProvider.class).toProvider(() -> connectionSettingsProvider);
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
                octane = new Octane.Builder(currentConnectionSettings.getAuthentication())
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
                GoogleHttpClient httpClient = new GoogleHttpClient(currentConnectionSettings.getBaseUrl());
                httpClientPreviousConnectionSettings = currentConnectionSettings;

                httpClient.authenticate(currentConnectionSettings.getAuthentication());
                
                //Do not set the field until authenticate is done, otherwise you get multithreading issues
                ServiceModule.this.octaneHttpClient = httpClient;
            }

            return octaneHttpClient;
        };
    }

}