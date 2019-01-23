package com.hpe.adm.octane.ideplugins.services.connection;

import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;

/**
 * {@link SimpleUserAuthentication} that exposes the username and password field
 */
public class UserAuthentication extends SimpleUserAuthentication {

    public UserAuthentication(String userName, String password) {
        super(userName, password);
    }

    @Override
    public String getUserName() {
        return super.getUserName();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

}