<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.Gson" %>
<%@ include file="/WEB-INF/views/adminBar.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage HR</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<style>
.hrbtn{
	width:150px;
	height:30px;
	background-color:#4A45F9;
	border:2px solid grey;
	border-radius:4px;
	color:white;
	font-size:14px;
	font-weight:bold;
}
th,td{
	padding:5px;
}
thead{
	color:white;
	background-color:	 #1a1aff;
	font-size:14px;
}
</style>
<body>
<br>

<h2>HR Management</h2>
<br>
<form id="filterForm">
    <label for="statusFilter">Filter by Status:</label>
    <select name="statusFilter" id="statusFilter">
        <option value="">--Select--</option>
        <option value="active">Active</option>
        <option value="inactive">Inactive</option>
    </select>
    <button class="hrbtn" type="submit">Filter</button>
</form>

<br>
<% 

int userRoleId = 2; 
session.setAttribute("role_id", userRoleId); 
%>
<br>
<table border="1" id="hrTable">
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
<br>
<a href="Insert.jsp">
    <button class="hrbtn" type="submit">Add HR</button>
</a>

<script>
$(document).ready(function() {

    
    function loadHRData(statusFilter = '') {
        $.ajax({
            url: 'fetchData', 
            type: 'GET',
            data: { 
                statusFilter: statusFilter, 
                role_id: 2 
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

</body>
</html>
