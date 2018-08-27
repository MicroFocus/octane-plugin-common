package com.hpe.adm.octane.ideplugins.services.connection.sso;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.Calendar;


public class SsoLoginGoogleHttpClient extends GoogleHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(SsoLoginGoogleHttpClient.class.getName());

    private long pollingTimeoutMillis = 1000 * 60 * 2;

    public interface SsoTokenPollingStartedHandler {
        void pollingStarted(String loginUrl);
    }

    public interface SsoTokenPollingCompleteHandler {
        void pollingComplete();
    }

    private SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler;
    private SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler;

    public SsoLoginGoogleHttpClient(String urlDomain) {
        super(urlDomain);
    }

    public SsoTokenPollingStartedHandler getSsoTokenPollingStartedHandler() {
        return ssoTokenPollingStartedHandler;
    }

    public void setSsoTokenPollingStartedHandler(SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler) {
        this.ssoTokenPollingStartedHandler = ssoTokenPollingStartedHandler;
    }

    public SsoTokenPollingCompleteHandler getSsoTokenPollingCompleteHandler() {
        return ssoTokenPollingCompleteHandler;
    }

    public void setSsoTokenPollingCompleteHandler(SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler) {
        this.ssoTokenPollingCompleteHandler = ssoTokenPollingCompleteHandler;
    }

    @Override
    public boolean authenticate(Authentication authentication) {
        if (authentication instanceof SsoAuthentication) {

            SsoAuthentication ssoAuthentication = (SsoAuthentication) authentication;
            Calendar calendar = Calendar.getInstance();

            //do not authenticate if lwssoValue is true
            if (!lwssoValue.isEmpty()) {
                return true;
            }

            try {
                HttpRequest identifierRequest = requestFactory.buildGetRequest(new GenericUrl(urlDomain + "/authentication/grant_tool_token"));
                JSONObject identifierResponse = new JSONObject(identifierRequest.execute().parseAsString());

                if(ssoTokenPollingStartedHandler != null) {
                    ssoTokenPollingStartedHandler.pollingStarted(identifierResponse.getString("authentication_url"));
                }

                JSONObject identifierRequestJson = new JSONObject();
                identifierRequestJson.put("identifier", identifierResponse.getString("identifier"));
                ByteArrayContent identifierRequestContent = ByteArrayContent.fromString("application/json", identifierRequestJson.toString());
                JSONObject pollResponse;

                long pollingTimeoutTimestamp = calendar.getTimeInMillis() + this.pollingTimeoutMillis;

                while(pollingTimeoutTimestamp > calendar.getTimeInMillis()) {

                    try {
                        HttpRequest pollRequest = requestFactory.buildPostRequest(new GenericUrl(urlDomain + "/authentication/grant_tool_token"), identifierRequestContent);
                        pollResponse = new JSONObject(pollRequest.execute().parseAsString());
                    } catch (Exception ex) {
                        logger.debug("Polling for grant_tool_token");
                        continue;
                    }

                    lastUsedAuthentication = authentication;
                    lwssoValue = pollResponse.getString("access_token");

                    if(ssoTokenPollingCompleteHandler != null) {
                        ssoTokenPollingCompleteHandler.pollingComplete();
                    }

                    return true;
                }

                throw new RuntimeException("Login timed out.");

            } catch (IOException ex) {
                logger.error("Failed to get grant_tool_token: " + ex);
            }

            return false;
        } else {
            return super.authenticate(authentication);
        }
    }

    public HttpCookie getSessionHttpCookie() {
        if (lwssoValue == null && lastUsedAuthentication != null) {
            authenticate(lastUsedAuthentication);
        }
        return new HttpCookie("LWSSO_COOKIE_KEY", lwssoValue);
    }

}