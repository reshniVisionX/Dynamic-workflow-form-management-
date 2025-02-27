<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Profile Page</title>
    <style>
        table {
            width: 50%;
            border-collapse: collapse;
            margin: 20px auto;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color:  #0039e6;
            font-weight: bold;
			color:white;
			font-weight:bold;
        }
        tr:hover {
            background-color: #e6ecff;
        }
    </style>
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

    String name = (String) request.getAttribute("username");
    Long contact = (Long) request.getAttribute("contact");
    String dob = (String) request.getAttribute("dob");
    String jobRole = (String) request.getAttribute("jobRole");
    Double salary = (Double) request.getAttribute("salary");
    String address = (String) request.getAttribute("address");
%>

<h2 style="text-align: center;">Employee Profile</h2>
<table>
    <tr>
        <th>Field</th>
        <th>Details</th>
    </tr>
    <tr>
        <td>Name</td>
        <td><%= name %></td>
    </tr>
    <tr>
        <td>Job Role</td>
        <td><%= jobRole %></td>
    </tr>
    <tr>
        <td>Contact</td>
        <td><%= contact %></td>
    </tr>
    <tr>
        <td>DOB</td>
        <td><%= dob %></td>
    </tr>
    <tr>
        <td>Salary</td>
        <td><%= salary %></td>
    </tr>
    <tr>
        <td>Address</td>
        <td><%= address %></td>
    </tr>
</table>

</body>
</html>
