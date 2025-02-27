import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Integer user_ID = (Integer) session.getAttribute("user_ID");
        System.out.println("User ID for notification is: " + user_ID);

        List<String> notifications = new ArrayList<>();

        if (user_ID != null) {
            try (Connection conn = DBConnection.getInstance().getConnection()) {
            
                String fetchSQL = "SELECT message FROM notification WHERE user_id = ?";
                try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSQL)) {
                    fetchStmt.setInt(1, user_ID);
                    try (ResultSet rs = fetchStmt.executeQuery()) {
                        while (rs.next()) {
                            notifications.add(rs.getString("message"));
                        }
                    }
                }

                if (!notifications.isEmpty()) {
                    String deleteSQL = "DELETE FROM notification WHERE user_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                        deleteStmt.setInt(1, user_ID);
                        int deletedRows = deleteStmt.executeUpdate();
                        System.out.println("Deleted " + deletedRows + " notifications for user ID: " + user_ID);
                    }
                } else {
                    System.out.println("No notifications found for user ID: " + user_ID);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("User ID is null");
        }

        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("{ \"notifications\": [");

        for (int i = 0; i < notifications.size(); i++) {
            jsonResponse.append("\"").append(notifications.get(i).replace("\"", "\\\"")).append("\"");
            if (i < notifications.size() - 1) {
                jsonResponse.append(", ");
            }
        }

        jsonResponse.append("] }");

        try (PrintWriter out = response.getWriter()) {
            out.write(jsonResponse.toString());
        }
    }
}
