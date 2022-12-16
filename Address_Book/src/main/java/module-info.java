module com.example.address_book {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.example.address_book to javafx.fxml;
    exports com.example.address_book;
}