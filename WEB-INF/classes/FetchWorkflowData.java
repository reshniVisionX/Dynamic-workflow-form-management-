import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/FetchWorkflowData")
public class FetchWorkflowData extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {        
      
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();       
    
        Integer userId = (Integer) request.getSession().getAttribute("user_ID");
        if (userId == null) {
            out.write("{\"error\": \"User not logged in\"}");
            return;
        }else{
			System.out.println("The userId is"+userId);
		}
        
        JSONArray finalResponse = new JSONArray();
        
        try (Connection conn = DBConnection.getInstance().getConnection()) {
      
            String actionQuery = 
                "SELECT wf.wrkfl_name, wa.cur_id " +
                "FROM workflow_action wa " +
                "JOIN workflow wf ON wa.wkfl_id = wf.wkfl_id " +
                "WHERE wa.approver_id = ?";
            
            PreparedStatement actionStmt = conn.prepareStatement(actionQuery);
            actionStmt.setInt(1, userId);
            ResultSet actionRs = actionStmt.executeQuery();            
         
            Map<String, JSONArray> tableDataMap = new HashMap<>();
            int f=0;
            while (actionRs.next()) {
                String workflowTableName = actionRs.getString("wrkfl_name");
                int curId = actionRs.getInt("cur_id");
                f=1;
               
                JSONArray records = fetchTableData(conn, workflowTableName, curId);
                
               
                if (tableDataMap.containsKey(workflowTableName)) {
                    JSONArray existingData = tableDataMap.get(workflowTableName);
                    for (int i = 0; i < records.length(); i++) {
                        existingData.put(records.getJSONObject(i));
                    }
                } else {
                    tableDataMap.put(workflowTableName, records);
                }
            }
            
            actionRs.close();
            actionStmt.close();
                     
            for (String tableName : tableDataMap.keySet()) {
                JSONObject jsonTable = new JSONObject();
                jsonTable.put(tableName, tableDataMap.get(tableName));
                finalResponse.put(jsonTable);
            }
            if(f==0){
				 out.write("{\"error\": \"No workflow action is pending: \"}");
				 return;
			}
            out.write(finalResponse.toString());
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            out.write("{\"error\": \"Database error occurred: " + e.getMessage() + "\"}");
        }
    }

    private JSONArray fetchTableData(Connection conn, String tableName, int curId) {
        JSONArray data = new JSONArray();
        
        try {
         
            String query = "SELECT * FROM " + tableName + " WHERE cur_id = ? and status != 'verification'";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, curId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                JSONObject record = new JSONObject();                
               
                ResultSetMetaData metaData = rs.getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    record.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                
                data.put(record);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }
}
