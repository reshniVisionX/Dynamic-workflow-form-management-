
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add HR</title>
	<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

 <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f4f4f4;
          
        }

        h1 {
            text-align: center;
            color: #333;
        }

        form {
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 600px;
            box-sizing: border-box;
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
        textarea {
            width: 70%;
            padding: 10px;
            margin: 8px 0;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }

        textarea {
            resize: vertical;
        }

        input[type="submit"] {
            background-color:  #0040ff;
            color: white;
            border: none;
            padding: 10px 20px;
            font-size: 16px;
            border-radius: 4px;
            cursor: pointer;
            width: 68%;
            margin-top: 10px;
        }

        input[type="submit"]:hover {
            background-color: #45a049;
        }

        .form-container {
            width: 100%;
            max-width: 400px;
            margin: 20px;
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
<br>
<h1>Enter the Details to add HR</h1>
<br>
<form id="addHrForm" >
    <label for="username">Username:</label><br>
    <input type="text" id="username" name="username" placeholder="Enter name" required><br><br>

    <label for="user_email">Email:</label><br>
    <input type="email" id="user_email" name="user_email" placeholder="Enter email"  required><br><br>

    <label for="user_password">Password:</label><br>
    <input type="password" id="user_password" name="user_password" placeholder="Enter password" required><br><br>

    <label for="contact">Contact:</label><br>
    <input type="text" id="contact" name="contact" required><br><br>

    <label for="dob">Date of Birth:</label><br>
    <input type="date" id="dob" name="dob" required><br><br>

    <label for="job_role">Job Role:</label><br>
    <input type="text" id="job_role" name="job_role" value="HR Specialist" placeholder="Enter role" required><br><br>

    <label for="salary">Salary:</label><br>
    <input type="number" id="salary" name="salary" placeholder="Enter salary"  required><br><br>

    <label for="address">Address:</label><br>
    <textarea id="address" name="address" rows="4" required></textarea><br><br>

   
	<label for="role_id">Role Id:</label><br>
<select id="role_id" name="role_id" required>
    <option value="2">HR</option>
    <option value="3">Employee</option>
</select><br><br>
    <button type="submit" >Add HR </button>
</form>

<br><br>

<div id="error-message" style="color: red; font-weight: bold;"></div> 

<br><br>

<script>
  $(document).ready(function () {
    $('#addHrForm').on('submit', function (e) {
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
                   
                    alert('HR added successfully!');
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
