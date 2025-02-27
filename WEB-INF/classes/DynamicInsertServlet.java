import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.postgresql.util.PSQLException;
import org.json.JSONObject;
import org.json.JSONArray;


public class DynamicInsertServlet extends HttpServlet {

 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    PreparedStatement workflowActionStmt = null;
   
    try (Connection conn = DBConnection.getInstance().getConnection()) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
		conn.setAutoCommit(false);

        String requestBody = sb.toString();
        Map<String, String> requestData = parseRequestData(requestBody);
        System.out.println("The data parsed is " + requestData);
        String tableName = requestData.get("table_name");
        requestData.remove("table_name");
		requestData.remove("status");

        if (tableName == null || tableName.isEmpty() || requestData.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid table name or data.\"}");
            return;
        }

        String workflowQuery = "SELECT process, wkfl_id FROM workflow WHERE LOWER(wrkfl_name) = ?";
        PreparedStatement workflowStmt = conn.prepareStatement(workflowQuery);
        workflowStmt.setString(1, tableName.toLowerCase());
        ResultSet workflowResult = workflowStmt.executeQuery();
        int wkfl_id = 0;

        Integer userId = (Integer) request.getSession().getAttribute("user_id");
        Integer deptId = (Integer) request.getSession().getAttribute("dept_id");

        System.out.println("Session user_id: " + userId);
        System.out.println("Session dept_id: " + deptId);

        if (userId != null) {
            System.out.println("Added user_id to data: " + userId);
        }
        if (deptId != null) {
            System.out.println("Added dept_id to data: " + deptId);
        }
        String workflowProcess="";

        requestData.put("user_id", String.valueOf(userId));
        requestData.put("dept_id", String.valueOf(deptId));
        requestData.put("status","pending");
        String insertSQL = buildInsertSQL(tableName, requestData);
       System.out.println("Inserting table : "+insertSQL);
        if (workflowResult.next()) {
		
            wkfl_id = workflowResult.getInt("wkfl_id");
			System.out.println("-----wkfl_id-----"+wkfl_id);
            requestData.put("wkfl_id", String.valueOf(wkfl_id));
			String process = workflowResult.getString("process");
			 
		      JSONArray workflowArray = new JSONArray(process);

        for (int i = 0; i < workflowArray.length(); i++) {
            JSONObject block = workflowArray.getJSONObject(i);
            if (block.getString("state").equals("EmployeeUpdate")) {
                block.put("approver_id", userId);
                break; 
            }
        }

        workflowProcess = workflowArray.toString(2); 
        System.out.println("The workflow process "+workflowProcess);
		 PreparedStatement stmt = conn.prepareStatement(insertSQL);

    ResultSet res = stmt.executeQuery();

        if (res.next()) {
		
			 int s_no = res.getInt("s_no");
             System.out.println("Inserted record s_no: " + s_no);
           
                workflowActionStmt = conn.prepareStatement(
                    "INSERT INTO workflow_action (user_id, dept_id,wkfl_id, state, current_state, current_step, approver_id, updates,status) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?) RETURNING cur_id");
   System.out.println("Setting workflowProcess: " + workflowProcess);
                workflowActionStmt.setInt(1, userId);
                workflowActionStmt.setInt(2, deptId);
                workflowActionStmt.setInt(3,wkfl_id);
				workflowActionStmt.setObject(4, workflowProcess, java.sql.Types.OTHER);

                workflowActionStmt.setString(5, "EmployeeUpdate");
                workflowActionStmt.setString(6, "1");
                workflowActionStmt.setInt(7, 0);
                workflowActionStmt.setString(8, "approved");
				workflowActionStmt.setString(9, "pending");
 System.out.println("Insert Workflow query "+workflowActionStmt);
               ResultSet upd= workflowActionStmt.executeQuery();
			   System.out.println("Insert Workflow query "+workflowActionStmt);
			   if(upd.next()){
				   int curId = upd.getInt("cur_id");
                   System.out.println("Inserted record ID: " + curId);
				   String trigger = "INSERT INTO workflow_trigger (cur_id,updates) VALUES(?,?)";
				   PreparedStatement updtrigger = conn.prepareStatement(trigger);
                    updtrigger.setInt(1,curId);
					updtrigger.setString(2,"approved");
					
    int utrig = updtrigger.executeUpdate();
                   if(utrig>0){
					   
				   System.out.println("Trigger table updated");
                 String query1 = "UPDATE " + tableName + " SET cur_id = ? WHERE s_no = ?";
                 PreparedStatement updateStmt = conn.prepareStatement(query1);
   
    updateStmt.setInt(1, curId); 
    updateStmt.setInt(2, s_no); 

    int rowsUpdated = updateStmt.executeUpdate();

    if (rowsUpdated > 0) {
        System.out.println("Update successful! " + rowsUpdated + " row(s) affected.");
        conn.commit();
        response.getWriter().write("{\"status\": \"success\", \"message\": \"Workflow ID updated successfully...!\"}");
    } else {
        System.out.println("No rows were updated.");
        conn.rollback(); 
        response.getWriter().write("{\"status\": \"error\", \"message\": \"No rows were updated.\"}");
    }
	}else{
					   System.out.println("Cant update workflow_trigger");
					 conn.rollback(); 
        response.getWriter().write("{\"status\": \"error\", \"message\": \"Trigger table were not updated.\"}");
    }
				   
   }else{
	    conn.rollback(); 
				   System.out.println("Error updating workflow");
				    response.getWriter().write("{\"status\": \"error\", \"message\": \"Error updating the workflow_action.\"}");
	 }

		}else{
			System.out.println("Submitting form without workflow");
			  stmt = conn.prepareStatement(insertSQL);

        int rowsInserted = stmt.executeUpdate();

         if (rowsInserted > 0) {
			conn.commit();
				  response.getWriter().write("{\"status\": \"success\", \"message\": \"Form inserted successfully.\"}");
		 }
         else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"No rows were inserted.\"}");
		}}
	} 
	
	}catch (IllegalArgumentException e) {
           response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
         }catch (PSQLException e) {
			 if (e.getMessage() != null && (e.getMessage().contains("date/time field value out of range") )) {
				  response.setStatus(HttpServletResponse.SC_OK);
			      String errorMessage = e.getMessage().replace("\n", " ").replace("\"", "\\\"");
                 response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid date format. Please enter the date in the format 'yyyy-MM-dd'. Error details: " + e.getMessage() + "\"}");

			 }else{
             response.setStatus(HttpServletResponse.SC_OK);
			 String errorMessage = e.getMessage().replace("\n", " ").replace("\"", "\\\"");
             response.getWriter().write("{\"status\": \"error\", \"message\": \" " +  errorMessage + "\"}");
			 }
        } catch (SQLException e) {
               response.setStatus(HttpServletResponse.SC_OK);
			    String errorMessage = e.getMessage().replace("\n", " ").replace("\"", "\\\"");
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Database error: " + errorMessage + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid request format or server error.\"}");
        }
    

 }

    
 private Map<String, String> parseRequestData(String requestBody) {
    Map<String, String> dataMap = new HashMap<>();
    requestBody = requestBody.replace("{", "").replace("}", "").replace("\"", "").replace("data:","");
    
  
    String[] pairs = requestBody.split(",");

    for (String pair : pairs) {
        String[] keyValue = pair.split(":");
        
        if (keyValue.length == 2) {
         
            dataMap.put(keyValue[0].trim(), keyValue[1].trim());
        } else if (keyValue.length == 1) {
                throw new IllegalArgumentException("Please fill in all fields..."+keyValue[0]);
            
        }
    }

    System.out.println("Parsed Data: " + dataMap);
    return dataMap;
}

private String buildInsertSQL(String tableName, Map<String, String> data) throws IllegalArgumentException {
    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();

    for (Map.Entry<String, String> entry : data.entrySet()) {
        String key = entry.getKey().toLowerCase(); // Ensure the key is lowercase
        String value = entry.getValue() == null ? "" : entry.getValue(); // Handle null values gracefully

        if (columns.length() > 0) {
            columns.append(", ");
            values.append(", ");
        }

        columns.append(key); // Avoid adding quotes unless necessary
        values.append("'").append(value.replace("'", "''")).append("'"); // Escape single quotes
    }

    String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ") RETURNING s_no";
    System.out.println("Generated SQL: " + sql);
    return sql;
}

  /*  
private String buildInsertSQL(String tableName, Map<String, String> data) throws IllegalArgumentException {
    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();

    for (String key : data.keySet()) {
        String value = data.getOrDefault(key, ""); 

     
        if (columns.length() > 0) {
            columns.append(", ");
            values.append(", ");
        }

        columns.append("\"").append(key.toLowerCase()).append("\"");
        values.append("'").append(value).append("'");
    }

    String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ") RETURNING s_no";
    System.out.println("Generated SQL: " + sql);
    return sql;
}

*/
}

