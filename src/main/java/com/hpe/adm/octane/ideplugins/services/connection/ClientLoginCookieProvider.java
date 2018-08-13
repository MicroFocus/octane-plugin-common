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
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.util.List;
import java.util.Optional;


public class ClientLoginCookieProvider implements Provider<HttpCookie>{

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Override
    public HttpCookie get() {
        // Disable the CookieHandler for this request, since if the cached login cookie is
        // sent to Octane in the login post request, the renewed login cookie would not be visible
        // Or something like that, no idea
        CookieHandler cookieHandler = CookieHandler.getDefault();
        CookieHandler.setDefault(null);

        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        HttpRequest httpRequest;
        HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();

        ByteArrayContent content = ByteArrayContent.fromString("application/json", connectionSettings.getAuthentication().getAuthenticationString());

        HttpResponse httpResponse;
        try {
            GenericUrl url = new GenericUrl(connectionSettings.getBaseUrl() + "/authentication/sign_in");
            httpRequest = requestFactory.buildPostRequest(url, content);
            httpResponse = httpRequest.execute();
            httpResponse.disconnect();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }

        List<String> strHPSSOCookieCsrf1 = httpResponse.getHeaders().getHeaderStringValues("Set-Cookie");
        HttpCookie lwssoCookie = null;
        for (String strCookie : strHPSSOCookieCsrf1) {
            List<HttpCookie> cookies = HttpCookie.parse(strCookie);
            Optional<HttpCookie> lwssoCookieOp = cookies.stream().filter(a -> a.getName().equals("LWSSO_COOKIE_KEY")).findFirst();
            if (lwssoCookieOp.isPresent()) {
                lwssoCookie = lwssoCookieOp.get();
                break;
            }
        }

        // Restore the old cookie handler after the request is done
        // God knows how many requests in other threads this will destroy
        CookieHandler.setDefault(cookieHandler);
        return lwssoCookie;
    }
}
