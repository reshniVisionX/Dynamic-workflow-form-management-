<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Department</title>
    <style>
        
        #departmentForm {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 600px;
        }

       
        label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            color: #333;
        }

       
        input[type="text"], input[type="number"], input[type="submit"], button {
            width: 100%;
            padding: 10px;
            margin-bottom: 15px;
            border-radius: 4px;
            border: 1px solid #ccc;
            box-sizing: border-box;
            font-size: 14px;
        }

        input[type="text"]:focus, input[type="number"]:focus {
            border-color: #007BFF;
            outline: none;
        }

      
        button {
            background-color: #0000ff;
            color: white;
            border: none;
            cursor: pointer;
            font-size: 16px;
        }

        button:hover {
            background-color:#00bfff;
        }

      
        .error {
            color: red;
            font-size: 14px;
        }
		#dept_name,#dept_count{
			width:20%;
		}
		.btn-allo{
			width:20%;
			height:35px;
			text-align:center;
			font-size:16px;
		}
		.error-message{
			font-size:18px;
			text-align:center;
		}
		
        .success-message {
            color: green;
            font-size: 18px;
			text-align:center;
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
<br>
    <h2>Add Department</h2><br/>
    <form id="departmentForm" >
        <label for="deptName">Department Name:</label>
        <input type="text" id="deptName" name="dept_name" required><br><br>

        <label for="deptHead">Department Head:</label>
        <input type="text" id="deptHead" name="dept_head" required><br><br>

        <label for="deptPriority">Department Priority:</label>
        <input type="number" id="deptPriority" name="dept_priority" required><br><br>

       <label for="deptStatus">Department Status:</label>
<select id="deptStatus" name="dept_status" required>
    
    <option value="active">Active</option>
    <option value="inactive">Inactive</option>
</select>
<br><br>

        <button type="submit">Add Department</button>
		
    </form>
	<br>
	<br>
	<div class="error-message" ></div>
	<br><br>
	
<h2> Allocate Department to employees </h2>
	
	<form id="employee" action="DragDropEmp.jsp" method="POST"> 
    <label for="dept_name">Select the Department:</label>
    <select id="dept_name" name="dept_name" required>
        <option value="" disabled selected>Select a department</option>
    </select><br><br>

    <label for="dept_count">Enter the count:</label>
    <input type="number" id="dept_count" name="dept_count" required><br><br>
    <button class="btn-allo" type="submit">Allocate</button>
</form>


</body>

<script>

 document.addEventListener('DOMContentLoaded', function () {
    fetchDepartments(); 
});

function fetchDepartments() {
    fetch('/EmployeeManagement/GetDepartmentsServlet')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            const deptDropdown = document.getElementById('dept_name');
            deptDropdown.innerHTML = '<option value="" disabled selected>Select a department</option>'; 

            const departments = data.departments;

            if (departments && departments.length > 0) {
                departments.forEach(department => {
                    const option = document.createElement('option');
                    option.value = department.dept_name; 
                    option.textContent = department.dept_name; 
                    deptDropdown.appendChild(option);
                });
            } else {
                console.error('No departments found in the response.');
            }
        })
        .catch(error => {
            console.error('Error fetching departments:', error);
            const deptDropdown = document.getElementById('dept_name');
            deptDropdown.innerHTML = '<option value="" disabled selected>Error loading departments</option>';
        });
}


    $(document).ready(function () {
      
        $('#departmentForm').on('submit', function (e) {
            e.preventDefault(); 

            const formData = $(this).serialize(); 

            $.ajax({
                url: 'InsertDeptServlet', 
                type: 'POST',
                data: formData,
                success: function (response) {
                   
                    $('.error-message').empty().removeClass('error success-message');

                    if (response.status === 'success') {
                        $('.error-message').addClass('success-message').text(response.message);
                        $('#departmentForm')[0].reset(); 
						 fetchDepartments(); 
                    } else {
                        $('.error-message').addClass('error').text(response.message);
                    }
                },
                error: function () {
                    $('.error-message').addClass('error').text('An error occurred while processing your request.');
                }
            });
        });
    });
</script>
</html>
