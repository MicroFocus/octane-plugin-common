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
package com.hpe.adm.octane.ideplugins.services.nonentity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.IOUtils;
import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.IdePluginsOctaneHttpClient;

public class ImageService {

    private final Logger logger = LoggerFactory.getLogger(ImageService.class);

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Inject
    private HttpClientProvider httpClientProvider;

    private File saveImageToTempFile(String pictureLink) throws Exception {

        String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpPath);
        File octanePhotosDir = new File(tmpDir, "Octane_pictures");

        if (!octanePhotosDir.exists()) {
            octanePhotosDir.mkdir();
        }
        int index = pictureLink.lastIndexOf("/");
        String pictureName = pictureLink.substring(index + 1, pictureLink.length());
        File imgFile = new File(octanePhotosDir, pictureName);

        if (!imgFile.exists()) {
            imgFile = new File(octanePhotosDir, pictureName);
        }

        HttpResponse httpResponse = downloadImage(pictureLink, 2);

        try (InputStream is = httpResponse.getContent();
             OutputStream os = new FileOutputStream(imgFile)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return imgFile;
    }

    /**
     * @param pictureLink - src link to the image from server
     * @return
     */
    private HttpResponse downloadImage(String pictureLink, int tryCount) throws Exception {

        HttpResponse httpResponse;
        HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        try {
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(pictureLink));
            HttpCookie lwssoCookie = ((IdePluginsOctaneHttpClient) httpClientProvider.getOctaneHttpClient()).getSessionHttpCookie();
            httpRequest.getHeaders().setCookie(lwssoCookie.toString());
            httpResponse = httpRequest.execute();
        } catch (IOException e) {
            if(e instanceof HttpResponseException && ((HttpResponseException)e).getStatusCode() == 401 && tryCount > 0) {
                //means that the cookie expired
                logger.error("Cookie expired, retrying: " + e.getMessage());
                httpClientProvider.getOctaneHttpClient().authenticate(connectionSettingsProvider.getConnectionSettings().getAuthentication());
                return downloadImage(pictureLink, --tryCount);
            } else {
                logger.error(e.getMessage());
                throw new Exception("Failed to load image from octane");
            }
        }
        return httpResponse;
    }

    public String downloadPictures(String descriptionField) throws Exception{


        //todo check if the image hasnt been swapped meanwhile (new image with the same name uploaded) (*check size, byte with byte, delete with every relog)
        String baseUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl();

        Document descriptionParser = Jsoup.parse(descriptionField);
        Elements link = descriptionParser.getElementsByTag("img");

        for (Element el : link) {
            String pictureLink = el.attr("src");
            if (pictureLink.startsWith("/api/shared_spaces")) {
                el.attr("src", baseUrl + pictureLink);
                pictureLink = el.attr("src");
            }

            if (!pictureLink.contains(baseUrl)) {
                continue;
            }

            el.attr("src", saveImageToTempFile(pictureLink).toURI().toString());
        }
        return descriptionParser.toString();
    }

}