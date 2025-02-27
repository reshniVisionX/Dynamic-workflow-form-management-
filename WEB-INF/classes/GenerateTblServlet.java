import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import org.json.JSONException;

public class GenerateTblServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String formDataJson = request.getParameter("formData");
       
        JSONObject formData = new JSONObject(formDataJson);
        String formName;
       
        try {
            formName = formData.getString("formName");
            formData.remove("formName");
        } catch (JSONException e) {
            response.getWriter().write("Error: formName not found in formData");
            return;
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ").append(formName).append(" (");
        queryBuilder.append("s_no SERIAL PRIMARY KEY, ");
        queryBuilder.append("user_id INT UNIQUE REFERENCES users(user_id), ");
        queryBuilder.append("dept_id INT REFERENCES department(dept_id), ");

    
        int i = 0;
        int numColumns = formData.length();

        for (String header : formData.keySet()) {
            String type = formData.getString(header);
            queryBuilder.append(header).append(" ");

            switch (type.toLowerCase()) {
                case "email":
                    queryBuilder.append("VARCHAR(50) CHECK (").append(header)
                        .append(" ~* '^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$')");
                    break;
                case "age":
                    queryBuilder.append("INT CHECK (").append(header).append(" >= 18)");
                    break;
                case "mobile":
                    queryBuilder.append("NUMERIC CHECK (").append(header).append("::TEXT ~'\\d{10}$')");
                    break;
                case "ipaddress":
                    queryBuilder.append("TEXT CHECK (").append(header)
                        .append(" ~ '^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$')");
                    break;
                case "url":
                    queryBuilder.append("TEXT CHECK (").append(header)
                        .append(" ~ '^https?://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(/[^\\s]*)?$')");
                    break;
                default:{
					queryBuilder.append(type);
				}
                   
            }
            if (++i < numColumns) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(");");

        System.out.println("Generated SQL Query: " + queryBuilder.toString());

        try (Connection connection = DBConnection.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            int rowsAffected = statement.executeUpdate(queryBuilder.toString());
            System.out.println("------------Dynamic DB Inserted-------------");

            String fetchFormId = "SELECT form_id FROM forms WHERE form_name=?";
            try (PreparedStatement fetchStmt = connection.prepareStatement(fetchFormId)) {
                fetchStmt.setString(1, formName);
                ResultSet formIdResult = fetchStmt.executeQuery();

                if (formIdResult.next()) {
                    int formId = formIdResult.getInt("form_id");

                    String fetchDeptData = "SELECT dept_ids FROM forms WHERE form_name=?";
                    String updateDeptForms = "UPDATE department SET forms=? WHERE dept_id=?";

                    try (PreparedStatement fetchDeptStmt = connection.prepareStatement(fetchDeptData);
                         PreparedStatement updateDeptStmt = connection.prepareStatement(updateDeptForms)) {

                        fetchDeptStmt.setString(1, formName);
                        ResultSet rs = fetchDeptStmt.executeQuery();

                        while (rs.next()) {
                            String deptIds = rs.getString("dept_ids").trim();

                            if (deptIds != null && !deptIds.isEmpty()) {
                                String[] ids = deptIds.split(",");
                                int updatedCount = 0;

                                for (String deptIdStr : ids) {
                                    int deptId = Integer.parseInt(deptIdStr.trim());
                                    updateDeptStmt.setInt(2, deptId);

                                    String query = "SELECT forms FROM department WHERE dept_id=?";
                                    PreparedStatement prep = connection.prepareStatement(query);
                                    prep.setInt(1, deptId);
                                    ResultSet deptResult = prep.executeQuery();

                                    if (deptResult.next()) {
                                        String existingForms = deptResult.getString("forms").trim();
                                        String updatedForms = (existingForms.isEmpty() || existingForms.equals("0")) ?
                                            String.valueOf(formId) : existingForms + "," + formId;

                                        updateDeptStmt.setString(1, updatedForms);
                                        int rowsUpdated = updateDeptStmt.executeUpdate();

                                        if (rowsUpdated > 0) {
                                            updatedCount++;
                                        } else {
                                            System.out.println("Failed to update department " + deptId + ".");
                                        }
                                    }
                                    deptResult.close();
                                }

                                if (updatedCount == ids.length) {
                                    System.out.println("--------------All Departments updated with the form--------------");
                                    response.getWriter().write("{\"status\": \"success\", \"message\": \"Database created and updated successfully.\"}");
                                }
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        response.getWriter().write("Error: " + e.getMessage());
                    }
                } else {
                    response.getWriter().write("Error: Form not found.");
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}
