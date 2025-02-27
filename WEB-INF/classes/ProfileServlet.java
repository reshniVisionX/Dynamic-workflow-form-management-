import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

public class ProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		HttpSession session = req.getSession();
       
        String email = (String) session.getAttribute("email");

        System.out.println("Fetching data for email: " + email);

       
        String query1 = "SELECT user_id FROM users WHERE user_email = ?";
        String dataprep = "SELECT username, contact, dob, job_role, salary, address " +
                          "FROM users u JOIN userdata d ON u.user_id = d.user_id WHERE u.user_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement prep = con.prepareStatement(query1);
             PreparedStatement data = con.prepareStatement(dataprep)) {

           
            prep.setString(1, email);

            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");

               
                data.setInt(1, userId);

                ResultSet userData = data.executeQuery();
                if (userData.next()) { 
                    String username = userData.getString("username");
                    long contact = userData.getLong("contact");  
                    String dob = userData.getString("dob");
                    String jobRole = userData.getString("job_role");
                    double salary = userData.getDouble("salary");
                    String address = userData.getString("address");

                 
                    System.out.println("Username: " + username);
                    System.out.println("Contact: " + contact);
                    System.out.println("DOB: " + dob);
                    System.out.println("Job Role: " + jobRole);
                    System.out.println("Salary: " + salary);
                    System.out.println("Address: " + address);

                    
                    req.setAttribute("username", username);
                    req.setAttribute("contact", contact);
                    req.setAttribute("dob", dob);
                    req.setAttribute("jobRole", jobRole);
                    req.setAttribute("salary", salary);
                    req.setAttribute("address", address);

                 
                    req.getRequestDispatcher("/myProfile.jsp").forward(req, res);
                } else {
                    res.getWriter().write("No user data found for userId: " + userId);
                }
            } else {
                res.getWriter().write("No user found with email: " + email);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            res.getWriter().write("Error: " + e.getMessage());
        }
    }
}
