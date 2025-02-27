import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class InsertDomainServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        System.out.println("Entered Servlet");

      
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }

        JSONObject requestData;
        try {
            requestData = new JSONObject(json.toString());
        } catch (Exception e) {
            sendErrorResponse(out, "Invalid JSON format.");
            return;
        }

        System.out.println("Received JSON: " + requestData);

       
        String type = requestData.optString("type");
        String domainName = requestData.optString("domain").toLowerCase();

        if (type.isEmpty() || domainName.isEmpty()) {
            sendErrorResponse(out, "Missing required fields: 'type' or 'domain'.");
            return;
        }

        try (Connection conn = DBConnection.getInstance().getConnection()) {
           
            if (isDomainExists(conn, domainName)) {
                sendErrorResponse(out, "The custom type name '" + domainName + "' already exists.");
                return;
            }

            switch (type) {
                case "regex":
                    handleRegexType(requestData, conn, domainName, out);
                    break;

                case "enum":
                    handleEnumType(requestData, conn, domainName, out);
                    break;

                case "date_range":
                    handleDateRangeType(requestData, conn, domainName, out);
                    break;

                default:
                    sendErrorResponse(out, "Invalid type specified.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            sendErrorResponse(out, "Server error: " + e.getMessage());
        }
    }

    private boolean isDomainExists(Connection conn, String domainName) throws SQLException {
        String query = "SELECT COUNT(*) FROM information_schema.domains WHERE domain_name = ? " +
                       "UNION SELECT COUNT(*) FROM pg_type WHERE typname = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, domainName.toLowerCase());
            stmt.setString(2, domainName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

   private void handleRegexType(JSONObject requestData, Connection conn, String domainName, PrintWriter out) throws SQLException {
    String regexCheck = requestData.optJSONObject("details").optString("check");
    if (regexCheck.isEmpty()) {
        sendErrorResponse(out, "Missing 'check' value for regex type.");
        return;
    }
    System.out.println(domainName + " " + regexCheck);
    
    
    String escapedDomainName = domainName.replace("\"", "\"\"").toLowerCase();
    
 
    String sql = "CREATE DOMAIN \"" + escapedDomainName + "\" AS TEXT CHECK (VALUE ~ \'"+regexCheck+"\' )";
    System.out.println("Executing SQL: " + sql);
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
       
        stmt.executeUpdate();
        sendSuccessResponse(out, "Regex domain '" + domainName + "' created successfully.");
    }
}

private void handleDateRangeType(JSONObject requestData, Connection conn, String domainName, PrintWriter out) throws SQLException {
    JSONObject details = requestData.optJSONObject("details");
    String startDate = details.optString("start_date");
    String endDate = details.optString("end_date");

    if (startDate.isEmpty() || endDate.isEmpty()) {
        sendErrorResponse(out, "Missing start or end date for date range type.");
        return;
    }

    if (startDate.compareTo(endDate) >= 0) {
        sendErrorResponse(out, "Start date must be earlier than end date.");
        return;
    }

    String escapedDomainName = domainName.replace("\"", "\"\"");
    
    String sql = "CREATE DOMAIN \"" + escapedDomainName + "\" AS DATE CHECK (VALUE >= \'"+startDate+"\' AND VALUE <= \'"+endDate+"\')";
    System.out.println("Executing SQL: " + sql);
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.executeUpdate();
        sendSuccessResponse(out, "Date range domain '" + domainName + "' created successfully.");
    }
}

private void handleEnumType(JSONObject requestData, Connection conn, String domainName, PrintWriter out) throws SQLException {
    JSONObject details = requestData.optJSONObject("details");
       if (details == null || !details.has("enum_values") || details.getJSONArray("enum_values").length() == 0) {
        sendErrorResponse(out, "Enum values cannot be empty.");
        return;
    }

    JSONArray enumValuesArray = details.getJSONArray("enum_values");
  
    StringBuilder enumValues = new StringBuilder();
    
    for (int i = 0; i < enumValuesArray.length(); i++) {
        enumValues.append("'").append(enumValuesArray.getString(i)).append("'");
        if (i < enumValuesArray.length() - 1) {
            enumValues.append(", ");
        }
    }

    
    System.out.println("Enum Values: " + enumValues.toString());


    String sql = "CREATE TYPE \"" + domainName + "\" AS ENUM (" + enumValues.toString() + ")";
    System.out.println("Executing SQL: " + sql);

    try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate(sql);
        sendSuccessResponse(out, "Enum type '" + domainName + "' created successfully.");
    }
}

    private void sendErrorResponse(PrintWriter out, String message) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "error");
        jsonResponse.put("message", message);
        out.print(jsonResponse.toString());
    }

    private void sendSuccessResponse(PrintWriter out, String message) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "success");
        jsonResponse.put("message", message);
        out.print(jsonResponse.toString());
    }
}
