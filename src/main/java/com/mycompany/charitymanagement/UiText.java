package com.mycompany.charitymanagement;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UiText {

    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile(
            "(?:\\u00C3[\\u0080-\\u00BF\\u00C0-\\u00FF])"
            + "|(?:\\u00E1[\\u00BA\\u00BB][\\u0080-\\u00BF\\u00C0-\\u00FF]?)"
            + "|(?:\\u00C4[\\u0080-\\u00BF\\u00C0-\\u00FF])"
            + "|(?:\\u00C6[\\u0080-\\u00BF\\u00C0-\\u00FF])"
            + "|[\\u0080-\\u009F\\uFFFD]"
    );

    private UiText() {
    }

    public static String clean(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (!looksBroken(value)) {
            return value;
        }
        String fixed = decodeUtf8ReadAsLatin1(value);
        return score(fixed) < score(value) ? fixed : value;
    }

    public static boolean looksBroken(String value) {
        return value != null && MOJIBAKE_PATTERN.matcher(value).find();
    }

    private static String decodeUtf8ReadAsLatin1(String value) {
        return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private static int score(String value) {
        int score = 0;
        Matcher matcher = MOJIBAKE_PATTERN.matcher(value);
        while (matcher.find()) {
            score += 10;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\uFFFD' || Character.isISOControl(c) && !Character.isWhitespace(c)) {
                score += 5;
            }
        }
        return score;
    }
}
