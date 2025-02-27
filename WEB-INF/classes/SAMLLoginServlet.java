import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;


import jakarta.servlet.http.HttpSession;

@WebServlet("/SAMLLoginServlet")
public class SAMLLoginServlet extends HttpServlet {
	
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String personname = request.getParameter("personname");
        String email = request.getParameter("personemail");

        HttpSession session = request.getSession();
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("Name: " + personname);
        out.println("Email: " + email);
		     System.out.println("Name: " + personname + ", Email: " + email);
			 
		String query = "SELECT * from users where user_email = ?";
		try (Connection conn = DBConnection.getInstance().getConnection()) {
			PreparedStatement prep = conn.prepareStatement(query);
			prep.setString(1,email);
			ResultSet res = prep.executeQuery();
			if(res.next()){
				int roleId = res.getInt("role_id");
				String username = res.getString("username");
				String status = res.getString("status");
				int user_ID = res.getInt("user_id");
				
				session.setAttribute("username", username);
				session.setAttribute("email",email);
				session.setAttribute("role",roleId);
				session.setAttribute("user_ID",user_ID);
			    session.setMaxInactiveInterval(30 * 60);
                     
				if(status.equals("active")){
                   if(email.equals("admin@gmail.com") && roleId == 1){
					   
					   response.sendRedirect(request.getContextPath() +"/adminPage.jsp");
					   return;
			    	}else{
						 
                      if (roleId == 2) {
                        response.sendRedirect(request.getContextPath() +"/hrPage.jsp");
                      } else if (roleId == 3) {
                        response.sendRedirect(request.getContextPath() +"/employeePage.jsp");
                      } else {
                        out.println("<h2>Role not found</h2>");
                      }
					}
                        
				}else{
					out.println("<h2>Login Failed</h2>");
                    out.println("<p>User is inactive can't access the dashboard</p>");
				}
					  
		 
            } 
            else{
					out.println("<h2>Login Failed</h2>");
                    out.println("<p>User doesnt exist</p>");
					out.println("<p>Invalid email or password. Please try again.</p>");
				}

				
			
		}catch (SQLException e) {
            e.printStackTrace();
            out.write("{\"status\":\"error\", \"message\":\"Error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"status\":\"error\", \"message\":\"Unexpected error: " + e.getMessage() + "\"}");
        } finally {
            out.close();
        }

    }
}