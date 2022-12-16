package com.example.address_book;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class NewContact {
    public TextField name_field;
    public TextField surname_field;
    public TextArea address_area;
    public TextField phone_field;
    public TextField fax_field;
    public TextArea extra_info_area;
    public Button save_button;
    public Button cancel_button;
    public TextField email_field;
    public TextField company_field;
    private final Connection conn = connectNow();


    /**
     * Adds a contact to the database.
     * Has the built-in check for mandatory field, which have to be filled in:
     *  -Name
     *  -Surname
     *  -Company
     * Has a safety feature built-in, which checks if the other fields are empty or not. If they are empty the error is displayed.
     * If the field is empty the NULL is sent to database, otherwise the actual input from the field or area is.
     */
    public void save_new_contact(ActionEvent event){
        if (checkRequired()) {
            try {
                String sql = "INSERT INTO addressbook VALUES (?,?,?,?,?,?,?,?,NOW())";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name_field.getText());
                ps.setString(2, surname_field.getText());
                ps.setString(3, company_field.getText());
                getArea(address_area,4,ps);
                getField(phone_field,5,ps);
                getField(phone_field,6,ps);
                getField(phone_field,7,ps);
                getArea(extra_info_area,8,ps);
                System.out.println(ps.toString());
                ps.execute();
                System.out.println("Added contact");
            } catch (SQLException | NumberFormatException throwables) {
                System.out.println("Contact not added - error");
                throwables.printStackTrace();
            }
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("WARNING - something is missing!");
            alert.setHeaderText("Required data is missing");
            alert.setContentText("""
                    All data denoted with '*' is required to be filled in!
                                        
                    Please check if these fields are not empty""");
            alert.show();
        }
    }

    /**
     * Refresh of the contact list is added at the place of opening the "new contact" window
     */
    public void quit(ActionEvent event) {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.close();

    }

    private Connection connectNow(){
        MysqlConnection mysqlConnection = new MysqlConnection();
        return mysqlConnection.getConnection();
    }

    /**
     * Populates the contact data after a contact is being clicked on.
     */
    private boolean checkRequired(){
        return !name_field.getText().isEmpty() && !surname_field.getText().isEmpty() && !company_field.getText().isEmpty();

    }

    /**
     * @param field -> field which is being used in SQL query
     * @param position -> number of element "?", which is being substituted
     * @param ps -> prepared statement's name
     * @throws SQLException
     *
     * Checks if the field that we try to use with SQL query is not empty, if it is we use "setNull " to pass NULL value.
     * Otherwise, we use setString, to pass the actual value, input in the field.
     */
    private void getField(TextField field, int position, PreparedStatement ps) throws SQLException {
        if (field.getText().isEmpty()){
            ps.setNull(position,Types.NULL);
        }
        else{ps.setString(position,field.getText());}
    }

    /**
     * @param area -> area which is being used in SQL query
     * @param position -> number of element "?", which is being substituted
     * @param ps -> prepared statement's name
     * @throws SQLException
     *
     * Checks if the area that we try to use with SQL query is not empty, if it is we use "setNull " to pass NULL value.
     * Otherwise, we use setString, to pass the actual value, input in the area.
     */
    private void getArea(TextArea area, int position, PreparedStatement ps) throws SQLException {
        if (area.getText().isEmpty()){
            ps.setNull(position, Types.NULL);
        }
        else{ps.setString(position, area.getText());}
    }
}
