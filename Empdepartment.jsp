<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Department Details</title>
<style>
  table {
            width: 50%;
            border-collapse: collapse;
            margin: 20px auto;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 9px;
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
    
%>

<%
 Integer dept_id = (Integer) request.getAttribute("dept_id");
 String dept_name = (String) request.getAttribute("dept_name");
 String dept_head = (String) request.getAttribute("dept_head");
 String position = (String) request.getAttribute("position");
 Integer dept_priority = (Integer) request.getAttribute("dept_priority");
  
  if (dept_id == null || dept_name == null || dept_head == null || position == null || dept_priority == null) {
        out.println("Error: Missing data.");
        return; 
    }
%>
<h2 style="text-align: center;">Department Details</h2>
<table>

<tr>
<th>Fields</th>
<th>Details</th>
</tr>

<tr>
<td><b>Department Id :</b></td>
<td><h3><%= dept_id %></h3></td>
</tr>
<tr>
<td><b>Department Name :</b></td>
<td><h3><%= dept_name %></h3></td>
</tr>
<tr>
<td><b>Department Head :</b></td>
<td><h3><%= dept_head %></h3></td>
</tr>
<tr>
<td><b>Department Position :</b></td>
<td><h3><%= position %></h3></td>
</tr>
<tr>
<td><b>Department Priority :</b></td>
<td><h3><%= dept_priority %></h3></td>
</tr>

</table>

</body>

</html>