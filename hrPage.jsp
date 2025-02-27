<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HR Page</title>
  
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
		    <%@ include file="/notification.jsp" %>
			
<%
        } else {
%>
            <%@ include file="/WEB-INF/views/employeeBar.jsp" %>
<%
        }
    
%>
  
 
 
    <div class="content">
        <h1>Welcome to the HR Page</h1>
       
        <div class="username">
            <h2>Welcome, <%= session.getAttribute("username") != null ? session.getAttribute("username") : "Guest" %></h2>
        </div>
      
       
    </div>
	
	<h2>Click to convert the below table to CSV</h2>
	 <form action="ExportCSVServlet" method="post">
        <input type="hidden" name="table_name" value="department">
        <button class="btn" type="submit">Department</button>
    </form>

  
    <form action="CSVExportServlet" method="post">
        <input type="hidden" name="table_name" value="forms">
        <button class="btn" type="submit">Forms</button>
    </form>
	
	<br/><br/><br/>
	
	<div class="form-container">
    <h2>Upload CSV File</h2>
    <form action="UploadCSVServlet" method="POST">
        <label for="filePath">Enter File Path (on Server):</label>
        <input class="text-box" type="text" id="filePath" name="filePath" placeholder="Enter file path on server" required>
        <input class="btn-csv" type="submit" value="Upload CSV">
    </form>
</div>
    
</body>
</html>
