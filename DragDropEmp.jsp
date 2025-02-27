<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Department Assignment</title>
    <style>
       
        #sidebar, #departmentFields {
            border: 1px solid #ccc;
            padding: 10px;
            margin: 10px;
            float: left;
            width: 45%;
            height: 450px;
            overflow-y: auto;
        }
        .draggable {
            cursor: pointer;
            margin: 5px 0;
            padding: 5px;
            border: 1px solid #000;
            background-color: #e7e7e7;
        }
        .dropzone {
            border: 1px dashed #aaa;
            min-height: 40px;
            margin: 5px 0;
            padding: 5px;
            background-color: #f9f9f9;
        }
		.removeButton{
			color:white;
			background-color:red;
			text-align:center;
			padding:5px;
			float:right;
		}
		.textbox{
			width:50px;
			height:25px;
			border:3px;
		}
    </style>
    <script>

    let deptName = "<%= request.getParameter("dept_name") %>";
    let deptCount = <%= Integer.parseInt(request.getParameter("dept_count")) %>;
    console.log("Dept Count: ", deptCount, "Name: ", deptName);

    function refreshSidebar() {
        fetch("FetchDeptPeopleServlet")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch updated user list");
                }
                return response.json();
            })
            .then(users => {
                const sidebar = document.getElementById("sidebar");

                if (!sidebar) {
                    console.error("Sidebar element not found.");
                    return;
                }

                sidebar.innerHTML = "<h3>Available Users to assign</h3>";

                displayUsers(users);
            })
            .catch(error => console.error("Error refreshing user list:", error));
    }

    function displayUsers(users) {
        const sidebar = document.getElementById("sidebar");

        if (!sidebar) {
            console.error("Sidebar element not found.");
            return;
        }

        users.forEach(user => {
            const userElement = document.createElement("div");
            userElement.className = "draggable";
            userElement.draggable = true;

            const userNameSpan = document.createElement("span");
            userNameSpan.textContent = user.user_name;

            const userIdSpan = document.createElement("span");
            userIdSpan.textContent = user.user_id;

            userElement.appendChild(userNameSpan);
            userElement.appendChild(document.createTextNode(" - ID: "));
            userElement.appendChild(userIdSpan);

            userElement.dataset.userId = user.user_id;
            userElement.dataset.userName = user.user_name;
            userElement.addEventListener("dragstart", event => {
                event.dataTransfer.setData("userId", userElement.dataset.userId);
                event.dataTransfer.setData("userName", userElement.dataset.userName);
            });

            sidebar.appendChild(userElement);
        });
    }

  function setupDropzones() {
    console.log("Dept count in setupDropzones: ", deptCount);
    const assignedUserIds = new Set();

    for (let i = 0; i < deptCount; i++) {
        const dropzone = document.createElement("div");
        dropzone.className = "dropzone";

        dropzone.addEventListener("dragover", event => event.preventDefault());

        dropzone.addEventListener("drop", event => {
            event.preventDefault();

            const userId = event.dataTransfer.getData("userId");
            const userName = event.dataTransfer.getData("userName");

            if (!userId || !userName) {
                console.error("Invalid drag data.");
                return;
            }
            if (assignedUserIds.has(userId)) {
                alert("This user has already been assigned to another position.");
                return;
            }
            if (dropzone.dataset.userId) {
                alert("This position is already filled no overlap.");
                return;
            }
            dropzone.dataset.userId = userId;
            dropzone.dataset.userName = userName;
			
            assignedUserIds.add(userId);

            const userSpan = document.createElement("span");
            userSpan.textContent = userName;
			userSpan.style.width="40px";
			userSpan.style.height="25px";
            dropzone.appendChild(userSpan);

            dropzone.appendChild(document.createElement("br"));
			
            const positionInput = document.createElement("input");
            positionInput.type = "text";
            positionInput.className = "positionInput";
            positionInput.placeholder = "Enter Position";
            dropzone.appendChild(positionInput);

            const removeButton = document.createElement("button");
            removeButton.textContent = "X";
            removeButton.className = "removeButton";

            removeButton.addEventListener("click", () => {
                dropzone.innerHTML = ""; 
                delete dropzone.dataset.userId; 
                delete dropzone.dataset.userName;

                assignedUserIds.delete(userId);
            });

            dropzone.appendChild(removeButton);

            console.log("The id removed is ", userId);
        });

        document.getElementById("departmentFields").appendChild(dropzone);
    }
}



    function submitData() {
        const assignedUsers = [];
        const dropzones = document.querySelectorAll(".dropzone");

        dropzones.forEach(dropzone => {
            const userId = dropzone.dataset.userId;
            const userName = dropzone.dataset.userName;
            const positionInput = dropzone.querySelector(".positionInput")?.value;

            if (userId && userName) {
                assignedUsers.push({
                    user_id: userId,
                    user_name: userName,
                    position: positionInput || "" 
                });
            }
        });

        const data = {
            dept_name: deptName,
            users: assignedUsers
        };

        fetch("DeptAssignmentServlet", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to submit department assignment");
                }
                return response.json();
            })
            .then(result => {
                alert("Department assignment submitted successfully");
                refreshSidebar(); 
				
            })
            .catch(error => console.error("Error submitting data:", error));
    }

    document.addEventListener("DOMContentLoaded", refreshSidebar);


</script>

</head>
</body>
<body onload="setupDropzones()">

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
    <h2>Assign Users to Department: <%= request.getParameter("dept_name") %></h2>
    <div id="sidebar">
        <h3>Available Users to assign</h3>
    
    </div>

    <div id="departmentFields">
        <h3>Enter Department Positions</h3>
    </div>

    <button onclick="submitData()">Submit Assignment</button>

</html>
