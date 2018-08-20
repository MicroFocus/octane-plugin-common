package com.hpe.adm.octane.ideplugins.services.connection.sso;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class SsoLoginGoogleHttpClient extends GoogleHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(SsoLoginGoogleHttpClient.class.getName());

    public SsoLoginGoogleHttpClient(String urlDomain) {
        super(urlDomain);
    }

    @Override
    public boolean authenticate(Authentication authentication) {
        if(authentication instanceof SsoAuthentication) {

            try {
                HttpRequest identifierRequest = requestFactory.buildGetRequest(new GenericUrl(urlDomain + "/authentication/grant_tool_token"));
                JSONObject identifierResponse = new JSONObject(identifierRequest.execute().parseAsString());

                // TODO: temporary
                Desktop.getDesktop().browse(new URI(identifierResponse.getString("authentication_url")));

                JSONObject identifierRequestJson = new JSONObject();
                identifierRequestJson.put("identifier", identifierResponse.getString("identifier"));
                ByteArrayContent identifierRequestContent = ByteArrayContent.fromString("application/json", identifierRequestJson.toString());
                JSONObject pollResponse;

                for (int i = 0; i < 60; i++) {

                    try {
                        HttpRequest pollRequest = requestFactory.buildPostRequest(new GenericUrl(urlDomain + "/authentication/grant_tool_token"), identifierRequestContent);
                        pollResponse = new JSONObject(pollRequest.execute().parseAsString());
                    } catch (Exception ex) {
                        logger.debug("Polling for grant_tool_token, tries: " + (i + 1));
                        continue;
                    }

                    lastUsedAuthentication = authentication;
                    lwssoValue = pollResponse.getString("access_token");
                    return true;
                }


            } catch (IOException | URISyntaxException ex) {
                logger.error("Failed to get grant_tool_token: " + ex);
            }

            return false;
        } else {
           return super.authenticate(authentication);
        }
    }

}