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
import com.hpe.adm.nga.sdk.OctaneClassFactory;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.*;
import com.hpe.adm.octane.ideplugins.services.connection.sso.SsoLoginGoogleHttpClient;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkServiceProxyFactory;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceModule extends AbstractModule {

    /**
     * Client type header to be used by all rest calls by the SDK
     * Changing the client type might restrict/permit access to certain server API
     */
    public static final ClientType CLIENT_TYPE = ClientType.HPE_REST_API_TECH_PREVIEW;

    private ConnectionSettingsProvider connectionSettingsProvider;

    protected final Supplier<Injector> injectorSupplier;

    private Octane octane;
    private OctaneHttpClient octaneHttpClient;

    private SsoLoginGoogleHttpClient.SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler;
    private SsoLoginGoogleHttpClient.SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler;

    public ServiceModule(ConnectionSettingsProvider connectionSettingsProvider) {
        this.connectionSettingsProvider = connectionSettingsProvider;
        injectorSupplier = Suppliers.memoize(() -> Guice.createInjector(this));

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

    private final Lock _mutex = new ReentrantLock(true);

    @Provides
    public OctaneProvider getOctane() {
        return () -> {
            ConnectionSettings currentConnectionSettings = connectionSettingsProvider.getConnectionSettings();
            if (octane == null) {

                //Will not auth

                _mutex.lock();

                octane = new CustomOctane(getOctaneHttpClient().getOctaneHttpClient(),
                        currentConnectionSettings.getBaseUrl(),
                        currentConnectionSettings.getSharedSpaceId().toString(),
                        currentConnectionSettings.getWorkspaceId());

                _mutex.unlock();
            }
            return octane;
        };
    }

    @Provides
    public HttpClientProvider getOctaneHttpClient() {
        return () -> {
            ConnectionSettings currentConnectionSettings = connectionSettingsProvider.getConnectionSettings();

            if (octaneHttpClient == null) {
                SsoLoginGoogleHttpClient httpClient = new SsoLoginGoogleHttpClient(currentConnectionSettings.getBaseUrl());
                httpClient.setSsoTokenPollingStartedHandler(ssoTokenPollingStartedHandler);
                httpClient.setSsoTokenPollingCompleteHandler(ssoTokenPollingCompleteHandler);

                _mutex.lock();
                httpClient.authenticate(currentConnectionSettings.getAuthentication());
                _mutex.unlock();

                ServiceModule.this.octaneHttpClient = httpClient;
            }

            return octaneHttpClient;
        };
    }

    public void setSsoTokenPollingStartedHandler(SsoLoginGoogleHttpClient.SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler) {
        this.ssoTokenPollingStartedHandler = ssoTokenPollingStartedHandler;
    }

    public void setSsoTokenPollingCompleteHandler(SsoLoginGoogleHttpClient.SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler) {
        this.ssoTokenPollingCompleteHandler = ssoTokenPollingCompleteHandler;
    }

}