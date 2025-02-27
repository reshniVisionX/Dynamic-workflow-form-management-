import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt; 

public class LoginServlet extends HttpServlet {

    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
          
            DBConnection dbConnect = DBConnection.getInstance();
            Connection connection = dbConnect.getConnection();

        
            String query = "SELECT user_id,role_id, username, status, user_password FROM users WHERE user_email = ?";
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();
            int roleId=0,user_ID=0;
            if (rs.next()) {
                String storedHashedPassword = rs.getString("user_password");  
                roleId = rs.getInt("role_id");
                String username = rs.getString("username");
                String status = rs.getString("status");
				user_ID = rs.getInt("user_id");
				
                if(roleId!=1){
                if (BCrypt.checkpw(password, storedHashedPassword)) {

                    if (status.equals("inactive")) {
                        out.println("<h2>Login Failed</h2>");
                        out.println("<p>The current user is inactive currently..</p>");
                        return;
                    }
                }
                 
                    HttpSession session = request.getSession();
                    session.setAttribute("username", username);
					session.setAttribute("email",email);
					session.setAttribute("role",roleId);
					session.setAttribute("user_ID",user_ID);
					
                    session.setMaxInactiveInterval(30 * 60);
                    if (roleId == 1) {
                        response.sendRedirect("adminPage.jsp");
                    } else if (roleId == 2) {
                        response.sendRedirect("hrPage.jsp");
                    } else if (roleId == 3) {
                        response.sendRedirect("employeePage.jsp");
                    } else {
                        out.println("<h2>Role not found</h2>");
                    }

                } else {
					   HttpSession session = request.getSession();
                       session.setAttribute("username", username);
					   session.setAttribute("email",email);
					   session.setAttribute("role",roleId);
					   
					    session.setMaxInactiveInterval(30 * 60);
                      if(email.equals("admin@gmail.com") && password.equals("Admin@123")){
					   response.sendRedirect("adminPage.jsp");return;
			    	  }else{
                        out.println("<h2>Login Failed</h2>");
                        out.println("<p>Invalid email or password. Please try again.</p>");
					  }
                }
            } else {
               
                out.println("<h2>Login Failed</h2>");
                out.println("<p>Invalid email or password. Please try again.</p>");
            }

        } catch (SQLException e) {
            out.println("<h2>DB Connection Failed</h2>");
            out.println("<p>Could not connect to the database. Please try again later.</p>");
            e.printStackTrace(); 
        } catch (ClassNotFoundException e) {
            out.println("<h2>Class Not Found</h2>");
            out.println("<p>Database driver not found.</p>");
            e.printStackTrace(); 
        } finally {
            out.close();
        }
    }
}
