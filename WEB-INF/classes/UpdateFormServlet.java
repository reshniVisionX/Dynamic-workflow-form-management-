import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

@WebServlet("/UpdateFormServlet")
public class UpdateFormServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String requestData = sb.toString();
            JSONObject jsonObject = new JSONObject(requestData);

            int curId = jsonObject.getInt("cur_id");
            String state = jsonObject.getString("state");
			String cur_status = jsonObject.getString("cur_status");
            String tableName = jsonObject.getString("table_name");
            JSONObject updatedRecord = jsonObject.getJSONObject("updated_record");
            String upd="";
            if(cur_status.equals("verification") || cur_status.equals("verified")){
				upd="verified";
			}else{
				upd="pending";
			}

            StringBuilder updateQuery = new StringBuilder("UPDATE " + tableName + " SET ");
            updateQuery.append("status = '").append(upd).append("', "); 
            
            for (String column : updatedRecord.keySet()) {
                updateQuery.append(column)
                        .append(" = '")
                        .append(updatedRecord.getString(column).replace("'", "''")) 
                        .append("', ");
            }
            updateQuery.setLength(updateQuery.length() - 2);
            updateQuery.append(" WHERE cur_id = ").append(curId);

            try (Connection conn = DBConnection.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {
                int updatedRows = stmt.executeUpdate(updateQuery.toString());
                System.out.println("Update Query: " + updateQuery);

                if (updatedRows > 0) {
					if(upd.equals("verified")){
						String query = "SELECT approver_id FROM workflow_action where cur_id=?";
						PreparedStatement prep=conn.prepareStatement(query);
						prep.setInt(1,curId);
						ResultSet res = prep.executeQuery();
						if(res.next()){
							int approver_id = res.getInt("approver_id");
							System.out.println("Approver id is "+approver_id);
							String notify = "INSERT INTO notification (user_id,message) VALUES(?,?)";
							PreparedStatement nprep=conn.prepareStatement(notify);
							nprep.setInt(1,approver_id);
							nprep.setString(2,"Your request is verified by the employee.");
						    int ntfy = nprep.executeUpdate();
							if(ntfy>0){
								System.out.println("Nofication success");
								response.getWriter().write("{\"success\": true,\"message\": \"Verification updated successfully.\"}");
							}else{
								System.out.println("Unable to notify");
								 response.getWriter().write("{\"success\": false, \"message\": \"Unable to notify.\"}");
							}
						}else{
							System.out.println("Unable to fetch the approver_id");
							 response.getWriter().write("{\"success\": false, \"message\": \"Unable to fetch the approver_id.\"}");
						}
					}else{
						 response.getWriter().write("{\"success\": true,  \"message\": \"Form updated successfully.\"}");
					}
                    
                } else {
					 response.getWriter().write("{\"success\": false, \"message\": \"Error: Unable to update.\"}");
                }
            }
        }catch (PSQLException e) {
			 if (e.getMessage() != null && (e.getMessage().contains("date/time field value out of range") )) {
				  response.setStatus(HttpServletResponse.SC_OK);
			      String errorMessage = e.getMessage().replace("\n", " ").replace("\"", "\\\"").replace("\r", " ");
                 response.getWriter().write("{\"status\": \"false\", \"message\": \"Invalid date format. Please enter the date in the format 'yyyy-MM-dd'. \"}");
			 }else{
             response.setStatus(HttpServletResponse.SC_OK);
			 String errorMessage = e.getMessage().replace("\n", " ").replace("\"", "\\\"");
             response.getWriter().write("{\"status\": \"false\", \"message\": \" " +  errorMessage + "\"}");
			 }
        } catch (SQLException e) {
               response.setStatus(HttpServletResponse.SC_OK);
			    String errorMessage = e.getMessage().replace("\n", " ").replace("\"", "\\\"");
               response.getWriter().write("{\"status\": \"false\", \"message\": \"Database error: " + errorMessage + "\"}");
        }catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Error updating record.\"}");
        }
    }
}
