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

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.authentication.SimpleUserAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.HttpCookie;
import java.util.List;
import java.util.Optional;


public class ClientLoginCookie {

    private HttpResponse httpResponse;
    private HttpCookie lwssoCookie;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    public HttpResponse loginClient() {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        HttpRequest httpRequest;
        HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        SimpleUserAuthentication authentication = new SimpleUserAuthentication(connectionSettings.getUserName(), connectionSettings.getPassword());
        ByteArrayContent content = ByteArrayContent.fromString("application/json", authentication.getAuthenticationString());

        try {
            httpRequest = requestFactory.buildPostRequest(new GenericUrl(connectionSettings.getBaseUrl() + "/authentication/sign_in"),
                    content);
            httpResponse = httpRequest.execute();
            httpResponse.disconnect();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        return httpResponse;

    }

    private HttpCookie setLwssoCookie() {
        List<String> strHPSSOCookieCsrf1 = httpResponse.getHeaders().getHeaderStringValues("Set-Cookie");

        for (String strCookie : strHPSSOCookieCsrf1) {
            List<HttpCookie> cookies = HttpCookie.parse(strCookie);
            Optional<HttpCookie> lwssoCookieOp = cookies.stream().filter(a -> a.getName().equals("LWSSO_COOKIE_KEY")).findFirst();
            if (lwssoCookieOp.isPresent()) {
                lwssoCookie = lwssoCookieOp.get();
                break;
            } else {
                return null;
            }
        }
        return lwssoCookie;
    }

    /**
     * @param pictureLink - src link to the image from server
     * @return
     */
    public HttpResponse getImageData(String pictureLink) {
        if (lwssoCookie == null) {
            loginClient();
            lwssoCookie = setLwssoCookie();
        }

        HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        try {
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(pictureLink));
            httpRequest.getHeaders().setCookie(lwssoCookie.toString());
            httpResponse = httpRequest.execute();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        //test http request format when cookie is expired
        if(httpResponse.getStatusCode() == 403){
            //means that the cookie expired
            loginClient();
            httpResponse = getImageData(pictureLink);
        }
        return httpResponse;
    }
}
