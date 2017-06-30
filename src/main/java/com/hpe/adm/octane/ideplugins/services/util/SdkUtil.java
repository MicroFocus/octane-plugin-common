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

package com.hpe.adm.octane.ideplugins.services.util;


import com.hpe.adm.nga.sdk.exception.OctaneException;

public class SdkUtil {

    /**
     * Attempt to get the error message from and OctaneException, if it fails returns null instead
     *
     * @param ex
     * @return
     */
    public static String getMessageFromOctaneException(OctaneException ex) {
        String message = ex.getError().getDescription();

        if (message.contains("401")) {
            message = "The username or the password is incorrect.";
        } else if (message.contains("404") || message.contains("500")) {
            if (message.contains("does not exist") && !message.contains("workspace")) {
                return "The requested entity does not exist";
            }
            message = "The sharedspace or the workspace is incorrect.";
        } else {
            message = "General octane connection error.";
        }

        return message;
    }
}
