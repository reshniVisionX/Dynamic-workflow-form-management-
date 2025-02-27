import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.json.JSONObject;

public class InsertServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); 
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String username = request.getParameter("username");
        String userEmail = request.getParameter("user_email");
        String userPassword = request.getParameter("user_password");
        String contact = request.getParameter("contact");
        String dob = request.getParameter("dob");
        String jobRole = request.getParameter("job_role");
        String salary = request.getParameter("salary");
        String address = request.getParameter("address");
        String roleIdStr = request.getParameter("role_id");

        StringBuilder errors = new StringBuilder();

        if (username == null || username.trim().isEmpty()) {
            errors.append("Username is required.\n");
        }
        if (userEmail == null || userEmail.trim().isEmpty() || !Pattern.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$", userEmail)) {
            errors.append("Invalid email format.\n");
        }
        if (userPassword == null || userPassword.length() < 6) {
            errors.append("Password must be at least 6 characters long.\n");
        }
        if (contact == null || !Pattern.matches("\\d{10}", contact)) {
            errors.append("Invalid contact number. Must be at least 10 digits.\n");
        }
        if (dob == null || dob.trim().isEmpty()) {
            errors.append("Date of birth is required.\n");
        }
        if (jobRole == null || jobRole.trim().isEmpty()) {
            errors.append("Job role is required.\n");
        }
        if (salary == null || salary.trim().isEmpty()) {
            errors.append("Salary is required.\n");
        }
        if (address == null || address.trim().isEmpty()) {
            errors.append("Address is required.\n");
        }

        int roleId = 0;
        try {
            roleId = Integer.parseInt(roleIdStr);
            if (roleId != 2 && roleId != 3) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid role ID. Role ID should be either 2 or 3.");
                out.write(jsonResponse.toString());
                return;
            }
        } catch (NumberFormatException e) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid role ID format.");
            out.write(jsonResponse.toString());
            return;
        }

        if (errors.length() > 0) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", errors.toString().replace("\n", " "));
            out.write(jsonResponse.toString());
            return;
        }

        Connection connection = null;
        PreparedStatement checkEmailStmt = null;
        PreparedStatement userStmt = null;
        PreparedStatement userDataStmt = null;
        PreparedStatement fetchUserIdStmt = null;

        try {
            connection = DBConnection.getInstance().getConnection();

            String checkEmailQuery = "SELECT user_id FROM users WHERE user_email = ?";
            checkEmailStmt = connection.prepareStatement(checkEmailQuery);
            checkEmailStmt.setString(1, userEmail);
            ResultSet rs = checkEmailStmt.executeQuery();
            if (rs.next()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Email already exists. Please use a different email.");
                out.write(jsonResponse.toString());
                return;
            }

            connection.setAutoCommit(false);

            String hashedPassword = BCrypt.hashpw(userPassword, BCrypt.gensalt());
            String userInsertQuery = "INSERT INTO users (username, user_email, user_password, role_id, status) VALUES (?, ?, ?, ?, ?)";
            userStmt = connection.prepareStatement(userInsertQuery);
            userStmt.setString(1, username);
            userStmt.setString(2, userEmail);
            userStmt.setString(3, hashedPassword);
            userStmt.setInt(4, roleId);
            userStmt.setString(5, "active");
            userStmt.executeUpdate();

            String fetchUserIdQuery = "SELECT user_id FROM users WHERE user_email = ?";
            fetchUserIdStmt = connection.prepareStatement(fetchUserIdQuery);
            fetchUserIdStmt.setString(1, userEmail);
            ResultSet generatedKeysResult = fetchUserIdStmt.executeQuery();
            if (generatedKeysResult.next()) {
                int userId = generatedKeysResult.getInt("user_id");

                String userDataInsertQuery = "INSERT INTO userData (user_id, contact, dob, job_role, salary, address) VALUES (?, ?, ?, ?, ?, ?)";
                userDataStmt = connection.prepareStatement(userDataInsertQuery);
                userDataStmt.setInt(1, userId);
                userDataStmt.setLong(2, Long.parseLong(contact));
                userDataStmt.setDate(3, java.sql.Date.valueOf(dob));
                userDataStmt.setString(4, jobRole);
                userDataStmt.setBigDecimal(5, new BigDecimal(salary));
                userDataStmt.setString(6, address);
                userDataStmt.executeUpdate();
            } else {
                throw new SQLException("Failed to fetch user ID after insertion.");
            }

            connection.commit();

            jsonResponse.put("status", "success");
            jsonResponse.put("message", "Data inserted successfully!");
            out.write(jsonResponse.toString());
        } catch (ClassNotFoundException | SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database error: " + e.getMessage());
            out.write(jsonResponse.toString());
        } finally {
            try {
                if (checkEmailStmt != null) checkEmailStmt.close();
                if (userStmt != null) userStmt.close();
                if (userDataStmt != null) userDataStmt.close();
                if (fetchUserIdStmt != null) fetchUserIdStmt.close();
                if (connection != null) connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
