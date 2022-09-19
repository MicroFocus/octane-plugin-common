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

import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;

/**
 * {@link SimpleUserAuthentication} that exposes the username and password field
 */
public class UserAuthentication extends SimpleUserAuthentication {

    public UserAuthentication(String userName, String password) {
        super(userName, password);
    }

    @Override
    public String getAuthenticationId() {
        return super.getAuthenticationId();
    }

    @Override
    public String getAuthenticationSecret() {
        return super.getAuthenticationSecret();
    }

}