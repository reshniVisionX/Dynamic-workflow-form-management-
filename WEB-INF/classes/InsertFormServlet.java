import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.ResultSet;

public class InsertFormServlet extends HttpServlet {
    public static int k=1;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
     
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        try {
            Gson gson = new Gson();
            JsonObject formData = gson.fromJson(sb.toString(), JsonObject.class);
          
            String formName = formData.get("form_name").getAsString();
            int noCols = formData.get("no_cols").getAsInt();
            String expiryDate = formData.get("expiry_date").getAsString();
            String deptIds = formData.get("departments").getAsString();
            JsonObject fields = formData.getAsJsonObject("fields");
            
            String tableName = formName.toLowerCase();
            int formId = 0;
            
            Connection conn = null;
            try {
                conn = DBConnection.getInstance().getConnection();
                conn.setAutoCommit(false);
                 String exist = "SELECT form_id from forms where LOWER(form_name) = ?";
                String insertFormSQL = "INSERT INTO Forms (form_name, no_cols, dept_ids, expiry_date) VALUES (?, ?, ?, ?) RETURNING form_id";
                 PreparedStatement exitprep = conn.prepareStatement(exist);
				  exitprep.setString(1,formName.toLowerCase());
						 ResultSet res = exitprep.executeQuery();
						 if(res.next()){
							 response.setContentType("application/json");
response.getWriter().write("{\"success\": false, \"message\": \"Form Name already exist\"}");

                          return;
						 }
                PreparedStatement pstmt = conn.prepareStatement(insertFormSQL);
                pstmt.setString(1, formName);
                pstmt.setInt(2, noCols);
                pstmt.setString(3, deptIds);
                pstmt.setDate(4, java.sql.Date.valueOf(expiryDate));
                
                ResultSet result = pstmt.executeQuery();
                if(result.next()){
                    formId = result.getInt("form_id");  
                    System.out.println("Inserted Form ID: " + formId);
                    System.out.println("Form Inserted Successfully------------");
                } else {
                    System.out.println("Error inserting the form data---------------");
                    return;
                }

                StringBuilder createTableSQL = new StringBuilder();
                createTableSQL.append("CREATE TABLE ").append(tableName).append(" (")
                            .append("s_no SERIAL PRIMARY KEY, ")
                            
                            .append("user_id INT UNIQUE REFERENCES users(user_id), ")
                            .append("dept_id INT REFERENCES department(dept_id), ");
                
                for (String fieldName : fields.keySet()) {
                    String fieldType = fields.get(fieldName).getAsString();
                    createTableSQL.append(buildColumnDefinition(fieldName, fieldType));
                }
                
                createTableSQL.setLength(createTableSQL.length() - 2);
                createTableSQL.append(")");
                System.out.println("Table Query "+createTableSQL.toString());
                PreparedStatement createTableStmt = conn.prepareStatement(createTableSQL.toString());
                createTableStmt.executeUpdate();
                
                String fetchDeptData = "SELECT dept_ids FROM forms WHERE form_id=?";
                String updateDeptForms = "UPDATE department SET forms=? WHERE dept_id=?";
               
                try (PreparedStatement fetchDeptStmt = conn.prepareStatement(fetchDeptData);
                     PreparedStatement updateDeptStmt = conn.prepareStatement(updateDeptForms)) {
                        
                    fetchDeptStmt.setInt(1, formId);
                    ResultSet rs = fetchDeptStmt.executeQuery();
                    
                    if (rs.next()) {
                        String deptIdsFromForm = rs.getString("dept_ids");
                        if (deptIdsFromForm != null && !deptIdsFromForm.isEmpty()) {
                            String[] ids = deptIdsFromForm.split(",");
                            int n = ids.length;
                            int c = 0;
                            
                            for (String deptIdStr : ids) {
                                int deptId = Integer.parseInt(deptIdStr.trim());
                                
                                updateDeptStmt.setInt(2, deptId);
                               
                                String query = "SELECT forms FROM department WHERE dept_id=?";
                                try (PreparedStatement prep = conn.prepareStatement(query)) {
                                    prep.setInt(1, deptId);
                                    ResultSet deptResult = prep.executeQuery();
                                    
                                    if (deptResult.next()) {
                                        String existingForms = deptResult.getString("forms").trim();
                                        String updatedForms = (existingForms.isEmpty() || existingForms.equals("0"))
                                                ? String.valueOf(formId)
                                                : existingForms + "," + formId;
                                        
                                        updateDeptStmt.setString(1, updatedForms);
                                        int rowsUpdated = updateDeptStmt.executeUpdate();
                                        
                                        if (rowsUpdated > 0) {
                                            c++;
                                        } else {
                                            System.out.println("Failed to update department " + deptId + ".");
                                        }
                                    }
                                    deptResult.close();
                                }
                            }
                            
                            if (c == n) {
                                System.out.println("--------------All Departments updated with the form--------------");
                            }
                        }
                    }
                    rs.close();
                }
                
                conn.commit();
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"message\": \"Form created successfully\"}");
                
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid form data\"}");
            e.printStackTrace();
        }
    }
    
    private String buildColumnDefinition(String fieldName, String fieldType) {
        StringBuilder columnDef = new StringBuilder();
        fieldName = fieldName.toLowerCase();
       
        columnDef.append(fieldName).append(" ");
        
        switch (fieldType) {
            case "text":
            case "varchar(50)":
                columnDef.append("VARCHAR(255) CHECK (")
                        .append(fieldName)
                        .append(" ~ '^[a-zA-Z0-9\\s\\-\\.\\,]*$'), ");
                break;
                
            case "email":
                columnDef.append("VARCHAR(100) CONSTRAINT emailcheck")
				         .append(k++)
				         .append(" CHECK (")
                        .append(fieldName)
                        .append(" ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'), ");
                break;
                
            case "tel":
                columnDef.append("VARCHAR(15) CONSTRAINT phcheck")
				         .append(k++)
				         .append(" CHECK (")
                        .append(fieldName)
                        .append(" ~ '^[0-9]{10}$'), ");
                break;
                
            case "number":
                columnDef.append("INT CONSTRAINT numcheck")
                         .append(k++)
						 .append(" CHECK (")
                        .append(fieldName)
                        .append(" >= 0), ");
                break;
                
            case "date":
                columnDef.append("DATE ,");
                        
                break;
                
          case "gender":
             columnDef.append("VARCHAR(10) CONSTRAINT gencheck")
			 .append(k++)
			 .append(" CHECK (LOWER(")
            .append(fieldName)
            .append(") IN ('male', 'female', 'other')), ");
    break;

                
            default:
                columnDef.append("TEXT, ");
        }
        k++;
        return columnDef.toString();
    }
}
