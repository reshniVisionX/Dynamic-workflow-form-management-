<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<html>
<head>
    <title>Edit HR</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
    <%
      
        String hrId = request.getParameter("hrId");

        session.setAttribute("user_id", hrId);
       

        out.println("<h2>Update Details for HR ID: " + hrId + "</h2>");
    %>

  <script>
   
    var user_id = "<%= session.getAttribute("user_id") %>";
	

    function loadHRData() {
        $.ajax({
            url: 'fetchData', 
            type: 'GET',
            data: { 
                role_id: 3, 
                user_id: user_id 
            },
            success: function(response) {
                if (response && response.length > 0) {
                    const user = response[0]; 
                    
                    document.getElementById('username').value = user[0]; 
                    document.getElementById('user_email').value = user[2]; 
                    document.getElementById('contact').value = user[4];
                    document.getElementById('dob').value = user[5]; 
                    document.getElementById('job_role').value = user[6]; 
                    document.getElementById('salary').value = user[7]; 
                    document.getElementById('address').value = user[8]; 
                    document.getElementById('status').value = user[10]; 
                }
            },
            error: function(error) {
                console.error('Error fetching HR data:', error);
            }
        });
    }

    $(document).ready(function() {
        loadHRData();
    });
</script>

    <form action="editHr" method="post">
       
        <input type="hidden" name="hrId" value="<%= hrId %>">
       
        <table>
            <tr>
                <td>Username</td>
                <td><input type="text" id="username" name="username" required></td>
            </tr>
            <tr>
                <td>User Email</td>
                <td><input type="email" id="user_email" name="user_email" required></td>
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
			
			<input type="hidden" name="user_id" value="<%= session.getAttribute("user_id") %>">

        </table>
        
        <button type="submit">Save Changes</button>
    </form>

</body>
</html>
