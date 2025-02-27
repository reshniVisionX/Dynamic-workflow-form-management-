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

@WebServlet("/FetchDynamicDBServlet")
public class FetchDynamicDBServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
      
        Integer user_ID = (Integer) request.getSession().getAttribute("user_ID");
        String tableName = request.getParameter("table_name");
        if (tableName == null || tableName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Table name is missing or invalid.\"}");
            return;
        }

        tableName = tableName.toLowerCase();
        Map<String, ArrayList<String>> enumColumns = new HashMap<>();
        ArrayList<String> columnNames = new ArrayList<>();
        ArrayList<Map<String, String>> allRecords = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            
            String columnQuery = "SELECT c.column_name, t.typname, e.enumlabel " +
                                 "FROM information_schema.columns c " +
                                 "LEFT JOIN pg_type t ON c.udt_name = t.typname " +
                                 "LEFT JOIN pg_enum e ON t.oid = e.enumtypid " +
                                 "WHERE c.table_name = ? " +
                                 "ORDER BY c.ordinal_position, e.enumsortorder";

            try (PreparedStatement columnStmt = conn.prepareStatement(columnQuery)) {
                columnStmt.setString(1, tableName);
                try (ResultSet columns = columnStmt.executeQuery()) {
                    while (columns.next()) {
                        String columnName = columns.getString("column_name");
                        String enumLabel = columns.getString("enumlabel");

                        if (!columnNames.contains(columnName)) {
                            columnNames.add(columnName);
                        }

                        if (enumLabel != null) {
                            enumColumns.computeIfAbsent(columnName, k -> new ArrayList<>()).add(enumLabel);
                        }
                    }
                }
            }

         String recordQuery = "SELECT * FROM " + tableName + " WHERE user_id = ?";
try (PreparedStatement recordStmt = conn.prepareStatement(recordQuery)) {
    recordStmt.setInt(1, user_ID); 
    try (ResultSet resultSet = recordStmt.executeQuery()) {
        while (resultSet.next()) {
            Map<String, String> record = new HashMap<>();
            for (String column : columnNames) {
                record.put(column, resultSet.getString(column));
            }
            allRecords.add(record);
        }
    }
}

            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");

            jsonResponse.append("\"data\":{");
            for (int i = 0; i < columnNames.size(); i++) {
                jsonResponse.append("\"").append(columnNames.get(i)).append("\":null");
                if (i < columnNames.size() - 1) {
                    jsonResponse.append(",");
                }
            }
            jsonResponse.append("},");

            jsonResponse.append("\"enum\":{");
            int enumCount = 0;
            for (Map.Entry<String, ArrayList<String>> entry : enumColumns.entrySet()) {
                jsonResponse.append("\"").append(entry.getKey()).append("\":[");
                ArrayList<String> values = entry.getValue();
                for (int j = 0; j < values.size(); j++) {
                    jsonResponse.append("\"").append(values.get(j)).append("\"");
                    if (j < values.size() - 1) {
                        jsonResponse.append(",");
                    }
                }
                jsonResponse.append("]");
                if (++enumCount < enumColumns.size()) {
                    jsonResponse.append(",");
                }
            }
            jsonResponse.append("},");

            jsonResponse.append("\"field_rows\":[");
            for (int i = 0; i < allRecords.size(); i++) {
                Map<String, String> record = allRecords.get(i);
                jsonResponse.append("{");
                int columnCount = 0;
                for (Map.Entry<String, String> entry : record.entrySet()) {
                    jsonResponse.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                    if (++columnCount < record.size()) {
                        jsonResponse.append(",");
                    }
                }
                jsonResponse.append("}");
                if (i < allRecords.size() - 1) {
                    jsonResponse.append(",");
                }
            }
            jsonResponse.append("]");

            jsonResponse.append("}");
            out.write(jsonResponse.toString());

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



/*import jakarta.servlet.ServletException;
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

@WebServlet("/FetchDynamicDBServlet")
public class FetchDynamicDBServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String tableName = request.getParameter("table_name");
        tableName = tableName != null ? tableName.toLowerCase() : "";
            Integer user_ID = (Integer)session.getAttribute("user_ID");
        if (tableName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Table name is missing or invalid.\"}");
            return;
        }

        Map<String, ArrayList<String>> enumColumns = new HashMap<>();
        ArrayList<String> columnNames = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            String columnQuery = "SELECT c.column_name, t.typname, e.enumlabel " +
                                 "FROM information_schema.columns c " +
                                 "LEFT JOIN pg_type t ON c.udt_name = t.typname " +
                                 "LEFT JOIN pg_enum e ON t.oid = e.enumtypid " +
                                 "WHERE c.table_name = ? " +
                                 "ORDER BY c.ordinal_position, e.enumsortorder";

            PreparedStatement columnStmt = conn.prepareStatement(columnQuery);
            columnStmt.setString(1, tableName);
            ResultSet columns = columnStmt.executeQuery();

            while (columns.next()) {
                String columnName = columns.getString("column_name");
                String enumLabel = columns.getString("enumlabel");

                if (!columnNames.contains(columnName)) {
                    columnNames.add(columnName);
                }

                if (enumLabel != null) {
                    enumColumns.computeIfAbsent(columnName, k -> new ArrayList<>()).add(enumLabel);
                }
            }

            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");

            jsonResponse.append("\"data\":{");
            for (int i = 0; i < columnNames.size(); i++) {
                jsonResponse.append("\"").append(columnNames.get(i)).append("\":null");
                if (i < columnNames.size() - 1) {
                    jsonResponse.append(",");
                }
            }
            jsonResponse.append("},");

            jsonResponse.append("\"enum\":{");
            int enumCount = 0;
            for (Map.Entry<String, ArrayList<String>> entry : enumColumns.entrySet()) {
                jsonResponse.append("\"").append(entry.getKey()).append("\":[");
                ArrayList<String> values = entry.getValue();
                for (int j = 0; j < values.size(); j++) {
                    jsonResponse.append("\"").append(values.get(j)).append("\"");
                    if (j < values.size() - 1) {
                        jsonResponse.append(",");
                    }
                }
                jsonResponse.append("]");
                if (++enumCount < enumColumns.size()) {
                    jsonResponse.append(",");
                }
            }
            jsonResponse.append("}");
            jsonResponse.append("}");
            out.write(jsonResponse.toString());

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
*/