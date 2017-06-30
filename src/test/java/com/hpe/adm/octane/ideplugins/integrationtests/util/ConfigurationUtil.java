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

package com.hpe.adm.octane.ideplugins.integrationtests.util;

import java.io.IOException;
import java.util.Properties;

public class ConfigurationUtil {

    private static final String PROP_PREFIX = "sdk";
    public enum PropertyKeys {
        URL(PROP_PREFIX + ".url"),
        SHAREDSPACE(PROP_PREFIX + ".sharedSpaceId"),
        WORKSPACE(PROP_PREFIX + ".workspaceId"),
        USERNAME(PROP_PREFIX + ".username"),
        PASSWORD(PROP_PREFIX + ".password");

        public String propertyKey;
        PropertyKeys(String propertyKey){
            this.propertyKey = propertyKey;
        }
    }

    private static final String MAIN_CONFIG_FILE_NAME = "configuration.properties";
    private static final Properties properties = new Properties();
    static {
        try {
            properties.load(ConfigurationUtil.class.getClassLoader().getResourceAsStream(MAIN_CONFIG_FILE_NAME));
        } catch (IOException ex){
            throw new RuntimeException("Error occured while loading config file: " + MAIN_CONFIG_FILE_NAME + ", " + ex);
        }
    }

    public static String getString(PropertyKeys property) {
        return properties.getProperty(property.propertyKey) ;
    }

    public static Long getLong(PropertyKeys property) {
        return Long.valueOf(properties.getProperty(property.propertyKey));
    }

}