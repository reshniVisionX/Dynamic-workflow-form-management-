import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SamlReqServlet")
public class SamlReqServlet extends HttpServlet {

    private static final String IDP_SSO_URL = "https://trial-5887870.okta.com/app/trial-5887870_employeemanagement_1/exkmpd104gv3kx6lc697/sso/saml";                                  
    private static final String SP_ENTITY_ID = "http://localhost:8080/EmployeeManagement";
    private static final String ACS_URL = "http://localhost:8080/EmployeeManagement/saml/consume";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("Sending SAML Authentication Request");
            
            String authnRequest = buildAuthnRequest(IDP_SSO_URL, SP_ENTITY_ID, ACS_URL);
            System.out.println("Generated AuthnRequest: " + authnRequest);

            String encodedSAMLRequest = Base64.getUrlEncoder().encodeToString(authnRequest.getBytes());
            System.out.println("Encoded SAML Request: " + encodedSAMLRequest);

            String redirectURL = IDP_SSO_URL + "?SAMLRequest=" + encodedSAMLRequest;
            System.out.println("Redirecting to: " + redirectURL);
            response.sendRedirect(redirectURL);
        } catch (Exception e) {
            log("Error generating SAML request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate SAML request");
        }
    }

    private String buildAuthnRequest(String idpSSOUrl, String spEntityId, String acsUrl) {
        String authnRequestID = "_" + UUID.randomUUID().toString();
        
        String xmlTemplate = 
            "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" " +
            "ID=\"" + authnRequestID + "\" " +
            "Version=\"2.0\" " +
            "IssueInstant=\"" + java.time.Instant.now() + "\" " +
            "Destination=\"" + idpSSOUrl + "\" " +
            "AssertionConsumerServiceURL=\"" + acsUrl + "\" " +
            "ForceAuthn=\"true\">" + 
            "<saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" + spEntityId + "</saml:Issuer>" +
            "</samlp:AuthnRequest>";

        return xmlTemplate;
    }
}

