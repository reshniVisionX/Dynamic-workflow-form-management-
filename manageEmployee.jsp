<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.Gson" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage HR</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>

h2 {
    color: #003366;
    text-align: left; 
    margin: 20px;
}

#filterForm {
    margin: 20px;
    text-align: left;
}

label {
    font-size: 14px;
    margin-right: 10px;
}

select, button {
    padding: 8px 12px;
    font-size: 14px;
    border-radius: 4px;
    border: 1px solid #007BFF;
    margin-right: 10px;
}

button {
    background-color: #007BFF;
    color: white;
    cursor: pointer;
}

button:hover {
    background-color: #0056b3; 
}


table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

th, td {
    padding: 12px;
    text-align: left;
    border: 1px solid #ddd;
}

th {
    background-color: #007BFF; 
    color: white;
}



tr:hover {
    background-color: #e0e0e0;
}


button {
    background-color: #28a745;
    border: none;
    padding: 6px 12px;
    font-size: 14px;
    color: white;
    cursor: pointer;
    border-radius: 4px;
}

button:hover {
    background-color: #218838; 
}

.add-employee-btn {
    display: inline-block;
    margin-top: 20px;
    padding: 10px 20px;
    background-color: #007BFF; 
    color: white;
    border: none;
    border-radius: 5px;
    font-size: 16px;
    text-decoration: none;
}

.add-employee-btn:hover {
    background-color: #0056b3; 
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


<h2>Employee Management</h2>

<form id="filterForm">
    <label for="statusFilter">Filter by Status:</label>
    <select name="statusFilter" id="statusFilter">
        <option value="">--Select--</option>
        <option value="active">Active</option>
        <option value="inactive">Inactive</option>
    </select>
    <button type="submit">Filter</button>
</form>

<br>

<% 
int userRoleId = 3; 
session.setAttribute("role_id", userRoleId); 
%>

<table id="hrTable">
    <thead>
        <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Email</th>
            <th>Contact</th>
            <th>Dob</th>
            <th>Job Role</th>
            <th>Salary</th>
            <th>Address</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>

<a href="InsertEmp.jsp">
    <button type="button" class="add-employee-btn">Add Employee</button>
</a>

<script>
$(document).ready(function() {
    function loadHRData(statusFilter = '') {
        $.ajax({
            url: 'fetchData', 
            type: 'GET',
            data: { 
                statusFilter: statusFilter, 
                role_id: 3
            },
            success: function(response) {
                $('#hrTable tbody').empty();
                 
                if (response.error) {
                    alert(response.error); 
                    return;
                }

                response.forEach(function(userDetails) {
                    var row = '<tr>' +
                        '<td>' + userDetails[1] + '</td>' + 
                        '<td>' + userDetails[0] + '</td>' +  
                        '<td>' + userDetails[2] + '</td>' +  
                        '<td>' + userDetails[4] + '</td>' +  
                        '<td>' + userDetails[5] + '</td>' +  
                        '<td>' + userDetails[6] + '</td>' +  
                        '<td>' + userDetails[7] + '</td>' +  
                        '<td>' + userDetails[8] + '</td>' +  
                        '<td>' + userDetails[10] + '</td>' + 
                        '<td>' +
                           '<form action="EditPage.jsp" method="get" style="display:inline;">' +
                                '<input type="hidden" name="hrId" value="' + userDetails[1] + '">' +
								
                                '<button type="submit">Update</button>' +
                            '</form>' +
                           '<button class="deleteHr" data-hrid="' + userDetails[1] + '">Delete</button>' +
                        '</td>' +
                    '</tr>';
                    $('#hrTable tbody').append(row); 
                });
            },
            error: function() {
                alert('Error loading data');
            }
        });
    }

  
    loadHRData();

    $('#filterForm').on('submit', function(e) {
        e.preventDefault();
        var statusFilter = $('#statusFilter').val();
        loadHRData(statusFilter);
    });

   
    $(document).on('click', '.deleteHr', function() {
        var hrId = $(this).data('hrid');
        if (confirm('Are you sure you want to delete HR ID ' + hrId + '?')) {
            $.ajax({
                url: 'deleteHr',
                type: 'POST',
                data: {
                    hrId: hrId
                },
                success: function(response) {
                    if (response.status === 'success') {
                        alert('HR deleted successfully!');
                        loadHRData(); 
                    } else {
                        alert('Error: ' + response.message);
                    }
                },
                error: function() {
                    alert('An error occurred while deleting HR.');
                }
            });
        }
    });
});
</script>
<br><br><br>
</body>
</html>
