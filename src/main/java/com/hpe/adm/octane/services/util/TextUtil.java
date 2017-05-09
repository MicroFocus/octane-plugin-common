package com.hpe.adm.octane.services.util;

public class TextUtil {

    private enum EscapeEnum {
        SIMPLE_QUOTE("\'", "\\\'"),
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
     * This method was called to escape special characters from a text (e.g. when searching for text having \ or ` will make the REST call crash)
     *      - a split on the text was required because the query failed mostly when the special character was isolated (e.g. " texttext text <<specialChar>> text text")
     *      - an inner enum class was used for this method as it is only used for char escaping; it was not designed for public usage.
     *
     * @param text
     * @return String text with escaped special characters
     */
    public static String escapeText(String text) {
        String result = text;

        String[] textMembers = text.split(" ");

        for (final String member : textMembers) {
            for (EscapeEnum escapeEnum : EscapeEnum.values()) {
                if (member.equals(escapeEnum.getWrong())) {
                    result = result.replace(member, escapeEnum.getCorrect());
                    break;
                }
            }
        }

        return result;
    }
}
