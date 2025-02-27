import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

public class DeleteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
  
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        
        String userId = request.getParameter("hrId");

        if (userId == null || userId.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid or missing HR ID.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try (Connection conn = DBConnection.getInstance().getConnection()) {
         
            String checkStatusQuery = "SELECT status FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkStatusQuery)) {
                pstmt.setInt(1, Integer.parseInt(userId));
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String currentStatus = rs.getString("status");
                        String newStatus = currentStatus.equals("inactive") ? "active" : "inactive";
                        String updateStatusQuery = "UPDATE users SET status = ? WHERE user_id = ?";
                        try (PreparedStatement updatePstmt = conn.prepareStatement(updateStatusQuery)) {
                            updatePstmt.setString(1, newStatus);
                            updatePstmt.setInt(2, Integer.parseInt(userId));
                            int rowsUpdated = updatePstmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                jsonResponse.put("status", "success");
                                jsonResponse.put("message", "User status toggled successfully.");
                            } else {
                                jsonResponse.put("status", "error");
                                jsonResponse.put("message", "Failed to update user status.");
                            }
                        }
                    } else {
                        jsonResponse.put("status", "error");
                        jsonResponse.put("message", "User not found.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An error occurred: " + e.getMessage());
        }

      
        response.getWriter().write(jsonResponse.toString());
    }
}
