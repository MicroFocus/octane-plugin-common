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

package com.hpe.adm.octane.ideplugins.services.util;

public class TextUtil {

    private enum EscapeEnum {
        DOUBLE_QUOTE("\"", "\\\""), 
        BACKSLASH("\\", "\\\\");

        private String wrong;
        private String correct;

        EscapeEnum(String wrong, String correct) {
            this.wrong = wrong;
            this.correct = correct;
        }

        public String getWrong() {
            return wrong;
        }

        public String getCorrect() {
            return correct;
        }
    }

    private TextUtil() {
    }

    /**
     * This method was called to escape special characters from a text (e.g.
     * when searching for text having \ or ` will make the REST call crash) <br>
     * - a split on the text was required because the query failed mostly when
     * the special character was isolated (e.g. " texttext text {specialChar}
     * text text") <br>
     * - an inner enum class was used for this method as it is only used for
     * char escaping; it was not designed for public usage. <br>
     *
     * @param text
     *            input text to be escaped
     * @return String text with escaped special characters
     */
    public static String escapeText(String text) {
        String result = text;
        String[] textMembers = text.split("");
        boolean flagForDoubleQuote = false;

        for (String member : textMembers) {
            for (EscapeEnum escapeEnum : EscapeEnum.values()) {
                if (member.equals(escapeEnum.getWrong()) && !flagForDoubleQuote) {
                    result = result.replace(member, escapeEnum.getCorrect());
                    if (member.equals("\"")) {
                        flagForDoubleQuote = true;
                    }
                    break;
                }
            }
        }

        return result;
    }
}
