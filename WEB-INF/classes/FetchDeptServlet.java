import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.*;

public class FetchDeptServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession();
	
		String email = (String) session.getAttribute("email");
		System.out.println("Fetching data from email "+email);
		  if (email == null) {
			  System.out.println("Null email");
            res.getWriter().write("No email found in session.");
            return;
        }


		String query1 = "SELECT user_id from users where user_email = ?";
		String query2 = "SELECT dept_id,position from employee where user_id = ?";
		String query3 = "SELECT dept_name,dept_head,dept_priority from department where dept_id=?";
		int user_id=0;
		try(Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement prep1 = con.prepareStatement(query1);
		PreparedStatement prep2 = con.prepareStatement(query2);
		PreparedStatement prep3 = con.prepareStatement(query3)){
			
			prep1.setString(1,email);
			ResultSet rs1 = prep1.executeQuery();
			if(rs1.next()){
				user_id = rs1.getInt("user_id");
				
				prep2.setInt(1,user_id);
				ResultSet rs2 = prep2.executeQuery();
				if(rs2.next()){
					int dept_id = rs2.getInt("dept_id");
					String position = rs2.getString("position");
					
					prep3.setInt(1,dept_id);
					ResultSet rs3 = prep3.executeQuery();
					if(rs3.next()){
						String dept_name = rs3.getString("dept_name");
						String dept_head = rs3.getString("dept_head");
						int dept_priority = rs3.getInt("dept_priority");
						
						System.out.println(dept_id+" "+dept_head+" "+dept_name+" "+dept_priority);
						
						req.setAttribute("dept_id",dept_id);
						req.setAttribute("dept_name",dept_name);
						req.setAttribute("dept_head",dept_head);
						req.setAttribute("position",position);
						req.setAttribute("dept_priority",dept_priority);
						
						req.getRequestDispatcher("/Empdepartment.jsp").forward(req, res);
					}else{
						System.out.println("No dept found ");
						res.getWriter().write("No department is found");
					}
				}else{
						System.out.println(" No dept is added for u");
					res.getWriter().write("No department is still added to the user "+email);return;
				}
			}else{
					System.out.println(" No user ");
				res.getWriter().write("No user data found for userEmail: " + email);
			}
		}catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            res.getWriter().write("Error: " + e.getMessage());
		}
	}
}