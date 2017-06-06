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

package com.hpe.adm.octane.services.util;
/**
 * <p>
 * Enum of ClientType and their AccessLevelValue
 * <p>
 * Each client needs to add itself to this ClientType enum
 * A client that wants access to PROTECTED resources needs to define its ClientType AccessLevelValue as AccessLevelValue.PROTECTED
 * and send its own client type in the HPECLIENTTYPE header.
 * A client that wants access to PUBLIC_INTERNAL resources needs to define its ClientType AccessLevelValue as AccessLevelValue.PUBLIC_INTERNAL
 * and send its own client type in the HPECLIENTTYPE header.
 */
public enum ClientType {

    // Production Client Types
    HPE_PUBLIC_API(AccessLevelValue.PUBLIC),
    HPE_SWAGGER_API(AccessLevelValue.PUBLIC),
    HPE_CI_CLIENT(AccessLevelValue.PROTECTED),
    HPE_SYNCHRONIZER(AccessLevelValue.PROTECTED),
    HPE_MQM_UI(AccessLevelValue.PROTECTED),
    HPE_MQM_MOBILE(AccessLevelValue.PROTECTED),
    HPE_REST_TESTS_TEMP(AccessLevelValue.PROTECTED),
    HPE_MQM_PLUGIN_UI(AccessLevelValue.PROTECTED),
    HPE_REST_API_TECH_PREVIEW(AccessLevelValue.PROTECTED),
    HPE_SERVICES(AccessLevelValue.PROTECTED),

    //for the ppm team (in hpe), currently used for com.hp.mqm.rest.platformservices.TimesheetResouce
    HPE_PPM(AccessLevelValue.PUBLIC_INTERNAL),

    // Client Types for IT
    IT_PUBLIC(AccessLevelValue.PUBLIC),
    IT_PUBLIC_INTERNAL(AccessLevelValue.PUBLIC_INTERNAL),
    IT_PROTECTED(AccessLevelValue.PROTECTED);


    public boolean isUIClient() {
        return this == HPE_MQM_UI || this == HPE_MQM_MOBILE;
    }

    private final AccessLevelValue accessLevelValue;

    ClientType(AccessLevelValue accessLevelValue) {
        this.accessLevelValue = accessLevelValue;
    }

    public AccessLevelValue getAccessLevelValue() {
        return this.accessLevelValue;
    }
}