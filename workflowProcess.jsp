<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Workflow Names</title>
    <style>
        #success-message, #error-message {
            display: none;
            margin-top: 10px;
            padding: 10px;
        }

        #success-message {
            color: green;
            background-color: #e6ffe6;
			border : 2px solid green;
        }

        #error-message {
            color: red;
            background-color: #ffe6e6;
			border : 2px solid red;
        }
		.wrkfl_names{
			width:150px;
			height:30px;
			border:2px solid black;
			padding:3px;
			display: flex;     
            flex-direction: column;
            gap: 20px;  
	        margin-top:15px;
			margin-left:50px;
			color:black;
			background-color:#e6f0ff;
			box-shadow:5px 5px gray;
			font-weight:bold;
		}
    </style>
    <script>
      
        async function fetchWorkflowNames() {
            try {
              
                const response = await fetch('/EmployeeManagement/fetchWrkflNamesServlet', {
                    method: 'GET'
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch workflow names.');
                }

                const workflowNames = await response.json();

                const listContainer = document.getElementById('workflowList');
                listContainer.innerHTML = ''; 

                workflowNames.forEach(name => {
                    const button = document.createElement('button');
                    button.textContent = name; 
                       button.className = "wrkfl_names";
					   button.onclick = function() {
                             const url = new URL(window.location.origin + '/EmployeeManagement/CustomWorkflow.jsp');
                             url.searchParams.set("wrkfl_name", name);
                             window.location.href = url.href;
                         };
					   
                    listContainer.appendChild(button); 
                });
            } catch (error) {
                console.error('Failed to fetch workflow names:', error);

                const errorDiv = document.getElementById("error-message");
                errorDiv.style.display = "block";
                errorDiv.textContent = "Error fetching workflow names. Please try again later.";
            }
        }

      
        window.onload = fetchWorkflowNames;
    </script>
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
    <h2>Click to edit workflows </h2>

    <div id="success-message"></div>
    <div id="error-message"></div>

    <div class="workflow_names" id="workflowList">
	<br/>
	
 </body>
 </html>
 
 