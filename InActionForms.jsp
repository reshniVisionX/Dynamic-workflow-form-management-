<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Workflows InAction</title>
    <style>
	

        #success-message, #error-message {
            display: none;
            margin-top: 5px;
            padding: 10px;
			position:absolute;
			width:800px;
			top:20%;
			left:50%;
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

       
		.update {
            background-color:  #47d147;
            color: white;
			border:1px solid white;
        }
		.update:hover {
            background-color:white ;
            color: #47d147;
			border:1px solid #47d147;
        }
       #workflowContainer{
		   margin-top:100px;
	   }
    </style>
    <script>
        async function fetchWorkflowData() {
            try {
                const response = await fetch('/EmployeeManagement/EmpProgressWkflServlet', {
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
        data.forEach((tableData) => {
            const tableName = Object.keys(tableData)[0];
            const records = tableData[tableName];
            const div = document.createElement('div');
            div.innerHTML = tableName;
            
            const table = document.createElement('table');
            const headerRow = document.createElement('tr');

            if (records.length > 0) {
                const firstRecord = records[0];
                Object.keys(firstRecord).forEach((column) => {
                    const th = document.createElement('th');
                    th.textContent = column;
                    headerRow.appendChild(th);
                });

                const updateTh = document.createElement('th');
                updateTh.textContent = 'Update';
                headerRow.appendChild(updateTh);
				
                table.appendChild(headerRow);

                records.forEach((record) => {
                    const row = document.createElement('tr');

                    Object.keys(record).forEach((column) => {
                        const td = document.createElement('td');
                        const nonEditableFields = ['s_no', 'user_id', 'dept_id', 'status', 'cur_id','current_state'];

                        if (nonEditableFields.includes(column)) {
                            td.textContent = record[column];
                        } else {
                            const input = document.createElement('input');
                            input.type = 'text';
                            input.value = record[column];
                            input.dataset.column = column; 
                            td.appendChild(input);
                        }
                        row.appendChild(td);
                    });

                    const updateTd = document.createElement('td');
                    const updateButton = document.createElement('button');
                    updateButton.textContent = 'Update';
                    updateButton.className = 'action-button update';

                    updateButton.onclick = () => triggerWorkflowUpdate(row, record.cur_id,record.status, tableName);
                    updateTd.appendChild(updateButton);
					
                    
                    row.appendChild(updateTd);
					
                    table.appendChild(row);
                });
            }

            div.appendChild(table);
            container.appendChild(div);
        });
    } else {
        const errorDiv = document.getElementById('error-message');
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'No data to display';
    }
}

async function triggerWorkflowUpdate(row, curId,status, tableName) {
    const updatedData = {};
    const inputs = row.querySelectorAll('input');
    inputs.forEach((input) => {
        updatedData[input.dataset.column] = input.value; 
    });

    try {
        const response = await fetch('/EmployeeManagement/UpdateFormServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                cur_id: curId,
                state: 'pending', 
                table_name: tableName,
				cur_status:status,
                updated_record: updatedData,
            }),
        });

        if (!response.ok) {
            throw new Error('Failed to update workflow');
        }

        const result = await response.json();
        if (result.success) {
            const successMessage = document.getElementById('success-message');
            successMessage.style.display = 'block';
            successMessage.textContent = result.message;
            fetchWorkflowData();
            setTimeout(() => {
                successMessage.style.display = 'none';
            }, 5000);
        } else {
            throw new Error(result.message || 'Unknown error');
        }
    } catch (error) {
        console.error('Error:', error);
        const errorDiv = document.getElementById('error-message');
        errorDiv.style.display = 'block';
        errorDiv.textContent = error.message;
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
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
    <h2>Your workflows in progress</h2>
    <div id="success-message"></div>
    <div id="error-message"></div>

    <div id="workflowContainer">
     
    </div>
</body>
</html>
