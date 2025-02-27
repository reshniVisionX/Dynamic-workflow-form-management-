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

public class FetchAllFormServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
		
		String query ="SELECT form_name from forms";
		JSONArray formsList = new JSONArray();

		try(Connection con = DBConnection.getInstance().getConnection();
		     PreparedStatement prep = con.prepareStatement(query)){
			 ResultSet rs = prep.executeQuery();
			 while(rs.next()){
				formsList.put(rs.getString("form_name"));
			 }
			 
		}catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.getWriter().write("{\"error\":\"Error fetching forms\"}");
            return;
		}
		System.out.println("The form list are :"+formsList.toString());
		response.getWriter().write(formsList.toString());
	}
}