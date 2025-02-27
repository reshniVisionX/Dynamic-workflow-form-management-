import java.io.File;
import java.io.IOException;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UploadCSVServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String filePath = request.getParameter("filePath");

        if (filePath == null || filePath.trim().isEmpty()) {
            response.getWriter().write("Please provide a valid file path.");
            return;
        }

        File csvFile = new File(filePath);
        if (!csvFile.exists() || !csvFile.canRead()) {
            response.getWriter().write("The file does not exist or cannot be read.");
            return;
        }

        try (Connection connection = DBConnection.getInstance().getConnection()) {

            String copyQuery = "COPY Forms (form_name, no_cols, dept_ids, expiry_date) FROM '" + csvFile.getAbsolutePath() + "' DELIMITER ',' CSV HEADER NULL AS ''";

          
            try (Statement statement = connection.createStatement()) {
                int rowsAffected = statement.executeUpdate(copyQuery);
                if (rowsAffected > 0) {
                    response.getWriter().write("CSV data uploaded and processed successfully.");
                } else {
                    response.getWriter().write("No data inserted.");
                }
            } catch (SQLException e) {
                response.getWriter().write("Error executing COPY command: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException | ClassNotFoundException e) {
            response.getWriter().write("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
