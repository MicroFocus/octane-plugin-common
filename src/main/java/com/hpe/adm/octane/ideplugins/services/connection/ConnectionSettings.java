/*******************************************************************************
 * Copyright 2017-2026 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.services.connection;

import com.hpe.adm.nga.sdk.authentication.JSONAuthentication;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.GrantTokenAuthentication;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ConnectionSettings {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionSettings.class.getName());

    private String baseUrl;
    private Long sharedSpaceId;
    private Long workspaceId;
    private JSONAuthentication authentication;

    public ConnectionSettings() {
    }

    public ConnectionSettings(String baseUrl, Long sharedSpaceId, Long workspaceId, JSONAuthentication authentication) {
        this.baseUrl = baseUrl;
        this.sharedSpaceId = sharedSpaceId;
        this.workspaceId = workspaceId;
        this.authentication = authentication;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Long getSharedSpaceId() {
        return sharedSpaceId;
    }

    public void setSharedSpaceId(Long sharedSpaceId) {
        this.sharedSpaceId = sharedSpaceId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public JSONAuthentication getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(JSONAuthentication authentication) {
        this.authentication = authentication;
    }

    public boolean isEmpty() {
        return isStringEmpty(baseUrl) &&
                sharedSpaceId == null &&
                workspaceId == null &&
                authentication == null;
    }

    private boolean isStringEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Settings which both have {@link GrantTokenAuthentication} will never be equal,
     * since the crendentials cannot be predetermined
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConnectionSettings that = (ConnectionSettings) o;

        return Objects.equals(baseUrl, that.baseUrl) &&
                Objects.equals(sharedSpaceId, that.sharedSpaceId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(authentication.getAuthenticationString(), that.authentication.getAuthenticationString()) &&
                !authentication.getClass().equals(GrantTokenAuthentication.class);

    }

    public boolean equalsExceptAuth(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConnectionSettings that = (ConnectionSettings) o;

        return Objects.equals(baseUrl, that.baseUrl) &&
                Objects.equals(sharedSpaceId, that.sharedSpaceId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, sharedSpaceId, workspaceId, authentication);
    }

    /**
     * Set the internal state of the object to math the param
     * 
     * @param connectionSettings
     *            {@link ConnectionSettings} to be copied
     * @return copy of param
     */
    public static ConnectionSettings getCopy(ConnectionSettings connectionSettings) {
        JSONAuthentication authentication;

        try {
            authentication = cloneAuthentication(connectionSettings.getAuthentication());
        } catch (ServiceRuntimeException ex) {
            logger.warn(ex.toString());
            logger.warn("Authentication object will not be cloned");
            authentication = connectionSettings.getAuthentication();
        }

        return new ConnectionSettings(
                connectionSettings.getBaseUrl(),
                connectionSettings.getSharedSpaceId(),
                connectionSettings.getWorkspaceId(),
                authentication);
    }

    private static JSONAuthentication cloneAuthentication(JSONAuthentication authentication) {
        if(authentication instanceof UserAuthentication) {
            UserAuthentication userAuthentication = (UserAuthentication) authentication;
            return new UserAuthentication(userAuthentication.getAuthenticationId(), userAuthentication.getAuthenticationSecret());

        } else if (authentication instanceof GrantTokenAuthentication){
            return new GrantTokenAuthentication();
        }

        throw new ServiceRuntimeException("Failed to clone authentication, unknown type of Authentication");
    }

}
