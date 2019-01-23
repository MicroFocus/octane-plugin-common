package com.hpe.adm.octane.ideplugins.services.connection;

import com.hpe.adm.nga.sdk.authentication.Authentication;
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
                Objects.equals(authentication.getClass(), that.authentication.getClass()) &&
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
        Authentication authentication;

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

    private static Authentication cloneAuthentication(Authentication authentication) {
        if(authentication instanceof UserAuthentication) {
            UserAuthentication userAuthentication = (UserAuthentication) authentication;
            return new UserAuthentication(userAuthentication.getUserName(), userAuthentication.getPassword());

        } else if (authentication instanceof GrantTokenAuthentication){
            return new GrantTokenAuthentication();
        }

        throw new ServiceRuntimeException("Failed to clone authentication, unknown type of Authentication");
    }

}
