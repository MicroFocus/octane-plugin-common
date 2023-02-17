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
package com.hpe.adm.octane.ideplugins.services.connection.granttoken;

import com.hpe.adm.nga.sdk.authentication.SimpleClientAuthentication;
import com.hpe.adm.octane.ideplugins.services.connection.IdePluginsOctaneHttpClient;

/**
 * See {@link IdePluginsOctaneHttpClient#grantTokenAuthenticate}
 */
public class GrantTokenAuthentication extends SimpleClientAuthentication {

    /**
     * Neither clientId nor clientSecret are used, therefore they can be set to null
     */
    public GrantTokenAuthentication() {
        super(null, null);
    }

}
