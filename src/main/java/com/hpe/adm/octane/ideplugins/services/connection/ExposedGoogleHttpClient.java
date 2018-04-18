package com.hpe.adm.octane.ideplugins.services.connection;

import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;

public class ExposedGoogleHttpClient extends GoogleHttpClient {

    public ExposedGoogleHttpClient(String urlDomain) {
        super(urlDomain);
    }

    public String getCookieValue(){
        return lwssoValue;
    }

}
