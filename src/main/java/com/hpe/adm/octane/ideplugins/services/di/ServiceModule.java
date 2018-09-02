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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.OctaneProvider;
import com.hpe.adm.octane.ideplugins.services.connection.sso.SsoLoginGoogleHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.sso.SsoLoginGoogleHttpClient.SsoTokenPollingCompleteHandler;
import com.hpe.adm.octane.ideplugins.services.connection.sso.SsoLoginGoogleHttpClient.SsoTokenPollingInProgressHandler;
import com.hpe.adm.octane.ideplugins.services.connection.sso.SsoLoginGoogleHttpClient.SsoTokenPollingStartedHandler;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkServiceProxyFactory;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;

public class ServiceModule extends AbstractModule {

    /**
     * Client type header to be used by all rest calls by the SDK Changing the
     * client type might restrict/permit access to certain server API
     */
    public static final ClientType CLIENT_TYPE = ClientType.HPE_REST_API_TECH_PREVIEW;

    private ConnectionSettingsProvider connectionSettingsProvider;

    protected final Supplier<Injector> injectorSupplier;

    private Octane octane;
    private OctaneHttpClient octaneHttpClient;

    private SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler;
    private SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler;
    private SsoTokenPollingInProgressHandler ssoTokenPollingInProgressHandler;

    public ServiceModule(ConnectionSettingsProvider connectionSettingsProvider) {
        this(connectionSettingsProvider, null, null, null);
    }

    public ServiceModule(ConnectionSettingsProvider connectionSettingsProvider, SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler,
            SsoTokenPollingInProgressHandler ssoTokenPollingInProgressHandler, SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler) {
        this.connectionSettingsProvider = connectionSettingsProvider;
        this.ssoTokenPollingStartedHandler = ssoTokenPollingStartedHandler;
        this.ssoTokenPollingCompleteHandler = ssoTokenPollingCompleteHandler;
        this.ssoTokenPollingInProgressHandler = ssoTokenPollingInProgressHandler;
        injectorSupplier = Suppliers.memoize(() -> Guice.createInjector(this));

        // Reset in case of connection settings change
        connectionSettingsProvider.addChangeHandler(() -> {
            octaneHttpClient = null;
            octane = null;
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
        MyWorkServiceProxyFactory backwardsCompatibleMyWorkServiceProvider = new MyWorkServiceProxyFactory();
        injectorSupplier.get().injectMembers(backwardsCompatibleMyWorkServiceProvider);

        return backwardsCompatibleMyWorkServiceProvider.getMyWorkService();
    }

    private final Lock _mutex = new ReentrantLock(true);

    @Provides
    public OctaneProvider getOctane() {
        return () -> {
            ConnectionSettings currentConnectionSettings = connectionSettingsProvider.getConnectionSettings();
            if (octane == null) {

                _mutex.lock();

                // Will not auth
                octane = new Octane.Builder(currentConnectionSettings.getAuthentication(), getOctaneHttpClient().getOctaneHttpClient())
                        .Server(currentConnectionSettings.getBaseUrl())
                        .workSpace(currentConnectionSettings.getWorkspaceId())
                        .sharedSpace(currentConnectionSettings.getSharedSpaceId())
                        .build();

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
                httpClient.setSsoTokenPollingInProgressHandler(ssoTokenPollingInProgressHandler);
                httpClient.setSsoTokenPollingCompleteHandler(ssoTokenPollingCompleteHandler);

                _mutex.lock();
                boolean authResult = httpClient.authenticate(currentConnectionSettings.getAuthentication());
                _mutex.unlock();

                if (!authResult) {
                    throw new ServiceRuntimeException("Failed to authenticate to Octane");
                }

                ServiceModule.this.octaneHttpClient = httpClient;
            }

            return octaneHttpClient;
        };
    }

}