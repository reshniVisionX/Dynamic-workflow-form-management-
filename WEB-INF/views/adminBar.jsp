<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Page</title>
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
            width: 55%;
			height:30px;
        }

        .dropdown{
			width:20px;
			height:30px;
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
        <a href="adminPage.jsp">Home</a>
        <a href="manageHr.jsp">Manage HR</a>
        <a href="manageEmployee.jsp">Manage Employee</a>
		<a href="viewforms.jsp">View Forms</a>
        <a href="forms.jsp">Create Forms</a>
		
        <a href="ProfileServlet">MyProfile</a>
        <a href="logout.jsp" class="logout logout-btn">Logout</a>
    </div>

    
    

</body>
</html>
