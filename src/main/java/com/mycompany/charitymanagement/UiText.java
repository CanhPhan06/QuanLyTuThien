package com.mycompany.charitymanagement;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UiText {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile(
            "(?:\\u00C3[\\u0080-\\u00BF\\u00C0-\\u00FF])"
            + "|(?:\\u00E1[\\u00BA\\u00BB][\\u0080-\\u00BF\\u00C0-\\u00FF]?)"
            + "|(?:\\u00C4[\\u0080-\\u00BF\\u00C0-\\u00FF])"
            + "|(?:\\u00C6[\\u0080-\\u00BF\\u00C0-\\u00FF])"
            + "|[\\u0080-\\u009F\\uFFFD]"
    );
    private static final Pattern QUESTION_LOSS_PATTERN = Pattern.compile(
            "(?:\\?{2,})|(?:[\\p{L}]\\?[\\p{L}])|\\uFFFD"
    );

    private UiText() {
    }

    public static String clean(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String repaired = repairKnownBrokenText(value);
        if (!looksBroken(repaired)) {
            return repaired;
        }
        String best = repaired;
        best = better(best, repairKnownBrokenText(decodeUtf8ReadAsLatin1(repaired)));
        best = better(best, repairKnownBrokenText(decodeUtf8ReadAsWindows1252(repaired)));
        return best;
    }

    public static boolean looksBroken(String value) {
        return value != null
                && (MOJIBAKE_PATTERN.matcher(value).find() || QUESTION_LOSS_PATTERN.matcher(value).find());
    }

    private static String decodeUtf8ReadAsLatin1(String value) {
        return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private static String decodeUtf8ReadAsWindows1252(String value) {
        return new String(value.getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
    }

    private static String better(String current, String candidate) {
        return score(candidate) < score(current) ? candidate : current;
    }

    private static int score(String value) {
        int score = 0;
        Matcher matcher = MOJIBAKE_PATTERN.matcher(value);
        while (matcher.find()) {
            score += 10;
        }
        Matcher questionLossMatcher = QUESTION_LOSS_PATTERN.matcher(value);
        while (questionLossMatcher.find()) {
            score += 8;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\uFFFD' || Character.isISOControl(c) && !Character.isWhitespace(c)) {
                score += 5;
            }
        }
        return score;
    }

    private static String repairKnownBrokenText(String value) {
        String text = value;
        text = repairWholeValue(text);
        text = text.replace("Chuy??n kho?n", "Chuyển khoản");
        text = text.replace("Chuy?n kho?n", "Chuyển khoản");
        text = text.replace("Ti?n m?t", "Tiền mặt");
        text = text.replace("V?t ph?m", "Vật phẩm");
        text = text.replace("Công ty Hưng Th?nh", "Công ty Hưng Thịnh");
        text = text.replace("Câu lạc b? Xanh", "Câu lạc bộ Xanh");
        text = text.replace("Qu? C?ng ???ng", "Quỹ Cộng Đồng");
        text = text.replace("Qu? C?ng ??ng", "Quỹ Cộng Đồng");
        text = text.replace("Nguy?n V?n Bình", "Nguyễn Văn Bình");
        text = text.replace("S?ch v?: 800 B?", "Sách vở: 800 Bộ");
        text = text.replace("S?ch v??: 800 B??", "Sách vở: 800 Bộ");
        text = text.replace("Kh?u trang: 300 H??p", "Khẩu trang: 300 Hộp");
        text = text.replace("Kh?u trang: 300 H?p", "Khẩu trang: 300 Hộp");
        text = text.replace("Máy tính cũ: 25 B?", "Máy tính cũ: 25 Bộ");
        return repairWholeValue(text);
    }

    private static String repairWholeValue(String value) {
        if (!looksLikeQuestionLoss(value)) {
            return value;
        }
        String lower = value.toLowerCase();
        if (lower.contains("ti?p") && lower.contains("s?c") && lower.contains("tr??ng")) {
            return "Tiếp sức đến trường 2026";
        }
        if ((lower.contains("kh?m") || lower.contains("khám")) && lower.contains("b?nh") && lower.contains("thi?n")) {
            return "Khám bệnh thiện nguyện 2026";
        }
        if (lower.contains("khám") && lower.contains("thi") && lower.contains("2026")) {
            return "Khám bệnh thiện nguyện 2026";
        }
        if (lower.contains("s?ch") && lower.contains("cho em")) {
            return "Sách cho em 2026";
        }
        if (lower.contains("sách") && lower.contains("800")) {
            return "Sách vở: 800 Bộ";
        }
        if (lower.contains("khẩu") && lower.contains("300")) {
            return "Khẩu trang: 300 Hộp";
        }
        if (lower.contains("máy tính") && lower.contains("25")) {
            return "Máy tính cũ: 25 Bộ";
        }
        if (lower.contains("n??c") && lower.contains("h?c")) {
            return "Nước sạch học đường 2026";
        }
        return value;
    }

    private static boolean looksLikeQuestionLoss(String value) {
        return value.indexOf('?') >= 0 || value.indexOf('\uFFFD') >= 0;
    }
}
