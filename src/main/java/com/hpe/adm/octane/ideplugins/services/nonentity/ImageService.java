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

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.IOUtils;
import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.services.connection.ClientLoginCookieProvider;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpCookie;

public class ImageService {

    private final Logger logger = LoggerFactory.getLogger(ImageService.class.getClass());

    @Inject
    private ClientLoginCookieProvider clientLoginCookieProvider;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private static HttpCookie lwssoCookie;
    private static Runnable resetLwssoCookie = () -> lwssoCookie = null;


    private File saveImageToTempFile(String pictureLink) {

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
    private HttpResponse downloadImage(String pictureLink, int tryCount) {
        if (lwssoCookie == null) {
            lwssoCookie = clientLoginCookieProvider.get();
        }

        HttpResponse httpResponse;
        HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        try {
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(pictureLink));
            httpRequest.getHeaders().setCookie(lwssoCookie.toString());
            httpResponse = httpRequest.execute();
        } catch (IOException e) {
            if(e instanceof HttpResponseException && ((HttpResponseException)e).getStatusCode() == 401 && tryCount > 0) {
                //means that the cookie expired
                logger.error("Cookie expired, retrying: " + e.getMessage());
                lwssoCookie = clientLoginCookieProvider.get();
                return downloadImage(pictureLink, --tryCount);
            } else {
                logger.error(e.getMessage());
                return null;
            }
        }
        return httpResponse;
    }

    public String downloadPictures(String descriptionField) {

        //Reset cookie in case connection settings change
        if(!connectionSettingsProvider.hasChangeHandler(resetLwssoCookie)) {
            connectionSettingsProvider.addChangeHandler(resetLwssoCookie);
        }

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