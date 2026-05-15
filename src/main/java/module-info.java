module com.mycompany.charitymanagement {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.charitymanagement to javafx.fxml;
    exports com.mycompany.charitymanagement;
}
