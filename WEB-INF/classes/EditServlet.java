import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.math.BigDecimal;
import org.json.JSONObject;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class EditServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();

        try {
       
            HttpSession session = request.getSession();
String userId = (String) session.getAttribute("user_id");

			System.out.println("The userid is "+userId);
            if (userId == null || userId.isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "User ID is required.");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            int userIdInt;
            try {
                userIdInt = Integer.parseInt(userId);
            } catch (NumberFormatException e) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid User ID format.");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            String contact = request.getParameter("contact");
            String dob = request.getParameter("dob");
            String jobRole = request.getParameter("job_role");
            String salary = request.getParameter("salary");
            String address = request.getParameter("address");
            String status = request.getParameter("status");

            StringBuilder error = new StringBuilder();
            boolean isValid = true;

            if (contact == null || !contact.matches("\\d{10}")) {
                error.append("Contact must be a 10-digit number.\n");
                isValid = false;
            }

            if (status == null || (!status.equalsIgnoreCase("active") && !status.equalsIgnoreCase("inactive"))) {
                error.append("Status must be 'active' or 'inactive'.\n");
                isValid = false;
            }

            BigDecimal salaryDecimal = null;
            if (salary != null) {
                try {
                    salaryDecimal = new BigDecimal(salary);
                    if (salaryDecimal.compareTo(new BigDecimal(10000)) < 0) {
                        error.append("Salary must be greater than 10,000.\n");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    error.append("Invalid salary format.\n");
                    isValid = false;
                }
            } else {
                error.append("Salary is required.\n");
                isValid = false;
            }

            Date dobDate = null;
            if (dob != null) {
                try {
                    dobDate = Date.valueOf(dob);
                } catch (IllegalArgumentException e) {
                    error.append("Invalid date of birth format.\n");
                    isValid = false;
                }
            } else {
                error.append("Date of Birth is required.\n");
                isValid = false;
            }

            if (address == null || address.isEmpty()) {
                error.append("Address is required.\n");
                isValid = false;
            }

            if (jobRole == null || jobRole.isEmpty()) {
                error.append("Job Role is required.\n");
                isValid = false;
            }

            if (!isValid) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", error.toString());
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            try (Connection conn = DBConnection.getInstance().getConnection()) {
                String userQuery = "UPDATE users SET status = ? WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(userQuery)) {
                    ps.setString(1, status);
                    ps.setInt(2, userIdInt);
                    if (ps.executeUpdate() == 0) {
                        jsonResponse.put("status", "error");
                        jsonResponse.put("message", "Failed to update user status.");
                        response.getWriter().write(jsonResponse.toString());
                        return;
                    }
                }

                String userDataQuery = "UPDATE userData SET contact = ?, dob = ?, job_role = ?, salary = ?, address = ? WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(userDataQuery)) {
                    ps.setLong(1, Long.parseLong(contact));
                    ps.setDate(2, dobDate);
                    ps.setString(3, jobRole);
                    ps.setBigDecimal(4, salaryDecimal);
                    ps.setString(5, address);
                    ps.setInt(6, userIdInt);

                    if (ps.executeUpdate() > 0) {
                        jsonResponse.put("status", "success");
                        jsonResponse.put("message", "User details updated successfully.");
                    } else {
                        jsonResponse.put("status", "error");
                        jsonResponse.put("message", "Failed to update user details.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Database error occurred.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred.");
        }

        response.getWriter().write(jsonResponse.toString());
    }
}
