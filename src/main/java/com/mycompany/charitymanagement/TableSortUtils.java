package com.mycompany.charitymanagement;

import java.time.LocalDate;
import java.util.Comparator;
import javafx.scene.control.TableColumn;

final class TableSortUtils {

    private TableSortUtils() {
    }

    static <T> void configureDateColumn(TableColumn<T, String> column) {
        column.setComparator((left, right) -> {
            LocalDate leftDate = UiFormOptions.parseDate(left);
            LocalDate rightDate = UiFormOptions.parseDate(right);
            if (leftDate != null && rightDate != null) {
                return leftDate.compareTo(rightDate);
            }
            if (leftDate != null) {
                return -1;
            }
            if (rightDate != null) {
                return 1;
            }
            return safe(left).compareToIgnoreCase(safe(right));
        });
    }

    static <T> void configureMoneyColumn(TableColumn<T, String> column) {
        column.setComparator(Comparator.comparingDouble(TableSortUtils::moneyValue));
    }

    static <T> void configureNumberColumn(TableColumn<T, String> column) {
        column.setComparator(Comparator.comparingDouble(TableSortUtils::numberValue));
    }

    private static double moneyValue(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return 0;
        }
        try {
            return FormatUtils.parseMoney(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static double numberValue(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return 0;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
