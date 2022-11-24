package myprojects.com.adressbook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    public TextField username_field;
    public PasswordField password_field;
    public Button login_button;
    public Connection conn = MySql.getConnection();


    public void login(ActionEvent event) throws SQLException, IOException {
        String sql = "SELECT EXISTS (SELECT 1 FROM credentials WHERE username = ?)";
        String sql1 = "SELECT password FROM credentials WHERE username = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1,username_field.getText());
        if (ps.getResultSet().getInt(1) == 1){
            ps.close();
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,username_field.getText());
            if (ps.getResultSet().getString(1) == password_field.getText()){
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(StartingClass.class.getResource("AddressBook.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 320, 240);
                stage.setTitle("AddressBook");
                stage.setScene(scene);
                stage.show();
            }

        }
    }
}
