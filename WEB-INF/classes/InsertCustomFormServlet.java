import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class InsertCustomFormServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            JSONObject formData = new JSONObject(request.getReader().lines().reduce("", String::concat));

            String formName = formData.getString("form_name");
            int noCols = formData.getInt("no_cols");
            String deptIds = formData.getString("departments");
            String expiryDate = formData.getString("expiry_date");
            JSONObject fields = formData.getJSONObject("fields");
			
			String checkFormSQL = "SELECT COUNT(*) AS count FROM Forms WHERE LOWER(form_name) = LOWER(?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkFormSQL);
            checkStmt.setString(1, formName);

            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt("count") > 0) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Form name already exists. Please use a different name.");
                out.print(jsonResponse.toString());
                return; 
            }
	
			HttpSession session = request.getSession();
            session.setAttribute("form_name", formName);


            System.out.println(formName + " -no-ofcol-" + noCols + " ,deptids-" + deptIds);

            String insertFormSQL = "INSERT INTO Forms (form_name, no_cols, dept_ids, expiry_date) VALUES (?, ?, ?, ?) RETURNING form_id";
            PreparedStatement stmt = conn.prepareStatement(insertFormSQL);
            stmt.setString(1, formName);
            stmt.setInt(2, noCols);
            stmt.setString(3, deptIds);
            stmt.setDate(4, java.sql.Date.valueOf(expiryDate));

            System.out.println("Insert Query: " + insertFormSQL);
            ResultSet rs = stmt.executeQuery();
            int formId = 0;
            if (rs.next()) {
                formId = rs.getInt("form_id");
            }

            if (formId == 0) {
                throw new Exception("Failed to insert into Forms table.");
            }

            StringBuilder createTableSQL = new StringBuilder("CREATE TABLE " + formName + " (");
            createTableSQL.append("s_no SERIAL PRIMARY KEY, ")
                          .append("user_id INT REFERENCES users(user_id), ")
                          .append("dept_id INT REFERENCES department(dept_id), ")
                          .append("cur_id INT , ")
					      .append("status TEXT DEFAULT 'pending',");
fields.keys().forEachRemaining(key -> {
            String value = fields.getString(key);
            System.out.println("Key: " + key + ", Value: " + value);
			createTableSQL.append("\"").append(key).append("\" ").append(value).append(", ");
      
        });

            createTableSQL.setLength(createTableSQL.length() - 2);
            createTableSQL.append(")");
            System.out.println("Create table Query is "+createTableSQL);
            Statement tableStmt = conn.createStatement();
            tableStmt.executeUpdate(createTableSQL.toString());
 
            String value = "SELECT forms from department where dept_id=?";
            String query = "UPDATE department set forms = ? where dept_id=?";
			
            String ids[]=deptIds.split(",");
			PreparedStatement selectStmt = conn.prepareStatement(value);
    int c=0;
    for (int i = 0; i < ids.length; i++) {
        int id = Integer.parseInt(ids[i]);

        selectStmt.setInt(1, id);
        ResultSet res = selectStmt.executeQuery();
        String currentForms = "0"; 
        
        if (res.next()) {
            currentForms = res.getString("forms");
        }
        
        String newForms = currentForms.equals("0") ? String.valueOf(formId) : currentForms + "," + formId;
        
        PreparedStatement upd = conn.prepareStatement(query);
        upd.setString(1, newForms);
        upd.setInt(2, id);
        
        int update = upd.executeUpdate();
		if(update>0){
			c++;
		}
	}
	if(c==ids.length){
			System.out.println("All deppartments updated successfully...");
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Form and table created successfully. Departments where updated with the form");
            out.print(jsonResponse.toString());
			
	}else{
		System.out.println("Error while upading table");
	}
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error: " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }

}
