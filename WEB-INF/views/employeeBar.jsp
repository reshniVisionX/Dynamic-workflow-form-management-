<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employee Page</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 10px;
            padding: 0;
        }
        .navbar {
            background-color: #000066;
            overflow: hidden;
        }
        .navbar a {
            float: left;
            display: block;
            color: white;
            text-align: center;
            padding: 14px 20px;
            text-decoration: none;
        }
        .navbar a:hover {
            background-color: #ddd;
            color: black;
        }
        .navbar .logout {
            float: right;
        }
        .content {
            padding: 20px;
        }
        h1 {
            color: #333;
        }
        .logout-btn {
            background-color: #f44336;
            color: white;
            padding: 10px 20px;
            border: none;
            cursor: pointer;
            text-align: center;
        }
        .logout-btn:hover {
            background-color: #e32d2d;
        }
    </style>
</head>
<body>
    <div class="navbar">
        <a href="employeePage.jsp">Home</a>
        <a href="FetchDeptServlet">Department</a>
        <a href="Empforms.jsp">Forms</a>
        <a href="ProfileServlet">MyProfile</a>
		<a href="InActionForms.jsp">My forms</a>
        <a href="logout.jsp" class="logout logout-btn">Logout</a>

        <form id="samlForm" action="SAMLLogoutServlet" method="POST">
       
        <button type="submit" class="btn">logout okta</button>
        </form>

        
    </div>
</body>
</html>
