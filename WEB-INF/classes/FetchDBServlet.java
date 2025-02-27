import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;

public class FetchDBServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");  
        PrintWriter out = response.getWriter();

        String tableName = request.getParameter("table_name");
        tableName = tableName.toLowerCase();

        if (tableName == null || tableName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Table name is missing or invalid.\"}");
            return;
        }

        ArrayList<Map<String, Object>> tableData = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection()) {
           
            String columnQuery = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "' ORDER BY ordinal_position"; 
            System.out.println("Executing SQL query for columns: " + columnQuery); 

            Statement columnStmt = conn.createStatement();
            ResultSet columns = columnStmt.executeQuery(columnQuery);

            ArrayList<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                columnNames.add(columns.getString("column_name"));
            }

            System.out.println("Columns in table " + tableName + ": " + columnNames);

            String query = "SELECT * FROM " + tableName;
            System.out.println("Executing SQL query: " + query); 

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("Rows extracted: " + rs);
            int rowCount = 0; 
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (String columnName : columnNames) {
                    Object columnValue = rs.getObject(columnName);

                    
                    if (columnValue instanceof Date) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        columnValue = sdf.format((Date) columnValue);
                    }

                    System.out.println("Column: " + columnName + ", Value: " + columnValue);
                    row.put(columnName, columnValue);
                }
                tableData.add(row);
                rowCount++;
            }

            System.out.println("Rows fetched: " + rowCount);
            if (rowCount == 0) {
                Map<String, Object> emptyRow = new HashMap<>();
                for (String columnName : columnNames) {
                    emptyRow.put(columnName, null);
                }
                tableData.add(emptyRow);
            }

            System.out.println("Table Data: " + tableData);

            if (tableData.isEmpty()) {
                out.write("{\"message\":\"No data found\"}"); 
            } else {
                StringBuilder jsonResponse = new StringBuilder();
                jsonResponse.append("[");

                for (int i = 0; i < tableData.size(); i++) {
                    Map<String, Object> row = tableData.get(i);
                    jsonResponse.append("{");

                    int j = 0;
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();

                        jsonResponse.append("\"").append(key).append("\":");

                        if (value instanceof Map) {
                            jsonResponse.append("{}"); 
                        } else if (value instanceof String) {
                            jsonResponse.append("\"").append(value).append("\"");
                        } else {
                            jsonResponse.append(value);  
                        }

                        if (j < row.size() - 1) {
                            jsonResponse.append(",");
                        }
                        j++;
                    }

                    jsonResponse.append("}");

                    if (i < tableData.size() - 1) {
                        jsonResponse.append(",");
                    }
                }

                jsonResponse.append("]");

                System.out.println("JSON Response: " + jsonResponse.toString());

                out.write(jsonResponse.toString());
            }
            out.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Failed to retrieve data. SQLException: " + e.getMessage() + "\"}"); 
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Failed to load JDBC driver. ClassNotFoundException: " + e.getMessage() + "\"}");
        } finally {
            out.close(); 
        }
    }
}
