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
import java.util.*;

public class FetchDeptPeopleServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
		
		Map<Integer,String> users = new HashMap<>();
		String Query1 = "SELECT  username,user_id from users where user_id not in (SELECT user_id from employee) and role_id not in (2,1) ";
		try(Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement prep = con.prepareStatement(Query1)){
			    ResultSet rs = prep.executeQuery();
				while(rs.next()){
					  int id = rs.getInt("user_id");
					  String name = rs.getString("username");
					  users.put(id,name);
				}
				
				if(users.isEmpty()){
					response.getWriter().write("{\"message\":\"No users found\"}");
					return;
				}
				

				JSONArray arr=new JSONArray();
				for(Map.Entry<Integer,String> entry : users.entrySet()){
					JSONObject obj = new JSONObject();
					obj.put("user_id",entry.getKey());
					obj.put("user_name",entry.getValue());
					arr.put(obj);
				}
				response.getWriter().write(arr.toString());
				
		}catch(SQLException | ClassNotFoundException e){
			e.printStackTrace();
			response.getWriter().write("{\"error\":\"Error fetching users\"}");
            return;
		}
	}
}