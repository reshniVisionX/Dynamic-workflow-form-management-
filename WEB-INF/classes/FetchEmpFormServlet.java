import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetchEmpFormServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("email") == null) {
            response.getWriter().write("{\"error\":\"User not authenticated\"}");
            return;
        }
        
        String email = (String) session.getAttribute("email");
		System.out.println("The email is "+email);
        JSONArray formsList = new JSONArray();
        String dept ="SELECT e.dept_id,u.user_id from users u join userdata d on u.user_id=d.user_id join employee e on e.user_id=d.user_id where user_email = ?";
        String query = "SELECT forms from department where dept_id=?";
        String fetchFormNameQuery = "SELECT form_name FROM forms WHERE form_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
		     PreparedStatement prep = conn.prepareStatement(dept);
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement fetchStmt = conn.prepareStatement(fetchFormNameQuery)) {
             prep.setString(1,email);
			 ResultSet res = prep.executeQuery();
			 if(res.next()){
				  int userId = res.getInt("user_id");
                  int deptId = res.getInt("dept_id");
			 
            stmt.setInt(1,deptId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                  String forms = rs.getString("forms");
				  
                
                session.setAttribute("user_id", (Integer)userId);
                session.setAttribute("dept_id", (Integer)deptId);
                if (forms != null && !forms.isEmpty()) {
                    for (String formId : forms.split(",")) {
                        fetchStmt.setInt(1, Integer.parseInt(formId.trim()));
                        ResultSet formRs = fetchStmt.executeQuery();
                        
                        if (formRs.next()) {
                            JSONObject formObject = new JSONObject();
                            formObject.put("formName", formRs.getString("form_name"));
                            formsList.put(formObject);
                        }
                    }
                }
			}
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.getWriter().write("{\"error\":\"Error fetching forms\"}");
            return;
        }
         System.out.println("The form list is "+formsList.toString());
        response.getWriter().write(formsList.toString());
    }
}
