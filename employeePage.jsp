<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employee Page</title>
</head>
<style>
.wrk-btn{
	margin-left:30px;
	color:white;
	background-color:blue;
	height:30px;
	width:100px;
	border:2px solid gray;
	margin-top:30px;
}
.wrk-btn:hover{
	color:blue;
	background-color:white;
	
}
</style>
<body>
<%
    
    Integer role = (Integer) session.getAttribute("role"); 
    if (role != null) {
        if (role == 1) {
%>
            <%@ include file="/WEB-INF/views/adminBar.jsp" %>
<%
        } else if (role == 2) {
%>
            <%@ include file="/WEB-INF/views/hrBar.jsp" %>
<%
        } else {
%>
            <%@ include file="/WEB-INF/views/employeeBar.jsp" %>
<%
        }
    }

%>

  <div class="content">
    <%
        String email = (String) session.getAttribute("email");
        if (session != null) {
            String username = (String) session.getAttribute("username");

            if (username != null) {
                out.println("<h1>Welcome, " + username + "!</h1>");
            } else {
                out.println("<h1>User not logged in!</h1>");
            }
        } else {
            out.println("<h1>Session not found!</h1>");
        }
    %>
  </div>
  
  
 <%
    int user_ID = (int) session.getAttribute("user_ID");
    if (user_ID == 97) {
        out.println("User ID: " + user_ID);
%>
       
		<button type="button" class="wrk-btn" onclick="window.location.href='workflowProgress.jsp';">Workflow</button>

<%
    } else {
        out.println("<h3>Current User ID: " + user_ID + "</h3>");
    }
%>


<%@ include file="/notification.jsp" %>
</body>
</html>
