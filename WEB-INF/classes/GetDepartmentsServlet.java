import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.*;

public class GetDepartmentsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonResponse = new JSONObject();
        JSONArray departments = new JSONArray();

        try (Connection con = DBConnection.getInstance().getConnection()) {
            String query = "SELECT dept_id, dept_name FROM department";
            try (PreparedStatement stmt = con.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject department = new JSONObject();
                    department.put("dept_id", rs.getInt("dept_id"));
                    department.put("dept_name", rs.getString("dept_name"));
                    departments.put(department);
                }
            }
        } catch (Exception e) {
            jsonResponse.put("error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }

        jsonResponse.put("departments", departments);

      
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }
}
