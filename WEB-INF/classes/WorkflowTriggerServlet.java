import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/WorkflowTriggerServlet")
public class WorkflowTriggerServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("action");
        int curId = Integer.parseInt(request.getParameter("cur_id"));

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String updateQuery = "UPDATE workflow_trigger SET updates = ? WHERE cur_id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, action); 
            stmt.setInt(2, curId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                out.write("{\"status\":\"success\", \"message\":\"Action performed successfully\"}");
            } else {
                out.write("{\"status\":\"error\", \"message\":\"No rows updated\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"status\":\"error\", \"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
}
