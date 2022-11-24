module myprojects.com.adressbook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens myprojects.com.adressbook to javafx.fxml;
    exports myprojects.com.adressbook;
}