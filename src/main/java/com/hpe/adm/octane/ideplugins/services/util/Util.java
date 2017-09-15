/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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

package com.hpe.adm.octane.ideplugins.services.util;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.model.MultiReferenceFieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.ui.FormField;
import com.hpe.adm.octane.ideplugins.services.ui.FormLayout;
import com.hpe.adm.octane.ideplugins.services.ui.FormLayoutSection;
import org.apache.commons.lang.CharEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Util {
    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    private static final Logger logger = LogManager.getLogger(Util.class.getName());

    /**
     * This method is for displaying in the UI only
     *
     * @param fieldModel from an {@link EntityModel}
     * @return string value of the field
     */
    public static String getUiDataFromModel(FieldModel fieldModel) {
        return getUiDataFromModel(fieldModel, "name");
    }

    /**
     * This method is for displaying in the UI only
     *
     * @param fieldModel fieldModel from an {@link EntityModel}
     * @param neededProperty can check {@link ReferenceFieldModel} and {@link MultiReferenceFieldModel} for property to use
     * @return string value of the field
     */
    public static String getUiDataFromModel(FieldModel fieldModel, String neededProperty) {
        String result = "";
        if (null != fieldModel) {
            FieldModel tempFieldModel = null;
            if (fieldModel instanceof ReferenceFieldModel) {
                tempFieldModel = getValueOfChild((EntityModel) fieldModel.getValue(), neededProperty);
                if (null != tempFieldModel) {
                    result = String.valueOf(tempFieldModel.getValue());
                }
            } else if (fieldModel instanceof MultiReferenceFieldModel) {
                result = getValueOfChildren((List<EntityModel>) fieldModel.getValue(), neededProperty);
            } else {
                //In case of dates, we need to convert to local timezone
                if (fieldModel.getValue() instanceof ZonedDateTime) {
                    ZonedDateTime serverdateTime = (ZonedDateTime) fieldModel.getValue();
                    ZonedDateTime localTime = serverdateTime.withZoneSameInstant(ZoneId.systemDefault());
                    result = localTime.toLocalDateTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
                } else {
                    result = String.valueOf(fieldModel.getValue());
                }
            }
        }

        //if the string happens to be valid json, strip it down to look like a normal string
        try {
            new JSONObject(result);
            //in case it is json, make it pretty!
            result = result.replaceAll("\"", "");
            result = result.replaceAll("\\}", "");
            result = result.replaceAll("\\{", "");
        } catch (JSONException ex) {
        }

        return (null == result) ? " " : result;
    }

    private static FieldModel getValueOfChild(EntityModel entityModel, String child) {
        FieldModel result = null;
        if (null != entityModel) {
            for (FieldModel fieldModel : entityModel.getValues()) {
                if (child.equals(fieldModel.getName())) {
                    result = fieldModel;
                }
            }
        }
        return result;
    }

    public static FieldModel getNonNullValue(EntityModel entityModel, String... keys) {
        for (String key : keys) {
            FieldModel childModel = getValueOfChild(entityModel, key);
            if (childModel != null && childModel.getValue() != null)
                return childModel;
        }
        return null;
    }

    public static FieldModel getContainerItemForCommentModel(EntityModel commentModel) {
        return getNonNullValue(commentModel, "owner_work_item", "owner_test", "owner_run");
    }

    private static String getValueOfChildren(List<EntityModel> entityModelList, String child) {
        StringJoiner result = new StringJoiner("; ");
        String tempFieldModelValue = " ";
        if (null != entityModelList) {
            for (EntityModel entityModel : entityModelList) {
                for (FieldModel fieldModel : entityModel.getValues()) {
                    if (child.equals(fieldModel.getName())) {
                        tempFieldModelValue = String.valueOf(fieldModel.getValue());
                    }
                }
                result.add(tempFieldModelValue);
            }
        }
        return result.toString();
    }

    public static String stripHtml(String html) {
        Document descriptionDoc = Jsoup.parse(html);
        descriptionDoc.outputSettings().escapeMode(Entities.EscapeMode.base);
        descriptionDoc.outputSettings().charset(CharEncoding.US_ASCII);
        descriptionDoc.outputSettings().prettyPrint(false);
        return (null == descriptionDoc.text()) ? " " : descriptionDoc.text();
    }

    public static String ellipsisTruncate(String text, int maximumLength) {
        if (text.length() > maximumLength) {
            return text.substring(0, maximumLength) + "...";
        }
        return text;
    }

    public static String createQueryForMultipleValues(String queryParamName, String... queryParamValues) throws UnsupportedEncodingException {
        StringJoiner stringJoiner = new StringJoiner("||", "\"(", ")\"");
        String finalQuery = "";
        if (queryParamValues.length != 0) {
            for (String entity : queryParamValues) {
                stringJoiner.add(queryParamName + "='" + entity + "'");
            }
            finalQuery = stringJoiner.toString();
        }
        return finalQuery;
    }

    public static String createQueryForMultipleValues(String queryParamName, List<String> queryParamValues) throws UnsupportedEncodingException {
        return createQueryForMultipleValues(queryParamName, (String[]) queryParamValues.toArray());
    }

    public static List<FormLayout> parseJsonWithFormLayoutData(String responseJson,OctaneVersion version) {
        logger.debug("Parsing JSON response");
        List<FormLayout> entitiesFormLayout = new ArrayList<>();
        if (responseJson != null && !responseJson.isEmpty()) {
            JSONTokener tokener = new JSONTokener(responseJson);
            JSONObject jsonObj = new JSONObject(tokener);
            JSONArray data = jsonObj.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject tempJsonObj = data.getJSONObject(i);
                FormLayout formLayout = new FormLayout();
                formLayout.setFormId(Long.valueOf(tempJsonObj.getString("id")));
                formLayout.setFormName(tempJsonObj.getString("name"));
                formLayout.setEntity(Entity.getEntityType(tempJsonObj.getString("entity_type"), tempJsonObj.optString("entity_subtype")));
                formLayout.setFormLayoutSections(getFormLayoutSections(tempJsonObj.getJSONObject("body").getJSONObject("layout").getJSONArray("sections")));
                if(OctaneVersion.compare(version, OctaneVersion.Operation.LOWER_EQ,OctaneVersion.FENER_P2)){
                    formLayout.setDefault(tempJsonObj.getInt("is_default"));
                } else {
                    formLayout.setDefault(tempJsonObj.getJSONObject("body").getBoolean("isDefault"));
                }
                entitiesFormLayout.add(formLayout);
            }
        }
        logger.debug("Parsing is DONE, and the result has: " + entitiesFormLayout.size() + " elements");
        return entitiesFormLayout;
    }

    private static List<FormLayoutSection> getFormLayoutSections(JSONArray sections) {
        List<FormLayoutSection> retSections = new ArrayList<>();
        for (int i = 0; i < sections.length(); i++) {
            JSONObject tempSections = sections.getJSONObject(i);
            FormLayoutSection formLayoutSection = new FormLayoutSection();
            formLayoutSection.setSectionTitle(tempSections.getString("title"));
            formLayoutSection.setFields(getSectionFields(tempSections.getJSONArray("fields")));
            retSections.add(formLayoutSection);
        }
        return retSections;
    }

    private static List<FormField> getSectionFields(JSONArray fields) {
        List<FormField> retFields = new ArrayList<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject tempField = fields.getJSONObject(i);
            FormField formField = new FormField();
            formField.setName(tempField.getString("name"));
            formField.setSize("large");
            retFields.add(formField);
        }
        return retFields;
    }
}
