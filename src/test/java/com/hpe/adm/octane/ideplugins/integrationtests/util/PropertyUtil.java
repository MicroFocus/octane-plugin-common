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

package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.hpe.adm.octane.ideplugins.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

public class PropertyUtil {

    public enum PropertyKeys {
        URL("url"),
        SHARED_SPACE("sharedSpaceId"),
        USERNAME("username"),
        PASSWORD("password");

        public String keyStr;
        PropertyKeys(String propertyKey){
            this.keyStr = propertyKey;
        }
    }

    public static ConnectionSettingsProvider readFormVmArgs(){
        Properties props = System.getProperties();
        boolean allPropsPresent =
                Stream.of(
                        PropertyKeys.URL,
                        PropertyKeys.SHARED_SPACE,
                        PropertyKeys.USERNAME,
                        PropertyKeys.PASSWORD
                ).allMatch(propertyKey -> props.keySet().contains(propertyKey.keyStr));

        if(!allPropsPresent){
            return null;
        } else {
            ConnectionSettings connectionSettings = new ConnectionSettings();
            connectionSettings.setBaseUrl(props.getProperty(PropertyKeys.URL.keyStr));
            try {
                connectionSettings.setSharedSpaceId(Long.valueOf(props.getProperty(PropertyKeys.SHARED_SPACE.keyStr)));
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Failed to parse shared space/workspace id as number: " + ex);
            }
            connectionSettings.setUserName(props.getProperty(PropertyKeys.USERNAME.keyStr));
            connectionSettings.setPassword(props.getProperty(PropertyKeys.PASSWORD.keyStr));
            return new BasicConnectionSettingProvider(connectionSettings);
        }
    }

    private static final String MAIN_CONFIG_FILE_NAME = "configuration.properties";
    private static Properties fileProps;

    private static String getString(PropertyKeys property) {
        return fileProps.getProperty(property.keyStr) ;
    }
    private static Long getLong(PropertyKeys property) {
        return Long.valueOf(fileProps.getProperty(property.keyStr));
    }

    public static ConnectionSettingsProvider readFromPropFile(){

        if(fileProps == null) {
            try {
                fileProps = new Properties();
                fileProps.load(PropertyUtil.class.getClassLoader().getResourceAsStream(MAIN_CONFIG_FILE_NAME));
            } catch (IOException ex) {
                throw new RuntimeException("Error occured while loading config file: " + MAIN_CONFIG_FILE_NAME + ", " + ex);
            }
        }

        boolean allPropsPresent =
                Stream.of(
                        PropertyKeys.URL,
                        PropertyKeys.SHARED_SPACE,
                        PropertyKeys.USERNAME,
                        PropertyKeys.PASSWORD
                ).allMatch(propertyKey -> fileProps.keySet().contains(propertyKey.keyStr));

        if(!allPropsPresent) {
            return null;
        } else {
            ConnectionSettings connectionSettings = new ConnectionSettings();
            connectionSettings.setBaseUrl(getString(PropertyKeys.URL));
            connectionSettings.setSharedSpaceId(getLong(PropertyKeys.SHARED_SPACE));
            connectionSettings.setUserName(getString(PropertyKeys.USERNAME));
            connectionSettings.setPassword(getString(PropertyKeys.PASSWORD));
            return new BasicConnectionSettingProvider(connectionSettings);
        }
    }

}