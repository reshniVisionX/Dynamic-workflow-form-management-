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
            border: 2px solid green;
        }

        #error-message {
            color: red;
            background-color: #ffe6e6;
            border: 2px solid red;
        }

        table {
            border-collapse: collapse;
            margin: 20px;
            width: 80%;
        }

        th, td {
            border: 1px solid black;
            padding: 8px;
            text-align: left;
        }

        th {
            background-color: #e6e6ff;
			color:blue;
        }

        h3 {
            margin-left: 20px;
        }

        .action-button {
            margin: 0 5px;
            padding: 5px 10px;
            cursor: pointer;
            border: none;
            border-radius: 5px;
            font-size: 14px;
        }

        .approve {
            background-color: green;
            color: white;
			border:1px solid white;
        }

        .reject {
            background-color: red;
            color: white;
			border:1px solid white;
        }

        .verify {
            background-color: blue;
            color: white;
			border:1px solid white;
        }
		.verify:hover{
			color:blue;
			background-color:white;
			border:1px solid blue;
		}
		.approve:hover {
            background-color: white;
            color: green;
			border:1px solid green;
        }

        .reject:hover {
            background-color: white;
            color: red;
			border:1px solid red;
        }
    </style>
    <script>
      
        async function fetchWorkflowData() {
            try {
                const response = await fetch('/EmployeeManagement/FetchWorkflowData', {
                    method: 'GET'
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch workflow data');
                }

                const workflowData = await response.json();
                displayWorkflowData(workflowData);
            } catch (error) {
                console.error('Error:', error);

                const errorDiv = document.getElementById('error-message');
                errorDiv.style.display = 'block';
                errorDiv.textContent = 'Error fetching workflow data.';
            }
        }

        
function triggerWorkflowAction(action, curId) {
   const url = new URL(window.location.origin + '/EmployeeManagement/WorkflowTriggerServlet');
    url.searchParams.set('action', action);
    url.searchParams.set('cur_id', curId);
                         
      fetch(url.href, { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            const successDiv = document.getElementById('success-message');
            const errorDiv = document.getElementById('error-message');

            successDiv.style.display = 'none';
            successDiv.textContent = '';
            errorDiv.style.display = 'none';
            errorDiv.textContent = '';

            if (data.status === 'success') {
                successDiv.style.display = 'block';
              successDiv.textContent = "Action '" + action + "' completed successfully!";
                fetchWorkflowData(); 
            } else {
                errorDiv.style.display = 'block';
                errorDiv.textContent = "Error performing action '"+action+"': "+data.message;
            }
        })
        .catch(error => {
            console.error('Error:', error);

            const errorDiv = document.getElementById('error-message');
            errorDiv.style.display = 'block';
            errorDiv.textContent = 'Error performing action. Please try again later.';
        });
}

        function displayWorkflowData(data) {
            const container = document.getElementById('workflowContainer');
            container.innerHTML = '';

            if (data.error) {
                const errorDiv = document.getElementById('error-message');
                errorDiv.style.display = 'block';
                errorDiv.textContent = data.error;
                return;
            }

            if (Array.isArray(data)) {
                data.forEach(tableData => {
                    const tableName = Object.keys(tableData)[0];
                    const records = tableData[tableName];
                  if (Array.isArray(records) && records.length > 0) {

                     const div = document.createElement('div');
                     div.innerHTML = tableName;
				   
                    const table = document.createElement('table');
                    const headerRow = document.createElement('tr');
                    let headersAdded = false;
					
                    if (records.length > 0) {
                        const firstRecord = records[0];
                        Object.keys(firstRecord).forEach(column => {
                            const th = document.createElement('th');
                            th.textContent = column;
                            headerRow.appendChild(th);
                        });

                        const actionTh = document.createElement('th');
                        actionTh.textContent = 'Actions';
                        headerRow.appendChild(actionTh);

                        table.appendChild(headerRow);
                        headersAdded = true;
                    }
                  
                    if (headersAdded) {
                        records.forEach(record => {
                            const row = document.createElement('tr');

                            Object.values(record).forEach(value => {
                                const td = document.createElement('td');
                                td.textContent = value;
                                row.appendChild(td);
                            });

                            const actionTd = document.createElement('td');

                            const approveButton = document.createElement('button');
                            approveButton.textContent = 'approve';
                            approveButton.className = 'action-button approve';
                            approveButton.onclick = () => triggerWorkflowAction('approved', record.cur_id);

                            const rejectButton = document.createElement('button');
                            rejectButton.textContent = 'reject';
                            rejectButton.className = 'action-button reject';
                            rejectButton.onclick = () => triggerWorkflowAction('rejected', record.cur_id);

                            const verifyButton = document.createElement('button');
                            verifyButton.textContent = 'Verify';
                            verifyButton.className = 'action-button verify';
                            verifyButton.onclick = () => triggerWorkflowAction('verification', record.cur_id);

                            actionTd.appendChild(approveButton);
                            actionTd.appendChild(rejectButton);
                            actionTd.appendChild(verifyButton);

                            row.appendChild(actionTd);
                            table.appendChild(row);
                        });
                    }

                    div.appendChild(table);
                    container.appendChild(div);
				  }else{
					 console.log("Skipped empty table");
				  }
                });
            } else {
                const errorDiv = document.getElementById('error-message');
                errorDiv.style.display = 'block';
                errorDiv.textContent = 'No data to display';
            }
        }

        window.onload = fetchWorkflowData;
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
    <h2>Action needed workflow tables </h2>
    <div id="success-message"></div>
    <div id="error-message"></div>
    <br>
    <div id="workflowContainer">
     
    </div>
</body>
</html>
