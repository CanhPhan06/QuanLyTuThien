package com.mycompany.charitymanagement;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class CrudDialogUtils {

    private CrudDialogUtils() {
    }

    public static String[] showForm(String title, String[] labels, String[] values) {
        Scene scene = App.getScene();
        if (scene == null || !Platform.isFxApplicationThread()) {
            return null;
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        titleLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));
        grid.getStyleClass().add("form-card");

        Control[] fields = new Control[labels.length];
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.getStyleClass().add("muted-text");

            Control field = createInput(labels[i], valueAt(values, i));
            field.getStyleClass().addAll("input-field", "wide-input-field");
            field.setMaxWidth(Double.MAX_VALUE);
            fields[i] = field;

            grid.add(label, 0, i);
            grid.add(field, 1, i);
            GridPane.setHgrow(field, Priority.ALWAYS);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(Math.min(520, 58 * labels.length + 45));
        scrollPane.getStyleClass().add("content-scroll");

        Button saveButton = new Button("Lưu");
        saveButton.getStyleClass().add("primary-button");
        Button cancelButton = new Button("Hủy");
        cancelButton.getStyleClass().add("quick-button");

        HBox actions = new HBox(12, cancelButton, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, titleLabel, scrollPane, actions);
        card.getStyleClass().add("detail-card");
        card.setMaxWidth(720);
        card.setMaxHeight(620);

        StackPane overlay = DetailDialogUtils.showCard(scene, card);
        if (overlay == null) {
            return null;
        }

        saveButton.setOnAction(event -> {
            String[] result = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                result[i] = readValue(fields[i]);
            }
            DetailDialogUtils.closeOverlay(overlay);
            Platform.exitNestedEventLoop(overlay, result);
        });
        cancelButton.setOnAction(event -> {
            DetailDialogUtils.closeOverlay(overlay);
            Platform.exitNestedEventLoop(overlay, null);
        });

        Platform.runLater(fields.length > 0 ? fields[0]::requestFocus : card::requestFocus);
        return (String[]) Platform.enterNestedEventLoop(overlay);
    }

    private static Control createInput(String label, String value) {
        if (isDateLabel(label)) {
            DatePicker picker = new DatePicker(UiFormOptions.parseDate(value));
            UiFormOptions.configureDatePicker(picker);
            picker.setMaxWidth(Double.MAX_VALUE);
            return picker;
        }

        String[] choices = choicesFor(label);
        if (choices.length > 0) {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll(choices);
            comboBox.setValue(value == null || value.isEmpty() ? choices[0] : value);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            return comboBox;
        }

        TextField field = new TextField(value);
        field.setPromptText(label);
        return field;
    }

    private static String[] choicesFor(String label) {
        String normalized = label.toLowerCase();
        if (isLocationChoiceLabel(normalized)) {
            return UiFormOptions.provinceOptions().toArray(String[]::new);
        }
        if (normalized.contains("tài khoản tnv")) {
            return AppData.getAccounts().stream()
                    .filter(UserAccount::isVolunteer)
                    .map(account -> account.getUsername() + " - " + account.getDisplayName())
                    .toArray(String[]::new);
        }
        if (normalized.contains("nhóm nội dung")) {
            return new String[]{"Tin tức", "Bình luận", "Thông báo", "Nhật ký hệ thống", "Tham số"};
        }
        if (normalized.contains("đối tượng liên quan") || normalized.contains("liên quan đến")) {
            java.util.List<String> options = new java.util.ArrayList<>();
            AppData.getActivities().forEach(activity
                    -> options.add(activity.getMaChienDich() + " - " + activity.getTenChienDich()));
            AppData.getAccounts().forEach(account
                    -> options.add(account.getUsername() + " - " + account.getDisplayName()));
            return options.toArray(String[]::new);
        }
        if (normalized.contains("trạng thái hiển thị")) {
            return new String[]{"Bản nháp", "Đã đăng", "Hiển thị", "Ẩn", "Chưa đọc", "Đã ghi", "Đang dùng"};
        }
        if (normalized.contains("trạng thái chiến dịch")) {
            return new String[]{"Chờ duyệt", "Đang xét", "Đã duyệt", "Đang thực hiện", "Hoàn thành", "Đã hủy"};
        }
        if (normalized.contains("trạng thái duyệt")) {
            return new String[]{"Chờ duyệt", "Đang xét", "Đã duyệt", "Từ chối"};
        }
        if (normalized.contains("trạng thái")) {
            return new String[]{"Chờ duyệt", "Đang xét", "Chờ xác nhận", "Đã duyệt", "Có mặt", "Đã xác nhận", "Từ chối"};
        }
        if (normalized.contains("chiến dịch")) {
            return AppData.getActivities().stream()
                    .map(activity -> activity.getMaChienDich() + " - " + activity.getTenChienDich())
                    .toArray(String[]::new);
        }
        if (normalized.contains("hình thức")) {
            return new String[]{"Tiền", "Vật phẩm", "Vật tư", "Vật dụng", "Tài trợ tiền", "Tài trợ vật phẩm"};
        }
        if (normalized.contains("trường")) {
            return new String[]{"UIT", "UEL", "HCMUS", "HCMUT", "HCMIU", "UHS", "HCMUSSH"};
        }
        if (normalized.contains("khoa")) {
            return new String[]{
                "Khoa Công nghệ phần mềm", "Khoa Hệ thống thông tin", "Khoa Khoa học máy tính",
                "Khoa Mạng máy tính và truyền thông", "Khoa Kinh tế", "Khoa Luật kinh tế",
                "Khoa Toán - Tin học", "Khoa Công nghệ thông tin", "Khoa Kỹ thuật xây dựng",
                "Khoa Kỹ thuật điện - điện tử", "Khoa Quản trị kinh doanh", "Khoa Y",
                "Khoa Quan hệ quốc tế", "Khoa Báo chí và truyền thông"
            };
        }
        if (normalized.contains("lĩnh vực")) {
            return new String[]{"Giáo dục", "Y tế", "Cộng đồng", "Vật phẩm", "Tài chính", "Trẻ em", "Xã hội"};
        }
        return new String[0];
    }

    private static String readValue(Control control) {
        if (control instanceof TextField) {
            String value = ((TextField) control).getText();
            return value == null ? "" : value.trim();
        }
        if (control instanceof ComboBox<?>) {
            Object value = ((ComboBox<?>) control).getValue();
            return value == null ? "" : String.valueOf(value).trim();
        }
        if (control instanceof DatePicker) {
            return UiFormOptions.formatDate(((DatePicker) control).getValue());
        }
        return "";
    }

    private static String valueAt(String[] values, int index) {
        return values != null && index < values.length && values[index] != null ? values[index] : "";
    }

    private static boolean isDateLabel(String label) {
        return label != null && label.toLowerCase().contains("ngày");
    }

    private static boolean isLocationChoiceLabel(String normalizedLabel) {
        if (normalizedLabel == null) {
            return false;
        }
        return normalizedLabel.contains("địa điểm")
                || normalizedLabel.contains("địa chỉ")
                || normalizedLabel.contains("tỉnh")
                || normalizedLabel.contains("thành phố")
                || normalizedLabel.contains("khu vực");
    }
}
