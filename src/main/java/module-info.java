module com.mycompany.charitymanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox;

    opens com.mycompany.charitymanagement to javafx.fxml;
    exports com.mycompany.charitymanagement;
}
