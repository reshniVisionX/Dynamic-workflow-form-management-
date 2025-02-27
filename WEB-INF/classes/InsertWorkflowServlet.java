import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import java.sql.SQLException;
import java.io.IOException;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;
@WebServlet("/InsertWorkflowServlet")

public class InsertWorkflowServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            
            JSONObject jsonRequest = new JSONObject(sb.toString());
            String workflowName = jsonRequest.getString("workflow");
            JSONArray steps = jsonRequest.getJSONArray("steps");
            String processJson = steps.toString();

            String selectQuery = "SELECT wkfl_id FROM workflow WHERE wrkfl_name = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setString(1, workflowName);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
              
                int workflowId = rs.getInt("wkfl_id");

                String updateQuery = "UPDATE workflow SET process = ?::jsonb WHERE wkfl_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, processJson);
                updateStmt.setInt(2, workflowId);

                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    out.write("{\"status\": \"success\", \"message\": \"Workflow updated successfully!\"}");
                } else {
                    out.write("{\"status\": \"error\", \"message\": \"Failed to update workflow.\"}");
                }
                updateStmt.close();
            } else {
              
                String insertQuery = "INSERT INTO workflow (wrkfl_name, process) VALUES (?, ?::jsonb)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, workflowName);
                insertStmt.setString(2, processJson);

                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    out.write("{\"status\": \"success\", \"message\": \"Workflow created successfully!\"}");
                } else {
                    out.write("{\"status\": \"error\", \"message\": \"Failed to create workflow.\"}");
                }
                insertStmt.close();
            }

            selectStmt.close();
            conn.close();

        } catch (SQLException | JSONException | ClassNotFoundException e) {
            e.printStackTrace();
            out.write("{\"status\": \"error\", \"message\": \"Error processing workflow data: " + e.getMessage() + "\"}");
        }
    }
}
