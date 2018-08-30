package com.hpe.adm.octane.ideplugins.services.connection.sso;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.exception.OctanePartialException;
import com.hpe.adm.nga.sdk.model.*;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.nga.sdk.network.google.GoogleHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.*;


@SuppressWarnings("ALL")
public class SsoLoginGoogleHttpClient extends GoogleHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(SsoLoginGoogleHttpClient.class.getName());

    private static final String LOGGER_REQUEST_FORMAT = "Request: {} - {} - {}";
    private static final String LOGGER_RESPONSE_FORMAT = "Response: {} - {} - {}";

    private static final String SET_COOKIE = "set-cookie";
    private static final String ERROR_CODE_TOKEN_EXPIRED = "VALIDATION_TOKEN_EXPIRED_IDLE_TIME_OUT";

    private final Map<OctaneHttpRequest, OctaneHttpResponse> cachedRequestToResponse = new HashMap<>();
    private final Map<OctaneHttpRequest, String> requestToEtagMap = new HashMap<>();

    private static final int HTTP_REQUEST_RETRY_COUNT = 1;

    private long pollingTimeoutMillis = 1000 * 60;
    private String sessionCookieName = "LWSSO_COOKIE_KEY";

    public class PollingStatus {
        public long timeoutTimeStamp;
        public Boolean shouldPoll = true; //needs to be an object to be able to modify it while it's being passed as a parameter
    }

    public interface SsoTokenPollingStartedHandler {
        void pollingStarted(String loginUrl);
    }

    public interface SsoTokenPollingInProgressHandler {
        void polling(PollingStatus pollingStatus);
    }

    public interface SsoTokenPollingCompleteHandler {
        void pollingComplete();
    }

    private SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler;
    private SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler;
    private SsoTokenPollingInProgressHandler ssoTokenPollingInProgressHandler;

    public SsoLoginGoogleHttpClient(String urlDomain) {
        super(urlDomain);

        //Have to reinit
        requestInitializer = request -> {
            request.setResponseInterceptor(response -> {
                // retrieve new LWSSO in response if any
                HttpHeaders responseHeaders = response.getHeaders();
                updateLWSSOCookieValue(responseHeaders);
            });

            request.setUnsuccessfulResponseHandler((httpRequest, httpResponse, b) -> false);

            final StringBuilder cookieBuilder = new StringBuilder();
            if (lwssoValue != null && !lwssoValue.isEmpty()) {
                cookieBuilder.append(sessionCookieName).append("=").append(lwssoValue);
            }
            if (octaneUserValue != null && !octaneUserValue.isEmpty()) {
                cookieBuilder.append(";").append(OCTANE_USER_COOKIE_KEY).append("=").append(octaneUserValue);
            }

            request.getHeaders().setCookie(cookieBuilder.toString());

            if (lastUsedAuthentication != null) {
                String clientTypeHeader = lastUsedAuthentication.getClientHeader();
                if (clientTypeHeader != null && !clientTypeHeader.isEmpty()) {
                    request.getHeaders().set(HPE_CLIENT_TYPE, clientTypeHeader);
                }
            }
            request.setReadTimeout(60000);
        };

        HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        requestFactory = HTTP_TRANSPORT.createRequestFactory(requestInitializer);
    }

    public void setSsoTokenPollingStartedHandler(SsoTokenPollingStartedHandler ssoTokenPollingStartedHandler) {
        this.ssoTokenPollingStartedHandler = ssoTokenPollingStartedHandler;
    }

    public void setSsoTokenPollingCompleteHandler(SsoTokenPollingCompleteHandler ssoTokenPollingCompleteHandler) {
        this.ssoTokenPollingCompleteHandler = ssoTokenPollingCompleteHandler;
    }

    public void setSsoTokenPollingInProgressHandler(SsoTokenPollingInProgressHandler ssoTokenPollingInProgressHandler) {
        this.ssoTokenPollingInProgressHandler = ssoTokenPollingInProgressHandler;
    }

    @Override
    public synchronized boolean authenticate(Authentication authentication) {

        if (authentication instanceof SsoAuthentication) {

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

                long pollingTimeoutTimestamp = new Date().getTime() + this.pollingTimeoutMillis;

                while(pollingTimeoutTimestamp > new Date().getTime()) {

                    if(ssoTokenPollingInProgressHandler != null) {
                        PollingStatus pollingStatus = new PollingStatus();
                        pollingStatus.timeoutTimeStamp = pollingTimeoutTimestamp;

                        ssoTokenPollingInProgressHandler.polling(pollingStatus);

                        if(pollingStatus.shouldPoll.equals(Boolean.FALSE)) {
                            break;
                        }
                    }

                    try {
                        HttpRequest pollRequest = requestFactory.buildPostRequest(new GenericUrl(urlDomain + "/authentication/grant_tool_token"), identifierRequestContent);
                        pollResponse = new JSONObject(pollRequest.execute().parseAsString());
                    } catch (Exception ex) {
                        logger.debug("Polling for grant_tool_token");
                        try {
                            Thread.sleep(1000L); //Do not DOS the server, not cool
                        } catch (InterruptedException e) {
                            continue;
                        }
                        continue;
                    }

                    lastUsedAuthentication = authentication;
                    lwssoValue = pollResponse.getString("access_token");
                    sessionCookieName = pollResponse.getString("cookie_name");

                    if(ssoTokenPollingCompleteHandler != null) {
                        ssoTokenPollingCompleteHandler.pollingComplete();
                    }

                    return true;
                }

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
        return new HttpCookie(sessionCookieName, lwssoValue);
    }

    @Override
    public OctaneHttpResponse execute(OctaneHttpRequest octaneHttpRequest) {
        return execute(octaneHttpRequest, HTTP_REQUEST_RETRY_COUNT);
    }

    /**
     * This method can be used internally to retry the request in case of auth token timeout
     * Careful, this method calls itself recursively to retry the request
     *
     * @param octaneHttpRequest abstract request, has to be converted into a specific implementation of http request
     * @param retryCount        number of times the method should retry the request if it encounters an HttpResponseException
     * @return OctaneHttpResponse
     */
    private OctaneHttpResponse execute(OctaneHttpRequest octaneHttpRequest, int retryCount) {

        final HttpRequest httpRequest = convertOctaneRequestToGoogleHttpRequest(octaneHttpRequest);
        final HttpResponse httpResponse;

        try {
            httpResponse = executeRequest(httpRequest);

            final OctaneHttpResponse octaneHttpResponse = convertHttpResponseToOctaneHttpResponse(httpResponse);
            final String eTag = httpResponse.getHeaders().getETag();
            if (eTag != null) {
                requestToEtagMap.put(octaneHttpRequest, eTag);
                cachedRequestToResponse.put(octaneHttpRequest, octaneHttpResponse);
            }
            return octaneHttpResponse;

        } catch (RuntimeException exception) {

            //Return cached response
            if(exception.getCause() instanceof HttpResponseException) {
                HttpResponseException httpResponseException = (HttpResponseException) exception.getCause();
                final int statusCode = httpResponseException.getStatusCode();
                if (statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                    return cachedRequestToResponse.get(octaneHttpRequest);
                }
            }

            //Handle session timeout exception
            if(retryCount > 0 && exception instanceof OctaneException) {
                OctaneException octaneException = (OctaneException) exception;
                StringFieldModel errorCodeFieldModel = (StringFieldModel) octaneException.getError().getValue("errorCode");

                //Handle session timeout
                if (errorCodeFieldModel != null && ERROR_CODE_TOKEN_EXPIRED.equals(errorCodeFieldModel.getValue()) && lastUsedAuthentication != null) {
                    logger.debug("Auth token expired, trying to re-authenticate");
                    try {
                        lwssoValue = ""; //force re-auth
                        authenticate(lastUsedAuthentication);
                    } catch (OctaneException ex) {
                        logger.debug("Exception while retrying authentication: {}", ex.getMessage());
                    }
                    logger.debug("Retrying request, retries left: {}", retryCount);
                    return execute(octaneHttpRequest, --retryCount);
                }
            }

            throw exception;
        }
    }

    private HttpResponse executeRequest(final HttpRequest httpRequest) {
        logger.debug(LOGGER_REQUEST_FORMAT, httpRequest.getRequestMethod(), httpRequest.getUrl().toString(), httpRequest.getHeaders().toString());

        final HttpContent content = httpRequest.getContent();

        // Make sure you don't log any http content send to the login rest api, since you don't want credentials in the logs
        if (content != null && logger.isDebugEnabled() && !httpRequest.getUrl().toString().contains(OAUTH_AUTH_URL)) {
            logHttpContent(content);
        }

        HttpResponse response;
        try {
            response = httpRequest.execute();
        } catch (Exception e) {
            throw wrapException(e);
        }

        logger.debug(LOGGER_RESPONSE_FORMAT, response.getStatusCode(), response.getStatusMessage(), response.getHeaders().toString());
        return response;
    }

    private static RuntimeException wrapException(Exception exception) {
        if(exception instanceof HttpResponseException) {

            HttpResponseException httpResponseException = (HttpResponseException) exception;
            logger.debug(LOGGER_RESPONSE_FORMAT, httpResponseException.getStatusCode(), httpResponseException.getStatusMessage(), httpResponseException.getHeaders().toString());

            List<String> exceptionContentList = new ArrayList<>();
            exceptionContentList.add(httpResponseException.getStatusMessage());
            exceptionContentList.add(httpResponseException.getContent());

            for(String exceptionContent : exceptionContentList) {
                try {
                    if(ModelParser.getInstance().hasErrorModels(exceptionContent)) {
                        Collection<ErrorModel> errorModels = ModelParser.getInstance().getErrorModels(exceptionContent);
                        Collection<EntityModel> entities = ModelParser.getInstance().getEntities(exceptionContent);
                        return new OctanePartialException(errorModels, entities);
                    } else {
                        ErrorModel errorModel = ModelParser.getInstance().getErrorModelFromjson(exceptionContent);
                        errorModel.setValue(new LongFieldModel("http_status_code", (long) httpResponseException.getStatusCode()));
                        return new OctaneException(errorModel);
                    }
                } catch (Exception ignored) {}
            }
        }

        //In case nothing in exception is parsable
        return new RuntimeException(exception);
    }

    /**
     * Util method to debug log {@link HttpContent}. This method will avoid logging {@link InputStreamContent}, since
     * reading from the stream will probably make it unusable when the actual request is sent
     *
     * @param content {@link HttpContent}
     */
    private static void logHttpContent(HttpContent content) {
        if (content instanceof MultipartContent) {
            MultipartContent multipartContent = ((MultipartContent) content);
            logger.debug("MultipartContent: " + content.getType());
            multipartContent.getParts().forEach(part -> {
                logger.debug("Part: encoding: " + part.getEncoding() + ", headers: " + part.getHeaders());
                logHttpContent(part.getContent());
            });
        } else if (content instanceof InputStreamContent) {
            logger.debug("InputStreamContent: type: " + content.getType());
        } else if (content instanceof FileContent) {
            logger.debug("FileContent: type: " + content.getType() + ", filepath: " + ((FileContent) content).getFile().getAbsolutePath());
        } else {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                content.writeTo(byteArrayOutputStream);
                logger.debug("Content: type: " + content.getType() + ", " + byteArrayOutputStream.toString());
            } catch (IOException ex) {
                logger.error("Failed to log content of " + content, ex);
            }
        }
    }

    /**
     * Retrieve new cookie from set-cookie header
     *
     * @param headers The headers containing the cookie
     * @return true if LWSSO cookie is renewed
     */
    private boolean updateLWSSOCookieValue(HttpHeaders headers) {
        boolean renewed = false;
        List<String> strHPSSOCookieCsrf1 = headers.getHeaderStringValues(SET_COOKIE);
        if (strHPSSOCookieCsrf1.isEmpty()) {
            return false;
        }

        /* Following code failed to parse set-cookie to get LWSSO cookie due to cookie version, check RFC 2965
        String strCookies = strHPSSOCookieCsrf1.toString();
        List<HttpCookie> Cookies = java.net.HttpCookie.parse(strCookies.substring(1, strCookies.length()-1));
        lwssoValue = Cookies.stream().filter(a -> a.getName().equals(LWSSO_COOKIE_KEY)).findFirst().get().getValue();*/
        for (String strCookie : strHPSSOCookieCsrf1) {
            List<HttpCookie> cookies;
            try {
                // Sadly the server seems to send back empty cookies for some reason
                cookies = HttpCookie.parse(strCookie);
            } catch (Exception ex) {
                logger.error("Failed to parse HPSSOCookieCsrf: " + ex.getMessage());
                continue;
            }
            Optional<HttpCookie> lwssoCookie = cookies.stream().filter(a -> a.getName().equals(sessionCookieName)).findFirst();
            if (lwssoCookie.isPresent()) {
                lwssoValue = lwssoCookie.get().getValue();
                renewed = true;
            } else {
                cookies.stream().filter(cookie -> cookie.getName().equals(OCTANE_USER_COOKIE_KEY)).findAny().ifPresent(cookie -> octaneUserValue = cookie.getValue());
            }
        }

        return renewed;
    }

}