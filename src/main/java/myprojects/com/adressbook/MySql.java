package myprojects.com.adressbook;
import java.sql.*;

public class MySql {

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/address_book", "test", "Test1234");
        } catch (Exception e) {
            e.printStackTrace();
        }
            return connection;
        }
}
