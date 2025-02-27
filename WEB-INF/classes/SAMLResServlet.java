import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.Init;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import jakarta.servlet.RequestDispatcher;

@WebServlet("/saml/consume")
public class SAMLResServlet extends HttpServlet {
    static {
        try {
            Init.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
				 response.setContentType("text/plain");
        try {
            System.out.println("Starting SAML response validation process...");

     
            String samlResponse = request.getParameter("SAMLResponse");
            if (samlResponse == null || samlResponse.isEmpty()) {
                System.out.println("SAMLResponse is missing");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "SAMLResponse is missing");
				 response.getWriter().write("Saml response is missing");
                return;
            }
            System.out.println("Received SAMLResponse. Proceeding with Base64 decoding...");

            byte[] decodedBytes = Base64.getDecoder().decode(samlResponse);
            System.out.println("Base64 decoding completed. Decoded data length: " + decodedBytes.length);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); 
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(decodedBytes));
            System.out.println("XML parsed successfully.");
			
			NodeList statusCodeList = document.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:protocol", "StatusCode");
            if (statusCodeList.getLength() > 0) {
                Element statusCodeElement = (Element) statusCodeList.item(0);
                String value = statusCodeElement.getAttribute("Value");

                if(!("urn:oasis:names:tc:SAML:2.0:status:Success".equals(value))){
					 response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Status code from the response ");
					 response.getWriter().write("Response status code isnt success");
                      return;
				}else{
					System.out.println("-------status code is success--------");
				}
            }

            NodeList signatureNodes = document.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
            if (signatureNodes.getLength() == 0) {
                System.out.println("No Signature elements found in the SAML response.");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No Signature elements found.");
				 response.getWriter().write("No signature element found");
                return;
            }
            System.out.println("Found " + signatureNodes.getLength() + " Signature element(s).");
			
             FileInputStream certInputStream = new FileInputStream("S:/Instant Install/okta.cert");
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate x509Certificate = (X509Certificate) certFactory.generateCertificate(certInputStream);
                PublicKey publicKey = x509Certificate.getPublicKey();
                System.out.println("Public key loaded successfully.");
				
            for (int i = 0; i < signatureNodes.getLength(); i++) {
                Element signatureElement = (Element) signatureNodes.item(i);
                System.out.println("Processing Signature element " + (i + 1));

                Element signedInfoElement = (Element) signatureElement.getElementsByTagName("ds:SignedInfo").item(0);
                Element signatureValueElement = (Element) signatureElement.getElementsByTagName("ds:SignatureValue").item(0);
                Element keyInfoElement = (Element) signatureElement.getElementsByTagName("ds:KeyInfo").item(0);

                if (signedInfoElement == null || signatureValueElement == null || keyInfoElement == null) {
                    System.out.println("Incomplete signature element. Missing SignedInfo, SignatureValue, or KeyInfo.");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature structure.");
					 response.getWriter().write("Invalid signature structure");
                    return;
                } else {
                    System.out.println("signedInfoElement, signatureValueElement, keyInfoElement extracted successfully");
                }

                String canonicalSignedInfo = canonicalize(signedInfoElement);
                byte[] signedInfoBytes = canonicalSignedInfo.getBytes("UTF-8");
                System.out.println("---Canonicalized SignedInfo: " + canonicalSignedInfo);

                byte[] signatureValueBytes = Base64.getDecoder().decode(signatureValueElement.getTextContent());
               
                if (verifySignature(publicKey, signedInfoBytes, signatureValueBytes)) {
                    System.out.println("****Signature " + (i + 1) + " is valid.***");
					System.out.println();
                } else {
                    System.out.println("Signature " + (i + 1) + " is invalid.");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature.");
					 response.getWriter().write("Invalid signature");
                    return;
                }
            }

