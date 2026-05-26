module com.mycompany.charitymanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.pdfbox;
    requires com.oracle.database.jdbc;

    opens com.mycompany.charitymanagement to javafx.fxml;
    exports com.mycompany.charitymanagement;
}
