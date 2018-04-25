package com.hpe.adm.octane.ideplugins.services.nonentity;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.IOUtils;
import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.services.connection.ClientLoginCookie;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ImageService {

    private File octanePhotosDir;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Inject
    private ClientLoginCookie  clientLoginCookie;

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    private void saveImage(String pictureLink, String octanePhotosName) {

        HttpResponse httpResponse = clientLoginCookie.getImageData(pictureLink);

        try (InputStream is = httpResponse.getContent();
             OutputStream os = new FileOutputStream(octanePhotosName)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public String downloadPictures(String descriptionField) {
        //todo check if the image hasnt been swapped meanwhile (new image with the same name uploaded) (*check size, byte with byte, delete with every relog)
        String baseUrl = connectionSettingsProvider.getConnectionSettings().getBaseUrl();
        String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpPath);
        octanePhotosDir = new File(tmpDir, "Octane_pictures");

        if (!octanePhotosDir.exists()) {
            octanePhotosDir.mkdir();
        }
        String octanePhotosPath = octanePhotosDir.getAbsolutePath();

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
            int index = pictureLink.lastIndexOf("/");
            String pictureName = pictureLink.substring(index + 1, pictureLink.length());
            String picturePath = octanePhotosPath + "\\" + pictureName;
            File imgFile = new File(octanePhotosDir, pictureName);
            if (!imgFile.exists()) {
                saveImage(pictureLink, picturePath);
                imgFile = new File(octanePhotosDir, pictureName);
            }
            el.attr("src", imgFile.toURI().toString());
        }
        return descriptionParser.toString();
    }

}
