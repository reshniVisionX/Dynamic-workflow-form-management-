import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;  
    private Connection connection;

    private static final String URL = "jdbc:postgresql://localhost:4040/postgres";  
    private static final String USER = "postgres";  
    private static final String PASSWORD = "root1"; 

    private DBConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Connection to the database was successful!");
    }

    public static DBConnection getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new DBConnection();
        } else if (instance.getConnection().isClosed()) { 
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
