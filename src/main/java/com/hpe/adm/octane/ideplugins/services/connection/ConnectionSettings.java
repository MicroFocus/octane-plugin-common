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

import java.util.Objects;

import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.octane.ideplugins.services.connection.sso.SsoAuthentication;

public class ConnectionSettings {

    private String baseUrl;
    private Long sharedSpaceId;
    private Long workspaceId;
    private Authentication authentication;

    public ConnectionSettings() {
    }

    public ConnectionSettings(String baseUrl, Long sharedSpaceId, Long workspaceId, Authentication authentication) {
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

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(Authentication authentication) {
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
     * Settings which both have {@link SsoAuthentication} will never be equal,
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
                Objects.equals(authentication.getClass(), that.authentication.getClass()) &&
                Objects.equals(authentication.getAuthenticationString(), that.authentication.getAuthenticationString()) &&
                !authentication.getClass().equals(SsoAuthentication.class);

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
        return new ConnectionSettings(
                connectionSettings.getBaseUrl(),
                connectionSettings.getSharedSpaceId(),
                connectionSettings.getWorkspaceId(),
                connectionSettings.getAuthentication()); // TODO: BAD BAD DOES
                                                         // NOT COPY
    }
}
