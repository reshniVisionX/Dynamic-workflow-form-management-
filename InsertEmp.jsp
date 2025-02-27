<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add HR</title>
    <style>
          body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 20px;
        }

        h1, h2 {
            color: #333;
            text-align: left;
        }

        .form-container {
            background-color: #fff;
            padding: 20px;
            margin: 20px 0;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 600px;
        }

        form {
            margin: 0;
            padding: 0;
        }

        label {
            font-size: 14px;
            color: #333;
            margin-bottom: 6px;
            display: block;
        }

        input[type="text"],
        input[type="email"],
        input[type="password"],
        input[type="number"],
        input[type="date"],
        select,
        textarea,
        input[type="file"] {
            width: 100%;
            padding: 10px;
            margin: 8px 0;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }

        textarea {
            resize: vertical;
        }

        input[type="submit"],
        .btn-csv {
            background-color: #007BFF;
            color: white;
            border: none;
            padding: 5px;
			text-align:center;
            font-size: 16px;
            border-radius: 4px;
            cursor: pointer;
            width: 100%;
            margin-top: 10px;
        }

        input[type="submit"]:hover,
        .btn-csv:hover {
            background-color: #0056b3;
        }

        .file-input {
            padding: 5px;
        }
		.form-emp{
			width:45%;
			padding:15px;
			border:2px solid #1E137E;
			background-color:#FFFFFF;
			box-shadow:2px 5px solid gray;
			margin-left:5%;
		}
    </style>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
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

<h1>Enter the Details to Add</h1>

<div class="form-container">
    <h2>Upload CSV File</h2>
    <form action="AddCSVEmpServlet" method="POST" enctype="multipart/form-data">
        <label for="csvFile">Select CSV File:</label>
        <input class="file-input" type="file" id="csvFile" name="csvFile" accept=".csv" required>
        <input class="btn-csv" type="submit" value="Upload CSV">
    </form>
</div>

<br><br>
<h2>Add Manually</h2>

<form id="addEmployeeForm" class="form-emp">
    <label for="username">Username:</label><br>
    <input type="text" id="username" name="username" required><br><br>

    <label for="user_email">Email:</label><br>
    <input type="email" id="user_email" name="user_email" required><br><br>

    <label for="user_password">Password:</label><br>
    <input type="password" id="user_password" name="user_password" required><br><br>

    <label for="contact">Contact:</label><br>
    <input type="text" id="contact" name="contact" required><br><br>

    <label for="dob">Date of Birth:</label><br>
    <input type="date" id="dob" name="dob" required><br><br>

    <label for="job_role">Job Role:</label><br>
    <input type="text" id="job_role" name="job_role" required><br><br>

    <label for="salary">Salary:</label><br>
    <input type="number" id="salary" name="salary" required><br><br>

    <label for="address">Address:</label><br>
    <textarea id="address" name="address" rows="4" required></textarea><br><br>

    <label for="role_id">Role Id:</label><br>
    <select id="role_id" name="role_id" required>
        <option value="3">Employee</option>
    </select><br><br>

    <button type="submit" style="background-color: #007BFF; color: white;padding:10px">Add Employee</button>
</form>
<br><br>

<div id="error-message" style="color: red; font-weight: bold;"></div> 

<br><br>
<script>
$(document).ready(function () {
    $('#addEmployeeForm').on('submit', function (e) {
        e.preventDefault(); 

        $.ajax({
            url: 'insertHr', 
            type: 'POST',
            data: $(this).serialize(), 
            dataType: 'json',
            success: function (response) {
               
                if (response.status === 'error') {
                    
                    document.getElementById('error-message').innerText = response.message;
                } else {
                   
                    alert('Employee added successfully!');
                    location.reload();
                }
            },
            error: function (xhr) {
                document.getElementById('error-message').innerText = 'An unexpected error occurred: ' + xhr.statusText;
            }
        });
    });
});


</script>

</body>
</html>
