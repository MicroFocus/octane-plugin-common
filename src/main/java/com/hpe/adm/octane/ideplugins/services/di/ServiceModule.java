/*
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.octane.ideplugins.services.connection.*;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.TokenPollingCompleteHandler;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.TokenPollingInProgressHandler;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.TokenPollingStartedHandler;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkServiceProxyFactory;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.ClientType;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(ServiceModule.class.getName());

    private ConnectionSettingsProvider connectionSettingsProvider;

    protected final Supplier<Injector> injectorSupplier;

    private Octane octane;
    private OctaneHttpClient octaneHttpClient;

    private TokenPollingStartedHandler tokenPollingStartedHandler;
    private TokenPollingCompleteHandler tokenPollingCompleteHandler;
    private TokenPollingInProgressHandler tokenPollingInProgressHandler;

    public ServiceModule(ConnectionSettingsProvider connectionSettingsProvider) {
        this(connectionSettingsProvider, null, null, null);
    }

    public ServiceModule(ConnectionSettingsProvider connectionSettingsProvider, 
    		TokenPollingStartedHandler tokenPollingStartedHandler,
            TokenPollingInProgressHandler tokenPollingInProgressHandler, 
            TokenPollingCompleteHandler tokenPollingCompleteHandler) {
    	
        this.connectionSettingsProvider = connectionSettingsProvider;
        this.tokenPollingStartedHandler = tokenPollingStartedHandler;
        this.tokenPollingCompleteHandler = tokenPollingCompleteHandler;
        this.tokenPollingInProgressHandler = tokenPollingInProgressHandler;
        injectorSupplier = Suppliers.memoize(() -> Guice.createInjector(this));

        // Reset in case of connection settings change
        connectionSettingsProvider.addChangeHandler(() -> {
        	logger.debug("Reseting octaneHttpClient and octane, connection settings changed");
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

    @Provides
    public OctaneProvider getOctane() {
        return () -> {
            ConnectionSettings currentConnectionSettings = connectionSettingsProvider.getConnectionSettings();
            if (octane == null) {

                // Will not auth
                octane = new Octane.Builder(currentConnectionSettings.getAuthentication(), getOctaneHttpClient().getOctaneHttpClient())
                        .Server(currentConnectionSettings.getBaseUrl())
                        .workSpace(currentConnectionSettings.getWorkspaceId())
                        .sharedSpace(currentConnectionSettings.getSharedSpaceId())
                        .build();

            }
            return octane;
        };
    }

    @Provides
    public HttpClientProvider getOctaneHttpClient() {
        return () -> {
            ConnectionSettings currentConnectionSettings = connectionSettingsProvider.getConnectionSettings();

            // authenticate and assigning the value to octaneHttpClient needs to be thread safe
            // because of the null check below
            synchronized(this) {
	            if (octaneHttpClient == null) {

	            	logger.debug("Creating http client");

                    ClientType clientType = getClientTypeForServer(currentConnectionSettings);
                    logger.debug("Client type used for connection settings: " + currentConnectionSettings + " is " + clientType);
	            	
	                IdePluginsOctaneHttpClient httpClient = new IdePluginsOctaneHttpClient(currentConnectionSettings.getBaseUrl(), clientType);
	                httpClient.setSsoTokenPollingStartedHandler(tokenPollingStartedHandler);
	                httpClient.setSsoTokenPollingInProgressHandler(tokenPollingInProgressHandler);
	                httpClient.setSsoTokenPollingCompleteHandler(tokenPollingCompleteHandler);
                    httpClient.setLastUsedAuthentication(currentConnectionSettings.getAuthentication());
	
	                boolean authResult = httpClient.authenticate();
	                
	                if (!authResult) {
	                    throw new ServiceRuntimeException("Failed to authenticate to Octane");
	                }
	
	                ServiceModule.this.octaneHttpClient = httpClient;
	            }
            }

            return octaneHttpClient;
        };
    }

    private static ClientType getClientTypeForServer(ConnectionSettings connectionSettings) {
        OctaneVersion octaneVersion = OctaneVersionService.getOctaneVersion(connectionSettings);
        // current version is more than or equal to JUVENTUS_P3
        if(OctaneVersion.LIVERPOOL_P0.compareTo(octaneVersion) > 0) {
            return ClientType.HPE_REST_API_TECH_PREVIEW;
        } else {
            return ClientType.OCTANE_IDE_PLUGIN;
        }
    }

}