
<!DOCTYPE html>
<html>
<head>
    <title>Workflow Designer</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
		.container{
			display:flex;
			flex-direction:row;
			
		}
        #sidebar {
            width: 20%;
            float: left;
            background: #f1f1f1;
            padding: 10px;
        }
        #workflowArea {
            width: 55%;
            float: right;
            min-height: 200px;
            border: 2px solid #ccc;
            padding: 10px;
			background-color:
			box-shadow:2px 2px gray;
			margin-bottom: 10px;
        }
        .draggable {
            margin: 10px;
            padding: 10px;
            background: #ddd;
            cursor: grab;
			box-shadow:4px 4px gray;
        }
        .workflow-item {
            position: relative;
            padding: 10px;
            margin: 5px;
            background: #e0e0e0;
            border: 1px solid #ccc;
        }
        .remove-btn {
            position: absolute;
            top: 5px;
            right: 5px;
            cursor: pointer;
            background: red;
            color: white;
            border: none;
            border-radius: 50%;
            width: 20px;
            height: 20px;
            text-align: center;
            line-height: 18px;
        }
       
        .error {
			
            color: red;
            font-weight: bold;
			padding: 10px;
            margin-top: 50px;
            border: 1px solid;
            border-radius: 5px;
	        display:none;
			background-color:#ffe6e6;
        }
		.success{
		    display:none;
			color:green;
			font-weight:bold;
			padding: 10px;
            margin-top: 50px;
            border: 1px solid;
            border-radius: 5px;
	       background-color:#e6ffe6;
		}
		.wrkbtn{
			color:white;
			background-color:blue;
			border:2px solid gray;
			padding:8px;
			font-weight:bold;
		}
		.wrkbtn:hover{
			color:blue;
			background-color:white;
			border:2px solid blue;
		}
		.draggable{
			border:2px solid black;
		}
    </style>
<%
    
    Integer userID = (Integer) session.getAttribute("user_ID");
    String workflowName = (String) session.getAttribute("form_name");
	  if (workflowName == null || workflowName.trim().isEmpty()) {
        workflowName = request.getParameter("wrkfl_name");
    }
    System.out.println("The form name fetched is :"+workflowName);
    if (userID != null) {
        System.out.println("User ID: " + userID);
    } else {
        System.out.println("User ID not found in the session.");
    }
	int id = (int)userID;
