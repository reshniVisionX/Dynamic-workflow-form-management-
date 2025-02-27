import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.*;

public class DeptAssignmentServlet extends HttpServlet {   
  
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            
            StringBuilder jsonData = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }

            JSONObject jsonObject = new JSONObject(jsonData.toString());
            String deptName = jsonObject.getString("dept_name");
            JSONArray usersArray = jsonObject.getJSONArray("users");

            String query1 = "SELECT dept_id FROM department WHERE LOWER(dept_name) = ?";
            String query2 = "INSERT INTO employee(user_id, dept_id, position) VALUES (?, ?, ?)";
            String query = "UPDATE userdata SET job_role = ? WHERE user_id = ?";

            int dept_id = -1; 
            try (Connection con = DBConnection.getInstance().getConnection();
                 PreparedStatement prep = con.prepareStatement(query1);
                 PreparedStatement prep1 = con.prepareStatement(query2)) {

                prep.setString(1, deptName.toLowerCase());
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next()) {
                        dept_id = rs.getInt("dept_id");
                    } else {
                        System.out.println("No department found for name: " + deptName);
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(new JSONObject().put("message", "Department not found.").toString());
                        return;
                    }
                }

                int n = usersArray.length();
                int successfulInserts = 0;

                for (int i = 0; i < n; i++) {
                    JSONObject userObject = usersArray.getJSONObject(i);
                    int userId = userObject.getInt("user_id");
                    String position = userObject.getString("position");

                    prep1.setInt(1, userId);
                    prep1.setInt(2, dept_id);
                    prep1.setString(3, position);

                    int rowsInserted = prep1.executeUpdate();
                    if (rowsInserted > 0) {
                        successfulInserts++;
						
					         PreparedStatement uprep = con.prepareStatement(query);
					         uprep.setInt(2,userId);
					         uprep.setString(1,position);
							 int update = uprep.executeUpdate();
							 if(update>0){
								 System.out.println("UserData Updated");
							 }
			     	 
                    }
					
                }

                  if (successfulInserts == n) {
              
                    System.out.println("All positions were updated successfully.");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(new JSONObject().put("message", "All positions were successfully assigned.").toString());
                } else {
                    System.out.println("Some entries were skipped or failed.");
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    out.print(new JSONObject().put("message", "Some entries were not successfully assigned.").toString());
                }

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(new JSONObject().put("message", "Database error occurred.").toString());
            }

        } catch (JSONException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject errorJson = new JSONObject();
            errorJson.put("message", "Invalid data format.");
            out.print(errorJson.toString());
        }
    }
}
