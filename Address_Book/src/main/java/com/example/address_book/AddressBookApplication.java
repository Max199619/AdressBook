package com.example.address_book;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AddressBookApplication extends Application {

    @Override
    public void start(Stage stage) {
    try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("AddressBook.fxml")));
            Scene scene = new Scene(root);
            stage.setTitle("Address Book");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public static void main(String[] args) {
        launch(args);
    }
}