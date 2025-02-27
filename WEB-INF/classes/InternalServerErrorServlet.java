import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class InternalServerErrorServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processError(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest request, HttpServletResponse response) throws IOException {
      
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

      
        out.println("<html><head><title>500 Internal Server Error...! </title></head><body>");
        out.println("<h1>Oops! Something went wrong.</h1>");
        out.println("<p>Try logging in again, session timed out!</p>");
        out.println("<a href='login.html'>Go to Login Page</a>"); 
        out.println("</body></html>");
    }
}
