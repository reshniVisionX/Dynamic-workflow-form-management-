<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Page</title>
 
</head>
<body>

   <%
    
    int role = (int)session.getAttribute("role");
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
    
%>

    <div class="content">
        <h2>Welcome to the Admin Page</h1>
       <br/>

        <div class="username">
            <h4>Welcome, <%= session.getAttribute("username") != null ? session.getAttribute("username") : "Guest" %></h2>
        </div>
    </div>

</body>
</html>
