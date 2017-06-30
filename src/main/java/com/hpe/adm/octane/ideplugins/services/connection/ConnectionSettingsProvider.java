/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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

package com.hpe.adm.octane.ideplugins.services.connection;

public interface ConnectionSettingsProvider {

    /**
     * This returns a copy, changing it won't change the provider
     * @return copy of {@link ConnectionSettings}
     */
    ConnectionSettings getConnectionSettings();

    /**
     * This allows you to change what the provider returns, will fire the change handlers
     * @param connectionSettings valid {@link ConnectionSettings}, does not modify param
     */
    void setConnectionSettings(ConnectionSettings connectionSettings);


    void addChangeHandler(Runnable observer);
}
