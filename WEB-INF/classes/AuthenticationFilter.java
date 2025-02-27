import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AuthenticationFilter implements Filter {
    public void init(FilterConfig config) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.endsWith("/LoginServlet") || requestURI.endsWith("/login.jsp") || requestURI.endsWith("/login.html") || requestURI.endsWith("/SamlReqServlet") ){ 
            chain.doFilter(request, response);  
            return;
        }
		 int role_id = (int)session.getAttribute("role");
		 System.out.println("The req role id is "+role_id);
		 String contextPath = httpRequest.getContextPath();
        String relativeURI = requestURI.substring(contextPath.length());

		if(role_id==3){
        System.out.println("Requested URI: " + requestURI);
        System.out.println("Relative URI: " + relativeURI);
			if(relativeURI.endsWith("/employeePage.jsp")  || relativeURI.endsWith("/Empforms.jsp") || relativeURI.endsWith("/FetchDeptServlet")  || relativeURI.endsWith("/FetchDynamicDBServlet") || relativeURI.endsWith("/ProfileServlet") || relativeURI.endsWith("/FetchEmpFormServlet") || relativeURI.endsWith("/FetchDBServlet") || relativeURI.endsWith("/DynamicInsertServlet") || relativeURI.endsWith("/logout.jsp") || relativeURI.endsWith("/FetchWorkflowData") ||  relativeURI.endsWith("/WorkflowTriggerServlet")  || relativeURI.endsWith("/workflowProgress.jsp") || relativeURI.endsWith("/NotificationServlet") || relativeURI.endsWith("/notification.jsp") || relativeURI.endsWith("/InActionForms.jsp") || relativeURI.endsWith("/EmpProgressWkflServlet")  || relativeURI.endsWith("/UpdateFormServlet")){
				  chain.doFilter(request, response);  
               return;
			}else{
				 httpResponse.sendRedirect("login.jsp?error=Please login first");
                  return;
			}
		
		} else if(role_id==2){
			if(relativeURI.endsWith("/adminPage.jsp") || relativeURI.endsWith("/manageHr.jsp") || relativeURI.endsWith("/Insert.jsp")){
				 httpResponse.sendRedirect("login.jsp?error=Please login first");
                  return;
			}
		}
      
        if (session == null || session.getAttribute("username") == null) {
            
            httpResponse.sendRedirect("login.jsp?error=Please login first");
            return;
        }else{
			System.out.println("You are authorized");
		}
        chain.doFilter(request, response);
    }

    public void destroy() {}
}
