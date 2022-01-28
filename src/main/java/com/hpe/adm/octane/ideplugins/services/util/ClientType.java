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
package com.hpe.adm.octane.ideplugins.services.util;

public enum ClientType {

    HPE_PUBLIC_API,
    HPE_SWAGGER_API,
    HPE_ODATA,
    HPE_CI_CLIENT,
    HPE_SYNCHRONIZER,
    HPE_MQM_UI,
    HPE_MQM_MOBILE,
    HPE_REST_TESTS_TEMP,
    HPE_MQM_PLUGIN_UI,
    OCTANE_IDE_PLUGIN,
    HPE_SERVICES,
    HPE_PPM,
    MF_E_SIGN,
    IT_PUBLIC,
    IT_PUBLIC_INTERNAL,
    IT_PUBLIC_TECH_PREVIEW,
    IT_PRIVATE,

    @Deprecated
    HPE_REST_API_TECH_PREVIEW
    
}