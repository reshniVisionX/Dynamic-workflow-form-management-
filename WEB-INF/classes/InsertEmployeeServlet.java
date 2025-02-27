import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InsertEmployeeServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String deptName = request.getParameter("dept_name");
        List<String> emails = new ArrayList<>();
        List<String> positions = new ArrayList<>();

        int i = 1;
        while (request.getParameter("user_email_" + i) != null) {
            emails.add(request.getParameter("user_email_" + i));
            positions.add(request.getParameter("position_" + i));
            i++;
        }

        try (Connection connection = DBConnection.getInstance().getConnection()) {
            
            String deptQuery = "SELECT dept_id FROM department WHERE LOWER(dept_name) = LOWER(?)";
            int deptId;

            try (PreparedStatement deptStmt = connection.prepareStatement(deptQuery)) {
                deptStmt.setString(1, deptName);
                ResultSet deptResult = deptStmt.executeQuery();
                if (deptResult.next()) {
                    deptId = deptResult.getInt("dept_id");
                } else {
                    response.getWriter().write("Department not found.");
                    return;
                }
            }
  
            String userQuery = "SELECT user_id FROM users WHERE user_email = ?";            
            String insertQuery = "INSERT INTO employee (user_id, dept_id, position) VALUES (?, ?, ?)";
            String updateQuery = "UPDATE userdata SET job_role = ? WHERE user_id = ?";

            try (PreparedStatement userStmt = connection.prepareStatement(userQuery);
                 PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                
                for (int j = 0; j < emails.size(); j++) {
                  
                    userStmt.setString(1, emails.get(j));
                    ResultSet userResult = userStmt.executeQuery();

                    if (userResult.next()) {
                        int userId = userResult.getInt("user_id");
                        int rowsInserted = 0;
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, deptId);
                        insertStmt.setString(3, positions.get(j));
                        rowsInserted = insertStmt.executeUpdate();
                       if (rowsInserted > 0) {
                            updateStmt.setString(1, positions.get(j));
                            updateStmt.setInt(2, userId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                      
                        response.getWriter().write("User with email " + emails.get(j) + " not found.");
                        return;
                    }
                }
            }

            System.out.println("----------Insertion Success----------");
            response.sendRedirect("hrPage.jsp");

        } catch (SQLException | ClassNotFoundException e) {
            response.getWriter().write("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
