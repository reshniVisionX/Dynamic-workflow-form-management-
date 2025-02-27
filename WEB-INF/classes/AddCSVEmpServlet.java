import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.http.Part;
import java.io.*;
import java.sql.*;
import com.opencsv.CSVReader;
import java.math.BigDecimal;
import org.postgresql.util.PSQLException;
import org.mindrot.jbcrypt.BCrypt;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import jakarta.servlet.annotation.MultipartConfig;

@MultipartConfig
public class AddCSVEmpServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("csvFile"); 
        if (filePart == null || filePart.getSize() == 0) {
			System.out.println("file is null");
            response.getWriter().write("No file uploaded or file is empty.");
            return;
		}
		File tempDir = new File("S:\\Zoho\\TempUploads");
try {
    if (!tempDir.exists()) {
        if (tempDir.mkdirs()) {
            System.out.println("Directory created: " + tempDir.getAbsolutePath());
        } else {
            System.out.println("Failed to create directory: " + tempDir.getAbsolutePath());
        }
    }
    File tempFile = File.createTempFile("uploaded-", ".csv", tempDir);
    System.out.println("Temporary file created at: " + tempFile.getAbsolutePath());
} catch (IOException e) {
    System.out.println("Error creating temporary file: " + e.getMessage());
    e.printStackTrace();
}
File tempFile = File.createTempFile("uploaded-", ".csv", tempDir);
System.out.println("Temporary file created at: " + tempFile.getAbsolutePath());
        try (InputStream inputStream = filePart.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        try (Connection connection = DBConnection.getInstance().getConnection();
             CSVReader csvReader = new CSVReader(new FileReader(tempFile))) {

            String[] nextLine;
			StringBuilder sb = new StringBuilder();
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine.length < 8) {
                    System.out.println("Skipping row due to insufficient columns.");
					sb.append("Skipping row due to insufficient columns. ");
                    continue;
                }

                String userName = nextLine[0];
                String userEmail = nextLine[1];
                String userPassword = nextLine[2];
                String contactStr = nextLine[3];
                String dobStr = nextLine[4];
                String jobRole = nextLine[5];
                String salaryStr = nextLine[6];
                String address = nextLine[7];

                if (userName != "" && userEmail != "" && userPassword != "" && contactStr != "" && dobStr != "" && jobRole != "" && salaryStr != "" && address != "") {

                    if (!isValidEmail(userEmail)) {
                        System.out.println("Skipping row due to invalid email: " + userEmail);
						sb.append("Email already exist : " + userEmail+". ");
                        continue;
                    }
                    if (!isValidPhoneNumber(contactStr)) {
                        System.out.println("Skipping row due to invalid contact number: " + contactStr);
						sb.append("Invalid contact number: " + contactStr+". ");
                        continue;
                    }

                    long contact = 0;
                    try {
                        contact = Long.parseLong(contactStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping row due to invalid contact format: " + contactStr);
						sb.append("Invalid contact format: " + contactStr+". ");
                        continue;
                    }

                    BigDecimal salary = null;
                    try {
                        salary = new BigDecimal(salaryStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping row due to invalid salary format: " + salaryStr);
						sb.append("Invalid salary format: " + contactStr+".");
                        continue;
                    }

                    String hashedPassword = BCrypt.hashpw(userPassword, BCrypt.gensalt());

                    java.sql.Date dob = null;
                    try {
                        dob = validateAndParseDate(dobStr);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Skipping row due to invalid date format: " + dobStr);
						sb.append("Invalid date format: " + dobStr+". ");
                        continue;
                    }

                    int userId = insertUser(connection, userName, userEmail, hashedPassword);
                    if (userId == -1) {
                        System.out.println("Skipping row due to duplicate user email: " + userEmail);
						sb.append("Duplicate email: " + userEmail+". ");
                        continue;
                    }
                    insertUserData(connection, userId, contact, dob, jobRole, salary, address);
                } else {
                    System.out.println("Skipping row due to missing required fields.");
					sb.append("Skipping row due to missing required fields. ");
                }
            }
                if (sb.length() > 0) {
                    response.setContentType("text/html");
                    response.getWriter().write("<script>");
					response.getWriter().write("alert('Issues during file processing:\\n" + sb.toString().replace("\n", "\\n") + "');");
                    response.getWriter().write("window.location.href = 'manageEmployee.jsp';");
                    response.getWriter().write("</script>");
                } else {
                    response.setContentType("text/html");
                    response.getWriter().write("<script>");
                    response.getWriter().write("alert('File processed successfully.');");
                    response.getWriter().write("window.location.href = 'manageEmployee.jsp';");
                    response.getWriter().write("</script>");
                }

        } catch (SQLException | ClassNotFoundException | CsvValidationException e) {
            response.getWriter().write("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\d{10}$");
    }

    public java.sql.Date validateAndParseDate(String dobStr) {
        String[] dateFormats = {
            "yyyy-MM-dd",
            "dd-MM-yyyy"
        };
        
        for (String format : dateFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate localDate = LocalDate.parse(dobStr, formatter);
                return java.sql.Date.valueOf(localDate);  
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        throw new IllegalArgumentException("Invalid date format.");
    }

    private int insertUser(Connection connection, String userName, String userEmail, String userPassword) throws SQLException {
        String sql = "INSERT INTO users (username, user_email, user_password, role_id, status) VALUES (?, ?, ?, ?, ?) RETURNING user_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userName);
            stmt.setString(2, userEmail);
            stmt.setString(3, userPassword);
            stmt.setInt(4, 3);
            stmt.setString(5, "active");

            try {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            } catch (PSQLException e) {
                if (e.getMessage().contains("duplicate key value violates unique constraint")) {
					System.out.println("Skipping due to duplicate entry");
                    return -1;
                }
                throw e;
            }
        }
        return -1;
    }

    private void insertUserData(Connection connection, int userId, long contact, java.sql.Date dob, String jobRole, BigDecimal salary, String address) throws SQLException {
        String sql = "INSERT INTO userData (user_id, contact, dob, job_role, salary, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setLong(2, contact);
            stmt.setDate(3, dob);
            stmt.setString(4, jobRole);
            stmt.setBigDecimal(5, salary);
            stmt.setString(6, address);

            stmt.executeUpdate();
        }
    }
} 