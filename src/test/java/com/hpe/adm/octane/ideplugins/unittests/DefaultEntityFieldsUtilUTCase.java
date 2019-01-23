package com.hpe.adm.octane.ideplugins.unittests;

import com.google.api.client.util.Charsets;
import com.google.common.io.CharStreams;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.DefaultEntityFieldsUtil;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DefaultEntityFieldsUtilUTCase {

    private static String readDefaultFile() {

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream(DefaultEntityFieldsUtil.DEFAULT_FIELDS_FILE_NAME);
            String readJsonString = CharStreams.toString(new InputStreamReader(input, Charsets.UTF_8));
            readJsonString = readJsonString.replaceAll("\\s", "");
            return readJsonString;

        } catch (IOException e) {
            fail("Failed to read " + DefaultEntityFieldsUtil.DEFAULT_FIELDS_FILE_NAME);
        }

        return "";
    }

    @Test
    public void testDefaultFields() {

        String readJsonString = readDefaultFile();

        // let the method read the same file
        Map<Entity, Set<String>> defaultFieldsMap = DefaultEntityFieldsUtil.getDefaultFields();

        String parsedJsonString = DefaultEntityFieldsUtil.entityFieldsToJson(defaultFieldsMap);

        // Compare the data, they have to be idential, order has to be preserved
        String readJsonStringData = new JSONObject(readJsonString).get("data").toString();
        String parsedJsonStringData = new JSONObject(parsedJsonString).get("data").toString();

        assertEquals("Expected parsed then reconverted json to equal to the input file", readJsonStringData, parsedJsonStringData);
    }

    @Test(expected = ServiceRuntimeException.class)
    public void testInvalidVersion() {
        String readJsonString = readDefaultFile();
        JSONObject jsonObject = new JSONObject(readJsonString);
        jsonObject.put("version", -1L);
        DefaultEntityFieldsUtil.entityFieldsFromJson(jsonObject.toString());
    }

}