            System.out.println("Checking SAML conditions...");
            if (!validateSAMLConditions(document)) {
                System.out.println("SAML conditions validation failed.");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "SAML conditions validation failed.");
				 response.getWriter().write("SAML validation conditions fails");
                return;
            }else{
				NodeList attributeNodes = document.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Attribute");
                 String email = "", peoplename = "";

                for (int i = 0; i < attributeNodes.getLength(); i++) {
                  Element attribute = (Element) attributeNodes.item(i);
                  String name = attribute.getAttribute("Name");
                  NodeList valueNodes = attribute.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "AttributeValue");
                    if (valueNodes.getLength() > 0) {
                     String value = valueNodes.item(0).getTextContent();
                     System.out.println(name + ": " + value);
                     if ("email".equals(name)) {
                         email = value;
                     } else if ("name".equals(name)) {
                      peoplename = value;
                     }
                    }
                }

             System.out.println("The logged-in person's name: " + peoplename + " email: " + email);

             String redirectUrl = "/SAMLLoginServlet?personname=" + java.net.URLEncoder.encode(peoplename, "UTF-8") +
                     "&personemail=" + java.net.URLEncoder.encode(email, "UTF-8");
                    RequestDispatcher dispatcher = request.getRequestDispatcher(redirectUrl);
                    dispatcher.forward(request, response);
			}

        } catch (Exception e) {
            System.out.println("Error processing SAML response: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing SAML response.");
			 response.getWriter().write("Caught error while processing the saml response.");
        }
    }

    private String canonicalize(Element element) throws Exception {
        try {
      
            Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            byte[] canonicalizedBytes = canon.canonicalizeSubtree(element);
            return new String(canonicalizedBytes, "UTF-8");
        } catch (Exception e) {
            System.out.println("Error during canonicalization: " + e.getMessage());
            throw e;
        }
    }

   private boolean verifySignature(PublicKey publicKey, byte[] signedInfoBytes, byte[] signatureValueBytes) throws Exception {
    try {
       
        System.out.println("Public Key Algorithm: " + publicKey.getAlgorithm());
        System.out.println("Public Key Format: " + publicKey.getFormat());
        System.out.println("---Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));

       
        System.out.println("---Signed Info Bytes (Base64 Encoded): " + Base64.getEncoder().encodeToString(signedInfoBytes));
      
   
        System.out.println("---Signature Value Bytes (Base64 Encoded):" + Base64.getEncoder().encodeToString(signatureValueBytes));
      
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(signedInfoBytes);

        boolean isValid = signature.verify(signatureValueBytes);
        System.out.println("---Signature verification result:-- \"" + isValid+" \"");

        return isValid;
    } catch (Exception e) {
        System.out.println("Error during signature verification: " + e.getMessage());
        throw e;
    }
}

private boolean validateSAMLConditions(Document document) {
    try {
        
        NodeList conditionsNodes = document.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Conditions");
        if (conditionsNodes.getLength() == 0) {
            System.out.println("No Conditions element found.");
            return false;
        }
        Element conditionsElement = (Element) conditionsNodes.item(0);

        String notBeforeStr = conditionsElement.getAttribute("NotBefore");
        String notOnOrAfterStr = conditionsElement.getAttribute("NotOnOrAfter");

        if (notBeforeStr.isEmpty() || notOnOrAfterStr.isEmpty()) {
            System.out.println("Conditions element is missing NotBefore or NotOnOrAfter attributes.");
            return false;
        }

        java.time.Instant notBefore = java.time.Instant.parse(notBeforeStr);
        java.time.Instant notOnOrAfter = java.time.Instant.parse(notOnOrAfterStr);
        java.time.Instant currentTime = java.time.Instant.now();

        System.out.println("NotBefore: " + notBefore);
        System.out.println("NotOnOrAfter: " + notOnOrAfter);
        System.out.println("CurrentTime: " + currentTime);

        if (currentTime.isBefore(notBefore) || !currentTime.isBefore(notOnOrAfter)) {
            System.out.println("Current time is outside the valid time range.");
            return false;
        }

        System.out.println("-----Current time is within the valid time range.-----");

       
        NodeList audienceRestrictionNodes = document.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "AudienceRestriction");
        if (audienceRestrictionNodes.getLength() == 0) {
            System.out.println("No AudienceRestriction element found.");
            return false;
        }
        Element audienceRestrictionElement = (Element) audienceRestrictionNodes.item(0);

      
        NodeList audienceNodes = audienceRestrictionElement.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Audience");
        if (audienceNodes.getLength() == 0) {
            System.out.println("No Audience element found inside AudienceRestriction.");
            return false;
        }

        String audience = audienceNodes.item(0).getTextContent();
        String expectedAudience = "http://localhost:8080/EmployeeManagement";
        System.out.println("Audience: " + audience);
        System.out.println("Expected Audience: " + expectedAudience);

        if (!audience.equals(expectedAudience)) {
            System.out.println("Audience validation failed.");
            return false;
        }

        System.out.println("------Audience validation matches.------");
        return true;

    } catch (Exception e) {
        System.out.println("Error validating SAML conditions: " + e.getMessage());
        return false;
    }
}

}
