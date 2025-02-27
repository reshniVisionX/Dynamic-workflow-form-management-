import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.nio.file.Paths;

public class CSVExportServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       
        String tableName = request.getParameter("table_name");
        
        if (tableName == null || tableName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Table name is required");
            return;
        }

        String csvFilePath = "S:/Zoho/CSV_files" + tableName + ".csv"; 

        Connection connection = null;
        try {
           
            connection = DBConnection.getInstance().getConnection();

            String copyQuery = "COPY " + tableName + " TO '" + csvFilePath + "' DELIMITER ',' CSV HEADER";

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(copyQuery);
            }

            File csvFile = new File(csvFilePath);
            if (csvFile.exists()) {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=" + tableName + ".csv");

                try (FileInputStream fileInputStream = new FileInputStream(csvFile);
                     OutputStream out = response.getOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fileInputStream.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV file generation failed");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error executing COPY command: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error streaming file: " + e.getMessage());
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
