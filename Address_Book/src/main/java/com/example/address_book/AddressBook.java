package com.example.address_book;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class AddressBook implements Initializable {

    public TreeView<String> contacts_tree = new TreeView<>();
    public TextField name_field;
    public TextField surname_field;
    public TextField company_field;
    public TextArea address_area;
    public TextField phone_field;
    public TextField fax_field;
    public TextField email_field;
    public TextArea extra_area;
    public Label last_updated_date;
    public MenuItem new_contact_menu;
    public MenuItem quit_menu;
    public MenuItem delete_menu;
    public MenuItem about_menu;
    public Button new_contact_button;
    public Button save_button;
    public Button delete_button;
    private final Connection conn = connectToDatabase();
    private List<String> company_list = new ArrayList<>();
    private List<List<String>> individual_contacts = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    /**
     * Deletes the currently selected item from the contacts.
     * The item can be a company and an individual contact.
     */
    public void DeleteContact(ActionEvent event) {
        TreeItem<String> item = contacts_tree.getSelectionModel().getSelectedItem();
        if (item != null){
            if (company_list.contains(item.getValue())){
                String sql = "DELETE FROM addressbook WHERE company = ?";
                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1,item.getValue());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation needed!");
                    alert.setHeaderText("You are about to delete all contacts of" + item.getValue() + "company!");
                    alert.setContentText("Are you sure you want to continue?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        ps.execute();
                        refreshTree();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if(!company_list.contains(item.getValue())){
                String sql = "DELETE FROM addressbook WHERE company = ? AND name = ? AND surname = ?";
                String[] nameAndSurname = item.getValue().split("\s");
                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1,item.getParent().getValue());
                    ps.setString(2, nameAndSurname[0]);
                    ps.setString(3, nameAndSurname[1]);
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation needed!");
                    alert.setHeaderText("You are about to delete this contact!");
                    alert.setContentText("Are you sure you want to continue?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        ps.execute();
                        refreshTree();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Opens "New contact" window, which allows adding contacts.
     */
    public void NewContact(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(AddressBook.class.getResource("NewContact.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("New contact");
            stage.show();
            stage.setOnHiding(action -> refreshTree());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ExitProgram(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Displays an "about" window with information about the program.
     */
    public void DisplayAbout(ActionEvent event) {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(AddressBookApplication.class.getResource("About.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("About...");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves any changes made to the contact data and updates the database.\n
     * There are three fields, which cannot be left empty:
     *  \n-Name
     *  \n-Surname
     *  \n-Company\n
     * Trying to update the database with these fields empty, will result in displaying an error.\n
     * If any other fields/areas will be left empty, NULL value will be passed to the database.
     */
    public void SaveContact(ActionEvent event) {
        if(checkRequired()) {
            TreeItem<String> item = contacts_tree.getSelectionModel().getSelectedItem();
            String sql = "UPDATE addressbook name = ?, surname = ?, company = ?, address = ?, phone_number = ?, fax_number = ?" +
                    "email = ?, ex_info = ?, last_updated = NOW() WHERE company = ? AND name = ? AND surname = ?";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                getField(name_field,1,ps);
                getField(surname_field,2,ps);
                getField(company_field,3,ps);
                getArea(address_area,4,ps);
                getField(phone_field,5,ps);
                getField(fax_field,6,ps);
                getField(email_field,7,ps);
                getArea(extra_area,8,ps);
                ps.setString(9, item.getParent().getValue());
                ps.setString(10, item.getValue().split("\s")[0]);
                ps.setString(11, item.getValue().split("\s")[1]);
                ps.execute();
                System.out.println("Updated");
            } catch (SQLException throwables) {
                System.out.println("Not updated - error:");
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

    private Connection connectToDatabase() {
        MysqlConnection MySql = new MysqlConnection();
        System.out.println("connected");
        return MySql.getConnection();
    }

    private void populateCompany() {
        company_list.clear();
        String sql = "SELECT DISTINCT company FROM addressbook";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                company_list.add(rs.getString(1));
            }
            company_list.sort(Comparator.naturalOrder());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void populateIndividualContacts(List<String> company_list) {
        individual_contacts.clear();
        String sql = "SELECT name, surname FROM addressbook WHERE company = ?";
        try {
            for (String s : company_list) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, s);
                ResultSet rs = ps.executeQuery();
                List<String> companys_contacts = new ArrayList<>();
                while (rs.next()) {
                    companys_contacts.add(rs.getString(1) + " " + rs.getString(2));
                    companys_contacts.sort(Comparator.naturalOrder());
                }
                individual_contacts.add(companys_contacts);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the tree - "contact list" from two lists, which are populated by methods:
     *  \n-populateCompany()
     *  \n-populateIndividualContacts()
     */
    private void createTheTree() {
        TreeItem<String> rootItem = new TreeItem<>("Contacts");
        for (int i = 0; i < individual_contacts.size(); i++) {
            TreeItem<String> company_item = new TreeItem<>(company_list.get(i));
            rootItem.getChildren().add(company_item);
            for (String s : individual_contacts.get(i)) {
                TreeItem<String> contact = new TreeItem<>(s);
                company_item.getChildren().add(contact);
                System.out.println(s);
            }
        }
        contacts_tree.setRoot(rootItem);
        contacts_tree.setShowRoot(false);

    }


    /**
     * "Refreshes" the contact list, displayed as a tree view.
     * Used as initialization method and also after any change or addition to the contacts list, so that the list is up-to-date
     */
    private void refreshTree(){
        populateCompany();
        populateIndividualContacts(company_list);
        createTheTree();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTree();
    }

    /**
     * Populates the contact data after a contact is being clicked on.
     */
    public void populateCompanyList(MouseEvent mouseEvent) {
        String sql = "SELECT * FROM addressbook WHERE company = ? AND name = ? AND surname = ?";
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            TreeItem<String> item = contacts_tree.getSelectionModel().getSelectedItem();

            if (item != null && !company_list.contains(item.getValue())) {
                String[] nameAndSurname = item.getValue().split("\s");
                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, contacts_tree.getSelectionModel().getSelectedItem().getParent().getValue());
                    ps.setString(2, nameAndSurname[0]);
                    ps.setString(3, nameAndSurname[1]);
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) {
                        name_field.setText(nameAndSurname[0]);
                        surname_field.setText(nameAndSurname[1]);
                        company_field.setText(rs.getString(3));
                        address_area.setText(rs.getString(4));
                        phone_field.setText(rs.getString(5));
                        fax_field.setText(rs.getString(6));
                        email_field.setText(rs.getString(7));
                        extra_area.setText(rs.getString(8));
                        java.sql.Timestamp db_date = rs.getTimestamp(9);
                        LocalDateTime datetime = db_date.toLocalDateTime();
                        String updated = datetime.format(formatter);
                        last_updated_date.setText(updated);
                    }
                } catch (SQLException throwables) {
                    System.out.println("Something went wrong");
                    throwables.printStackTrace();
                }

            }

        }

    }

    /**
     * @param field -> field which is being used in SQL query
     * @param position -> number of element "?", which is being substituted
     * @param ps -> prepared statement's name
     * @throws SQLException
     *
     * Checks if the field that we try to use with SQL query is not empty, if it is we use "setNull " to pass NULL value.\n
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
     * Checks if the area that we try to use with SQL query is not empty, if it is we use "setNull " to pass NULL value.
     * Otherwise, we use setString, to pass the actual value, input in the area.
     */
    private void getArea(TextArea area, int position, PreparedStatement ps) throws SQLException {
        if (area.getText().isEmpty()){
            ps.setNull(position, Types.NULL);
        }
        else{ps.setString(position, area.getText());}
    }

    /**
     * Checks if all the required field are filled.
     */
    private boolean checkRequired(){
        return !name_field.getText().isEmpty() && !surname_field.getText().isEmpty() && !company_field.getText().isEmpty();
    }
}
