package com.mycompany.charitymanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

final class UiFormOptions {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd/MM/yy");

    private UiFormOptions() {
    }

    static ObservableList<String> provinceOptions() {
        return FXCollections.observableArrayList(
                "Hà Nội", "TP.HCM", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
                "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
                "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
                "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông",
                "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang",
                "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình",
                "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
                "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định",
                "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên",
                "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị",
                "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên",
                "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang",
                "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
        );
    }

    static void configureDatePicker(DatePicker picker) {
        picker.setEditable(false);
        picker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return formatDate(date);
            }

            @Override
            public LocalDate fromString(String value) {
                return parseDate(value);
            }
        });
        picker.setPromptText("dd/MM/yyyy");
    }

    static String formatDate(LocalDate date) {
        return date == null ? "" : DISPLAY_DATE.format(date);
    }

    static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{DISPLAY_DATE, SHORT_DATE, DateTimeFormatter.ISO_LOCAL_DATE}) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ex) {
                // Try the next supported legacy format.
            }
        }
        return null;
    }
}