%>


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
    %><br>

    <script>
        let steps = [];
        const id = <%= id %>;
		const workflowName = "<%= workflowName %>";
        function allowDrop(event) {
            event.preventDefault();
        }

        function drag(event) {
            event.dataTransfer.setData("text", event.target.id);
        }

        function drop(event) {
            event.preventDefault();
            const data = event.dataTransfer.getData("text");

            const newDiv = document.createElement("div");
            newDiv.classList.add("workflow-item");

            let stepDetails = {
                state: "",
                approver_id: id,
                status: "pending",
                approved: "",
                rejected: "failed",
				
            };

            let stepCount = steps.filter(step => step.state.replace(/\d+$/, '') === data).length;
            let stepName = data + (stepCount > 0 ? stepCount + 1 : "");

            if (data === "employeeUpdate") {
                newDiv.innerText = "Employee Update";
                stepDetails.state = "EmployeeUpdate";
            } else if (data === "managerApproval" || data === "hrApproval") {
                const label = data === "managerApproval" ? "Manager Approval" : "HR Approval";
                const labelElement = document.createElement("strong");
                labelElement.innerText = label + (stepCount > 0 ? stepCount + 1 : "");
                newDiv.appendChild(labelElement);

                const optionsDiv = document.createElement("div");

                const autoApproveLabel = document.createElement("label");
                const autoApproveInput = document.createElement("input");
                autoApproveInput.type = "radio";
                autoApproveInput.name = stepName;
                autoApproveInput.classList.add("auto-approve");
                autoApproveLabel.appendChild(autoApproveInput);
                autoApproveLabel.appendChild(document.createTextNode(" Auto Approve"));

                const verificationLabel = document.createElement("label");
                const verificationInput = document.createElement("input");
                verificationInput.type = "radio";
                verificationInput.name = stepName;
                verificationInput.classList.add("verification");
                verificationLabel.appendChild(verificationInput);
                verificationLabel.appendChild(document.createTextNode(" Verification"));

             //   const timeLimitInput = document.createElement("input");
            //    timeLimitInput.type = "text";
			//	timeLimitInput.name = stepName;
            //    timeLimitInput.classList.add("time-limit");
             //   timeLimitInput.placeholder = "Time limit (e.g., 12 in hours..)";

                optionsDiv.appendChild(autoApproveLabel);
                optionsDiv.appendChild(verificationLabel);
                optionsDiv.appendChild(document.createElement("br"));
                //optionsDiv.appendChild(timeLimitInput);

                newDiv.appendChild(optionsDiv);

                stepDetails.state = stepName;
                stepDetails.approver_id = data === "managerApproval" ? 97 : 6;
            }

            const removeButton = document.createElement("button");
            removeButton.innerText = "X";
            removeButton.classList.add("remove-btn");
            removeButton.onclick = function () {
                newDiv.remove();
                steps = steps.filter(step => step.state !== stepDetails.state);
            };
            newDiv.appendChild(removeButton);

            document.getElementById("workflowArea").appendChild(newDiv);
            steps.push(stepDetails);
        }

        function submitWorkflow() {
            const errorDiv = document.getElementById("error-message");
            const errorMessage = document.getElementById("error-message");
               errorDiv.textContent = "";
               errorMessage.textContent = "";
            if (steps.length === 0 ) {
                errorMessage.innerText = "Workflow cant be empty.";
				 errorDiv.style.display = 'block';	
                 
                return;
            }
			if(steps[0].state !== "EmployeeUpdate"){
				 errorMessage.innerText = "Workflow must start with 'Employee Update'.";
				 errorDiv.style.display = 'block';	
                 
                return;
			}

            if (!workflowName) {
                errorMessage.innerText = "Workflow name is needed.";
                return;
            }

            steps.forEach((step, index) => {
                const autoApprove = document.querySelector('input.auto-approve[name="' + step.state + '"]');
                const verification = document.querySelector('input.verification[name="' + step.state + '"]');
                const timingInputs = document.querySelector('input.time-limit[name="' + step.state + '"]');
                step.approved = index + 1 < steps.length ? steps[index + 1].state : "success";

                if (autoApprove && autoApprove.checked) step.status = "success";
                if (verification && verification.checked) step.verification = "EmployeeUpdate";

               if (timingInputs && timingInputs.value !== "") {  
            step.duration = timingInputs.value;
            step.start_time = ""; 
            }
            });

            errorMessage.innerText = "";

            const workflow = { workflow: workflowName, steps };
            console.log(JSON.stringify(workflow));

    fetch("InsertWorkflowServlet", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(workflow),
    })
    .then(response => response.json()) 
    .then(data => {
      
        if (data.status === "success") {
           const successDiv = document.getElementById("success-message");  
			  successDiv.style.display = 'block';
			  successDiv.textContent = data.message;  
        } else {
          
             errorDiv.style.display = 'block';	
             errorDiv.textContent = data.message;  			 
        }
    })
    .catch(error => {
        console.error("Error submitting workflow:", error);
        document.getElementById("error-message").innerText = "Error submitting workflow."; 
    });

        }
    </script>
</head>
<body>
<div class="container">
    <div id="sidebar">
        <div id="employeeUpdate" class="draggable" draggable="true" ondragstart="drag(event)">Employee Update</div>
        <div id="managerApproval" class="draggable" draggable="true" ondragstart="drag(event)">Manager Approval</div>
        <div id="hrApproval" class="draggable" draggable="true" ondragstart="drag(event)">HR Approval</div>
    </div>
    <div id="workflowArea" ondrop="drop(event)" ondragover="allowDrop(event)"></div>
</div>
    <br>
	<div id="error-message" class="error"></div>
	<div id="success-message" class="success"></div>
	<br>
    <br>
    <button class="wrkbtn" onclick="submitWorkflow()">Submit Workflow</button><br>
    <br>
	
</body>
</html>
