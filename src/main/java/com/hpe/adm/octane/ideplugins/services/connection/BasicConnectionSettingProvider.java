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
package com.hpe.adm.octane.ideplugins.services.connection;

import java.util.ArrayList;
import java.util.List;

public class BasicConnectionSettingProvider implements ConnectionSettingsProvider {

    protected ConnectionSettings connectionSettings = new ConnectionSettings();

    public BasicConnectionSettingProvider() {
    }

    public BasicConnectionSettingProvider(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    private List<Runnable> changeHandlers = new ArrayList<>();

    @Override
    public void addChangeHandler(Runnable changeHandler) {
        changeHandlers.add(changeHandler);
    }

    @Override
    public boolean hasChangeHandler(Runnable observer) {
        return changeHandlers.contains(observer);
    }

    private void callChangeHandlers() {
        changeHandlers.forEach(handler -> handler.run());
    }

    @Override
    public ConnectionSettings getConnectionSettings() {
        return ConnectionSettings.getCopy(connectionSettings);
    }

    @Override
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
        callChangeHandlers();
    }

}
