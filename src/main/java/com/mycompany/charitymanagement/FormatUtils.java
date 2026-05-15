package com.mycompany.charitymanagement;

import java.text.DecimalFormat;

public final class FormatUtils {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private FormatUtils() {
    }

    public static String money(double amount) {
        return MONEY_FORMAT.format(amount) + " VNĐ";
    }

    public static double parseMoney(String value) {
        String digitsOnly = value == null ? "" : value.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            throw new NumberFormatException("Giá trị tiền đang trống");
        }
        return Double.parseDouble(digitsOnly);
    }
}
