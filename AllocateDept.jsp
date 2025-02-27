<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% 
    String deptName = request.getParameter("dept_name");
    int deptCount = Integer.parseInt(request.getParameter("dept_count"));
%>

<!DOCTYPE html>
<html>
<head>
    <title>Allocate Employees to Department</title>
</head>
<body>
    <h2>Allocate Employees to Department: <%= deptName %></h2>

    <form id="employeeDetailsForm" action="InsertEmployeeServlet" method="POST">
        <input type="hidden" name="dept_name" value="<%= deptName %>">
        
        <% for (int i = 1; i <= deptCount; i++) { %>
            <div>
                <label>User Email:</label>
                <input type="email" name="user_email_<%= i %>" required>
                
                <label>Position:</label>
                <input type="text" name="position_<%= i %>" required>
            </div>
            <br>
        <% } %>
        
        <button type="submit">Submit</button>
    </form>
</body>
</html>
