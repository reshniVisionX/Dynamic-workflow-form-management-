import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import java.util.Base64;
import java.util.UUID;
import java.net.URLEncoder;

@WebServlet("/SAMLLogoutServlet")
public class SAMLLogoutServlet extends HttpServlet {

    private static final String IDP_LOGOUT_URL = "https://trial-5887870.okta.com";  
    private static final String SP_ENTITY_ID = "http://localhost:8080/EmployeeManagement";  
    private static final String LOGOUT_RELAY_STATE_URL = "http://localhost:8080/EmployeeManagement/login.html";  

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("--------------------------Sending SAML Logout Request------------------------------");

            String logoutRequest = buildLogoutRequest(IDP_LOGOUT_URL, SP_ENTITY_ID);
            System.out.println("Generated LogoutRequest: " + logoutRequest);

            String encodedSAMLRequest = Base64.getUrlEncoder().encodeToString(logoutRequest.getBytes());
			
            System.out.println("Encoded SAML Logout Request: " + encodedSAMLRequest);

            String redirectURL = IDP_LOGOUT_URL + "?SAMLRequest=" + encodedSAMLRequest + "&RelayState=" + URLEncoder.encode(LOGOUT_RELAY_STATE_URL, "UTF-8");
            System.out.println("Redirecting to: " + redirectURL);
            response.sendRedirect(redirectURL);
        } catch (Exception e) {
            log("Error generating SAML logout request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate SAML logout request");
        }
    }

    private String buildLogoutRequest(String idpLogoutUrl, String spEntityId) {
        String logoutRequestID = "_" + UUID.randomUUID().toString();

        String xmlTemplate = 
            "<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" " +
            "ID=\"" + logoutRequestID + "\" " +
            "Version=\"2.0\" " +
            "IssueInstant=\"" + java.time.Instant.now() + "\" " +
            "Destination=\"" + idpLogoutUrl + "\">" + 
            "<saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" + spEntityId + "</saml:Issuer>" +
            "<samlp:SessionIndex>" + UUID.randomUUID().toString() + "</samlp:SessionIndex>" + 
            "</samlp:LogoutRequest>";

        return xmlTemplate;
    }
}
