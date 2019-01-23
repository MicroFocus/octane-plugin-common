package com.hpe.adm.octane.ideplugins.services.connection;

public interface ConnectionSettingsProvider {

    /**
     * This returns a copy, changing it won't change the provider
     * @return copy of {@link ConnectionSettings}
     */
    ConnectionSettings getConnectionSettings();

    /**
     * This allows you to change what the provider returns, will fire the change handlers
     * @param connectionSettings valid {@link ConnectionSettings}, does not modify param
     */
    void setConnectionSettings(ConnectionSettings connectionSettings);


    void addChangeHandler(Runnable observer);

    boolean hasChangeHandler(Runnable observer);
}
