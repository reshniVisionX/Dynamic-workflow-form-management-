<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Form Details</title>
<style>
.container{
	width:250px;
	background-color:#C8CAFA;
	border :2px solid #3E45F1;
	box-shadow:5px 5px 2px 2px gray;
}
.exportbtn{
	width:200px;
	height:30px;
    box-shadow:5px 5px gray;
	background-color:black;
	font-weight:bold;
	border-radius:4px;
	padding:5px;
	color:white;
}
.formbtn{
	width:80%;
	height:20%;
	padding:5px;
	margin-bottom:10px;
	font-weight:bold;
	color:blue;
	box-shadow:5px 5px gray;
	background-color:white;
}
.formbtn:hover{
	color:white;
	background-color:blue;
}
th{
	color:blue;
	padding:4px;
	background-color:#e6e6ff;
}
td{
	padding:5px;
	text-align:center;
}
</style>	
    <script>
     
        async function fetchUserForms() {
            try {
                const response = await fetch("FetchAllFormServlet");
                if (!response.ok) throw new Error("Failed to fetch forms");

                const formList = await response.json();
                const formContainer = document.getElementById("formContainer");
                formContainer.innerHTML = "";  

                formList.forEach(form => {
                    const formButton = document.createElement("button");
                    formButton.innerText = form;
					formButton.className="formbtn";
					 console.log("Form Name ",form);
                    formButton.onclick = () => fetchFormData(form); 
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
                const response = await fetch("FetchDBServlet?table_name=" + encodeURIComponent(formName));
                if (!response.ok) throw new Error("Failed to fetch form data");
                const data = await response.json();
                displayFormData(data, formName);
            } catch (error) {
                console.error("Error fetching form data:", error);
                document.getElementById("formDataContainer").innerHTML = `<p>Error loading form data.</p>`;
            }
        }

   function displayFormData(data, formName) {
    console.log("Data:", data);
    console.log("Form name:", formName);

    const formDataContainer = document.getElementById("formDataContainer");
    formDataContainer.innerHTML = ""; 
     
    const formTitle = document.createElement("h3");
    formTitle.textContent = formName;
	console.log("Appending formTitle:", formTitle.textContent);
    formDataContainer.appendChild(formTitle);

    if (data.length === 0) {
        const noDataMessage = document.createElement("p");
        noDataMessage.textContent = "No data available for this table.";
        formDataContainer.appendChild(noDataMessage);
        return;
    }

    const table = document.createElement("table");
    table.border = "1";
    table.style.width = "100%";

    const headerRow = document.createElement("tr");
    const colHeaders = Object.keys(data[0]);
    colHeaders.forEach(key => {
        const th = document.createElement("th");
        th.textContent = key;
        headerRow.appendChild(th);
    });
    table.appendChild(headerRow);

    data.forEach(row => {
        const tr = document.createElement("tr");
        colHeaders.forEach(key => {
            const td = document.createElement("td");
            td.textContent = row[key] != null ? row[key] : "N/A";
            tr.appendChild(td);
        });
        table.appendChild(tr);
    });

    formDataContainer.appendChild(table);
	
	const submitButton = document.createElement("button");
            submitButton.innerText = "Export Data";
			submitButton.className = "exportbtn";
            submitButton.onclick = () => ExportFormData( formName);
            formDataContainer.appendChild(submitButton);
}
function ExportFormData(formName) {
   
    if (!formName) {
        alert("Form name is required.");
        return;
    }

    console.log("Exporting data for form: ", formName);
    const form = document.createElement("form");
    form.method = "POST";  
    form.action = "ExportCSVServlet"; 
  
    const tableNameInput = document.createElement("input");
    tableNameInput.type = "hidden";
    tableNameInput.name = "table_name";
    tableNameInput.value = formName;  

    form.appendChild(tableNameInput);

    document.body.appendChild(form);  
    form.submit(); 
    document.body.removeChild(form);  
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
    <h2>Employee Forms</h2>
    <div class="container" id="formContainer"></div> 
	<br>
    <h3>Form Data:</h3>
    <div id="formDataContainer"></div> 
</body>
</html>
