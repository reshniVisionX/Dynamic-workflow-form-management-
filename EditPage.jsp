<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<html>
<head>
    <title>Edit HR</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<style>
 
    body {
        font-family: Arial, sans-serif;
        background-color: #f4f4f9;
        margin: 0;
        padding: 0;
    }

    h2 {
        text-align: center;
        margin: 20px 0;
        color: #333;
    }

    form {
        width: 50%;
        margin: 0 auto;
        background: #fff;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }

    table {
        width: 100%;
        margin: 10px 0;
    }

    table td {
        padding: 10px;
        font-size: 14px;
        color: #555;
    }

    input[type="text"], 
    input[type="email"], 
    input[type="date"], 
    input[type="number"], 
    button {
        width: 70%;
        padding: 8px 10px;
        font-size: 14px;
        margin: 5px 0;
        border: 1px solid #ccc;
        border-radius: 4px;
    }

    input:disabled {
        background-color: #f0f0f0;
        color: #888;
        cursor: not-allowed;
    }

    button {
        background-color: #007BFF;
        color: white;
        border: none;
        cursor: pointer;
        padding: 10px 20px;
        font-size: 14px;
    }

    button:hover {
        background-color: #0056b3;
    }

    button:focus {
        outline: none;
    }

    tr td:first-child {
        text-align: right;
        font-weight: bold;
    }

    .error-message {
        text-align: center;
        color: red;
        font-weight: bold;
    }

    .success-message {
        text-align: center;
        color: green;
        font-weight: bold;
    }
</style>
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
<br>
<%
    String hrId = request.getParameter("hrId");
    session.setAttribute("user_id", hrId);
%>
<h2>Update Details for Employee ID: <%= hrId %></h2>

<script>
    $(document).ready(function () {
        var user_id = "<%= session.getAttribute("user_id") %>";

        function loadHRData() {
            $.ajax({
                url: 'fetchData',
                type: 'GET',
                data: { role_id: 2, user_id: user_id },
                success: function (response) {
                    if (response && response.length > 0) {
                        const user = response[0];
                        $('#username').val(user[0]);
                        $('#user_email').val(user[2]);
                        $('#contact').val(user[4]);
                        $('#dob').val(user[5]);
                        $('#job_role').val(user[6]);
                        $('#salary').val(user[7]);
                        $('#address').val(user[8]);
                        $('#status').val(user[10]);
                    }
                },
                error: function (error) {
                    console.error('Error fetching HR data:', error);
                }
            });
        }

        loadHRData();

        $('#editForm').on('submit', function (e) {
            e.preventDefault();

            const formData = $(this).serialize();
            $.ajax({
                url: 'editHr',
                type: 'POST',
                data: formData,
                success: function (response) {
                    if (response.status === 'success') {
                        $('.error-message').hide();
                        $('.success-message').text(response.message).show();
                    } else {
                        $('.success-message').hide();
                        $('.error-message').text(response.message).show();
                    }
                },
                error: function (xhr) {
                    $('.success-message').hide();
                    $('.error-message').text('An unexpected error occurred.').show();
                }
            });
        });
    });
</script>

<form id="editForm">
    <input type="hidden" name="hrId" value="<%= hrId %>">
    <table>
        <tr>
            <td>Username</td>
            <td><input type="text" id="username" name="username" disabled></td>
        </tr>
        <tr>
            <td>User Email</td>
            <td><input type="email" id="user_email" name="user_email" disabled></td>
        </tr>
        <tr>
            <td>Contact</td>
            <td><input type="text" id="contact" name="contact" required></td>
        </tr>
        <tr>
            <td>Date of Birth</td>
            <td><input type="date" id="dob" name="dob" required></td>
        </tr>
        <tr>
            <td>Job Role</td>
            <td><input type="text" id="job_role" name="job_role" required></td>
        </tr>
        <tr>
            <td>Salary</td>
            <td><input type="number" id="salary" name="salary" required></td>
        </tr>
        <tr>
            <td>Address</td>
            <td><input type="text" id="address" name="address" required></td>
        </tr>
        <tr>
            <td>Status</td>
            <td><input type="text" id="status" name="status" required></td>
        </tr>
    </table>
    <button type="submit">Save Changes</button>
</form>

<div class="error-message" style="display:none;"></div>
<div class="success-message" style="display:none;"></div>

</body>
</html>
