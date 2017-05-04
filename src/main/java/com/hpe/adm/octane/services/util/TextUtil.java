package com.hpe.adm.octane.services.util;

/**
 * Created by olteanm on 5/3/2017.
 */
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
