<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<style>
#formContainer{
	background-color:	 #b3c6ff;
	border:2px solid black;
	padding:10px;
	width:250px;
	box-shadow:5px 5px gray;
}
#errorDiv{
	color:red;
	padding:5px;
	border:2px solid red;
	background-color:#ffe6e6;
}
.formbtn{
	width:70%;
	height:20%;
	padding:5px;
	margin-bottom:10px;
	font-weight:bold;
	color:blue;
	box-shadow:5px 5px gray;
		border-radius:2px;
	border: 3px solid gray;
	
}
.formbtn:hover{
	color:white;
	background-color:blue;
	border-radius:2px;
	border: 3px solid white;
}
th{
	color:white;
	background-color:#3333ff;
	padding:8px;
	
}
td{
	color:black;
	padding:5px;
}
.submit-btn{
	color:blue;
	margin-top:10px;
	border:2px solid blue;
	font-size:16px;
	padding:5px;
	text-align:center;
	box-shadow:3px 3px solid gray;
}
.submit-btn:hover{
	color:white;
	background-color:blue;
	margin-top:10px;
	border:2px solid gray;
	font-size:16px;
	padding:5px;
	text-align:center;
}
</style>
<head>
    <title>Employee Forms</title>
    <script>
     
        async function fetchUserForms() {
            try {
				 clearErrorMessage();
                const response = await fetch("FetchEmpFormServlet");
                if (!response.ok) throw new Error("Failed to fetch forms");

                const formList = await response.json();
                const formContainer = document.getElementById("formContainer");
                formContainer.innerHTML = "";  

                formList.forEach(form => {
                    const formButton = document.createElement("button");
					formButton.className = "formbtn";
                    formButton.innerText = form.formName;
                    formButton.onclick = () => fetchFormData(form.formName); 
                    formContainer.appendChild(formButton);
                    formContainer.appendChild(document.createElement("br"));
                });
            } catch (error) {
                console.error("Error fetching user forms:", error);
                document.getElementById("formContainer").innerHTML = `<p>Error loading forms.</p>`;
            }
        }

    
        async function fetchFormData(formName) {
            try {
                const response = await fetch("FetchDynamicDBServlet?table_name=" + encodeURIComponent(formName));
                if (!response.ok) throw new Error("Failed to fetch form data");

                const data = await response.json();
                displayFormData(data, formName);
            } catch (error) {
                console.error("Error fetching form data:", error);
                document.getElementById("formDataContainer").innerHTML = `<p>Error loading form data.</p>`;
            }
        }

function displayFormData(response, formName) {
    const formDataTitle = document.getElementById("formtitle");
    formDataTitle.innerHTML = formName;

    const formDataContainer = document.getElementById("formDataContainer");
    formDataContainer.innerHTML = "";

    const data = response.data;
    const enumFields = response.enum || {};
    const fieldRows = response.field_rows || {}; 

    if (!data || typeof data !== "object") {
        formDataContainer.innerHTML = "<p>No data available to display.</p>";
        return;
    }

    const table = document.createElement("table");
    table.border = "1";

    const headerRow = document.createElement("tr");
    const colHeaders = Object.keys(data).filter(
        key => key !== "s_no" && key !== "user_id" && key !== "dept_id" && key !== "cur_id"  
    );

    colHeaders.forEach(key => {
        const th = document.createElement("th");
        th.textContent = key;
        headerRow.appendChild(th);
    });
    table.appendChild(headerRow);

    if (Object.keys(fieldRows).length > 0) {
        for (const record of Object.values(fieldRows)) {
            const recordRow = document.createElement("tr");

            colHeaders.forEach(columnName => {
                const td = document.createElement("td");
                td.textContent = record[columnName] || "";
                recordRow.appendChild(td);
            });

            table.appendChild(recordRow);
        }
    }

    // Add an empty row for new input
    const emptyRow = document.createElement("tr");
    colHeaders.forEach(columnName => {
        const td = document.createElement("td");

        if (enumFields[columnName]) {
            const select = document.createElement("select");
            select.className = "select_" + columnName; 
            enumFields[columnName].forEach(enumValue => {
                const option = document.createElement("option");
                option.value = enumValue;
                option.textContent = enumValue;
                select.appendChild(option);
            });
            td.appendChild(select);
        } else {
            const input = document.createElement("input");
            input.type = "text";
            input.className = "input_" + columnName; 
            td.appendChild(input);
        }

        emptyRow.appendChild(td);
    });
    table.appendChild(emptyRow);

    formDataContainer.appendChild(table);

    const submitButton = document.createElement("button");
    submitButton.innerText = "Submit Data";
	submitButton.className = "submit-btn";
    submitButton.onclick = () => submitFormData(data, formName, colHeaders);
    formDataContainer.appendChild(submitButton);
}



async function submitFormData(data, formName, colHeaders) {
    const formData = {};

    clearErrorMessage();

    colHeaders.forEach(function(columnName) {
        const inputField = document.querySelector('.input_' + columnName); 
        const selectField = document.querySelector('.select_' + columnName); 

        if (selectField) {
            formData[columnName] = selectField.value; 
        } else if (inputField) {
            formData[columnName] = inputField.value; 
        }
    });

    <% 
        Integer userId = (Integer) session.getAttribute("user_id");
        Integer deptId = (Integer) session.getAttribute("dept_id");
        if (userId != null && deptId != null) { 
    %>
        formData.user_id = <%= userId %>;
        formData.dept_id = <%= deptId %>;
    <% } %>

   

    console.log("Submitting Payload: ", formData);
  try {
        const response = await fetch("DynamicInsertServlet", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ table_name: formName, data: formData })
        });

        if (!response.ok) {
            
            const errorData = await response.json().catch(() => null); 
			console.log("errordata",errorData);
            const errorMessage = errorData?.message || `Server returned status ${response.status}`;
            displayErrorMessage(errorMessage);
            return;
        }

        const responseData = await response.json();
        console.log("Response:", responseData);

        if (responseData.status === "error") {
            displayErrorMessage(responseData.message || 'An error occurred while submitting the form.');
        } else {
            alert('Done !'+responseData.message);
            location.reload();
        }
    } catch (error) {
        console.error("Error submitting form data:", error);
 
        try {
            const errorResponse = await error.response.json();
            displayErrorMessage(errorResponse.message || 'An unexpected error occurred.');
        } catch (jsonError) {
            displayErrorMessage('An unexpected error occurred. Details: ' + error.message);
        }
    }
}
		
	function displayErrorMessage(message) {
    const errorDiv = document.getElementById('errorDiv');
    errorDiv.innerText = message;
    errorDiv.style.display = 'block';
	 
	}
	function clearErrorMessage() {
    const errorDiv = document.getElementById('errorDiv');
    errorDiv.style.display = 'none';
    errorDiv.innerText = '';

}
        window.onload = fetchUserForms;
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
    <h2>Employee Forms</h2><br>
    <div id="formContainer"></div> <br>
    <h3>Form Data:</h3><p id="formtitle"></p>
    <div id="formDataContainer"></div> <br>
	<h4 id="errorDiv"></h4>
	<br><br>
</body>
</html>
