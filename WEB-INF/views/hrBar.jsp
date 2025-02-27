<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HR Page</title>
    <style>
       
        * {
            margin: 10px;
            padding: 0;
            box-sizing: border-box;
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
    
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
        }

        .content {
            padding: 20px;
        }

        h1 {
            color: #333;
        }

        
        .search-container {
            margin: 20px 0;
            display: flex;
            justify-content: space-between;
        }

        .search-box {
            padding: 10px;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 5px;
            width: 48%;
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
		 .form-container {
            width: 400px;
            margin: 0 auto;
            padding: 20px;
            border: 1px solid #ccc;
            border-radius: 5px;
            background-color: #f9f9f9;
        }
        .form-container h2 {
            text-align: center;
        }
		.btn{
			width:20%;
			height:30px;
			border:2px solid black;
			margin-bottom:5px;
		}
		.text-box{
			width:50%;
			height:30px;
		}
		.btn-csv{
			width:25%;
			height:30px;
		}
		 .button-container {
            display: flex; 
            gap: 10px; 
        }
        .button-container a {
             text-decoration:none;
            padding: 10px 20px; 
            background-color: #000066; 
            color: white; 
            border: 2px solid gray; 
            border-radius: 5px; 
            font-size: 16px; 
            text-align: center; 
            display: inline-block; 
            cursor: pointer; 
            transition: background-color 0.3s ease; 
        }
        .button-container a:hover {
            background-color: white;
			color:#0000ff;
        }
    </style>
</head>
<body>

    <div class="navbar">
        <a href="hrPage.jsp">Home</a>
        <a href="manageEmployee.jsp">Manage Employee</a>
		<a href="department.jsp">Departments</a>
		<a href="viewforms.jsp">View Forms</a>
        <a href="forms.jsp">Create Forms</a>
        <a href="ProfileServlet">MyProfile</a>
        <a href="logout.jsp" class="logout logout-btn">Logout</a>
    </div>
    <div class="button-container">
        <a href="workflowProcess.jsp">WorkFlow Update</a>
        <a href="workflowProgress.jsp">WorkFlow in Process</a>
    </div>
	<br><br>
</body>
</html>
