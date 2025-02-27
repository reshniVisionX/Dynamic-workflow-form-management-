import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FetchServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        Integer roleId = (Integer) session.getAttribute("role_id");

        if (roleId == null) {
            out.println("{\"error\": \"Role not found in session.\"}");
            return;
        }

        String statusFilter = request.getParameter("statusFilter");

        String userIdParam = request.getParameter("user_id");

        List<ArrayList<String>> hrList = new ArrayList<>();

        try {
           
            Connection conn = DBConnection.getInstance().getConnection();

        
            String fetchQuery = "SELECT u.username, u.user_id, u.user_email, u.user_password, u.createdAt, "
                    + "d.contact, d.dob, d.job_role, d.salary, d.address, u.status "
                    + "FROM users u "
                    + "JOIN userData d ON u.user_id = d.user_id "
                    + "WHERE u.role_id = ? ";

            
            if (statusFilter != null && !statusFilter.isEmpty()) {
                fetchQuery += "AND u.status = ? ";
            }

           
            if (userIdParam != null && !userIdParam.isEmpty()) {
                fetchQuery += "AND u.user_id = ? ";
            }

            try (PreparedStatement stmt = conn.prepareStatement(fetchQuery)) {
             
                stmt.setInt(1, roleId);

               
                int paramIndex = 2;
                if (statusFilter != null && !statusFilter.isEmpty()) {
                    stmt.setString(paramIndex++, statusFilter);
                }

               
                if (userIdParam != null && !userIdParam.isEmpty()) {
                    stmt.setInt(paramIndex, Integer.parseInt(userIdParam));
                }

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    ArrayList<String> userDetails = new ArrayList<>();
                    userDetails.add(rs.getString("username"));
                    userDetails.add(String.valueOf(rs.getInt("user_id")));
                    userDetails.add(rs.getString("user_email"));
                    userDetails.add(rs.getString("user_password"));
                    userDetails.add(rs.getString("contact"));
                    userDetails.add(rs.getString("dob"));
                    userDetails.add(rs.getString("job_role"));
                    userDetails.add(rs.getString("salary"));
                    userDetails.add(rs.getString("address"));
                    userDetails.add(rs.getString("createdAt"));
                    userDetails.add(rs.getString("status"));
                 
                    hrList.add(userDetails);
                }
            }

           
            out.print(new com.google.gson.Gson().toJson(hrList));
            out.flush();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            out.println("{\"error\": \"Error fetching data: " + e.getMessage() + "\"}");
        }
    }
}
