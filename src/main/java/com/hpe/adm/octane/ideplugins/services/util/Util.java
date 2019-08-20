/*
 * Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
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

import com.hpe.adm.nga.sdk.model.*;
import org.apache.commons.lang.CharEncoding;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

@SuppressWarnings("rawtypes")
public class Util {
    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    /**
     * This method is for displaying in the UI only
     *
     * @param fieldModel from an {@link EntityModel}
     * @return string value of the field
     */
    public static String getUiDataFromModel(FieldModel fieldModel) {
        String referenceEntityField = "name"; //default

        //Exception for all user fields
        if (fieldModel instanceof ReferenceFieldModel && fieldModel.getValue() != null) {
            EntityModel entity = (EntityModel) fieldModel.getValue();
            if (entity.getValue("full_name") != null) {
                referenceEntityField = "full_name";
            }
        }

        return getUiDataFromModel(fieldModel, referenceEntityField);
    }

    /**
     * This method is for displaying in the UI only
     *
     * @param fieldModel     fieldModel from an {@link EntityModel}
     * @param neededProperty can check {@link ReferenceFieldModel} and
     *                       {@link MultiReferenceFieldModel} for property to use
     * @return string value of the field
     */
    public static String getUiDataFromModel(FieldModel fieldModel, String neededProperty) {
        String result = "";
        if (null != fieldModel) {
            FieldModel tempFieldModel;

            if (fieldModel instanceof ReferenceFieldModel) {

                tempFieldModel = getValueOfChild((EntityModel) fieldModel.getValue(), neededProperty);
                if (null != tempFieldModel) {
                    result = String.valueOf(tempFieldModel.getValue());
                }

            } else if (fieldModel instanceof MultiReferenceFieldModel) {
                result = getValueOfChildren((Collection<EntityModel>) fieldModel.getValue(), neededProperty);

            } else if (fieldModel instanceof EmptyFieldModel) {
                return " ";

            } else {
                // In case of dates, we need to convert to local timezone
                if (fieldModel.getValue() instanceof ZonedDateTime) {
                    ZonedDateTime serverdateTime = (ZonedDateTime) fieldModel.getValue();
                    ZonedDateTime localTime = serverdateTime.withZoneSameInstant(ZoneId.systemDefault());
                    result = localTime.toLocalDateTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
                } else {
                    result = String.valueOf(fieldModel.getValue());
                }
            }
        }

        // if the string happens to be valid json, strip it down to look like a
        // normal string
        try {
            new JSONObject(result);
            // in case it is json, make it pretty!
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
        return getNonNullValue(commentModel, "owner_work_item", "owner_test", "owner_run", "owner_requirement");
    }

    private static String getValueOfChildren(Collection<EntityModel> entityModelList, String child) {
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

    public static String createQueryForMultipleValues(String queryParamName, String... queryParamValues) {
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
        return createQueryForMultipleValues(queryParamName, queryParamValues.toArray(new String[]{}));
    }

}
