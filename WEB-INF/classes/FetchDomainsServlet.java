import com.google.gson.Gson;
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
import java.util.List;
import java.util.Map;

@WebServlet("/FetchDomainsServlet")
public class FetchDomainsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String sql = "SELECT domain_name, 'domain' AS object_type, NULL::text[] AS enum_values "
                        + "FROM information_schema.domains "
                        + "UNION "
                        + "SELECT t.typname AS domain_name, 'type' AS object_type, NULL::text[] AS enum_values "
                        + "FROM pg_type t "
                        + "WHERE typtype = 'e' "
                        + "UNION "
                        + "SELECT t.typname AS domain_name, 'enum' AS object_type, array_agg(e.enumlabel::text) AS enum_values "
                        + "FROM pg_type t "
                        + "JOIN pg_enum e ON t.oid = e.enumtypid "
                        + "GROUP BY t.typname "
                        + "ORDER BY domain_name;";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

        
            List<Map<String, Object>> fields = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> field = new HashMap<>();
                field.put("domain_name", rs.getString("domain_name"));
                field.put("object_type", rs.getString("object_type"));
                
                Array enumValuesArray = rs.getArray("enum_values");
                if (enumValuesArray != null) {
                    field.put("enum_values", (String[]) enumValuesArray.getArray()); 
                } else {
                    field.put("enum_values", null); 
                }

                fields.add(field);
            }

            Gson gson = new Gson();
            out.print(gson.toJson(fields));

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            out.print("[]"); 
        }
    }
}
