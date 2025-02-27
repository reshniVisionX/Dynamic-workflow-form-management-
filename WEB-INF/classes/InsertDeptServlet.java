import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

public class InsertDeptServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();

        String deptName = request.getParameter("dept_name");
        String deptHead = request.getParameter("dept_head");
        String deptPriority = request.getParameter("dept_priority");
        String deptStatus = request.getParameter("dept_status");
        String forms = request.getParameter("forms");

        if (deptName == null || deptName.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Department name is required.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        if (deptPriority == null || deptPriority.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Department priority is required.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try (Connection connection = DBConnection.getInstance().getConnection()) {
            
            String checkQuery = "SELECT dept_id FROM department WHERE LOWER(dept_name) = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, deptName.toLowerCase());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        jsonResponse.put("status", "error");
                        jsonResponse.put("message", "Department already exists.");
                        response.getWriter().write(jsonResponse.toString());
                        return;
                    }
                }
            }

            String insertQuery = "INSERT INTO department (dept_name, dept_head, dept_priority, dept_status, forms) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, deptName);
                insertStmt.setString(2, deptHead);
                insertStmt.setInt(3, Integer.parseInt(deptPriority));
                insertStmt.setString(4, deptStatus);
                insertStmt.setString(5, "0"); 

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Department added successfully.");
                } else {
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "Failed to add department.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }
}
