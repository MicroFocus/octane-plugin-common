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

package com.hpe.adm.octane.ideplugins.services.connection;

import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;

/**
 * {@link SimpleUserAuthentication} that exposes the username and password field
 */
public class UserAuthentication extends SimpleUserAuthentication {

    public UserAuthentication(String userName, String password) {
        super(userName, password, ServiceModule.CLIENT_TYPE.name());
    }

    @Override
    public String getUserName() {
        return super.getUserName();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

}