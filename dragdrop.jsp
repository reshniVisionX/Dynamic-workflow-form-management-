<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Drag and Drop Form Builder</title>
    <style>
        body {
            display: flex;
        }
        .sidebar {
            width: 200px;
            margin-right: 20px;
            padding: 10px;
            border: 1px solid #ccc;
            height: 700px;
            overflow-y: auto;
        }
        .draggable {
            padding: 10px;
            margin: 5px;
            background-color: #f0f0f0;
            border: 1px solid #ccc;
            cursor: pointer;
            text-align: center;
        }
        .drop-zone {
            margin-top: 10px;
            margin-bottom: 10px;
            padding: 20px;
            border: 2px dashed #ccc;
            min-height: 50px;
            text-align: center;
        }
        .container {
            flex-grow: 1;
        }
    </style>
</head>
<body>
<%
 int cols = 5;
 String formName = "NewTable";
 %>
  let deptName = "<%= request.getParameter("dept_name") %>";
  let deptCount = <%= Integer.parseInt(request.getParameter("dept_count")) %>;
  console.log("Dept Count: ", deptCount);  

    <div class="sidebar">
        <h4>Available Inputs</h4>
        <div class="draggable" id="emailBox" data-type="email" draggable="true" ondragstart="drag(event)">Email</div>
        <div class="draggable" id="ageBox" data-type="age" draggable="true" ondragstart="drag(event)">Age</div>
        <div class="draggable" id="mobileBox" data-type="mobile" draggable="true" ondragstart="drag(event)">Mobile</div>
        <div class="draggable" id="urlBox" data-type="url" draggable="true" ondragstart="drag(event)">URL</div>
        <div class="draggable" id="ipBox" data-type="ip" draggable="true" ondragstart="drag(event)">IPAddress</div>
        <div class="draggable" id="timestamp" data-type="timestamp" draggable="true" ondragstart="drag(event)">CurrentTimestamp</div>
        <div class="draggable" id="date" data-type="date" draggable="true" ondragstart="drag(event)">Date</div>
        
        <div class="custom-input">
            <label for="customHeader">Custom Header:</label>
            <input type="text" id="customHeader" placeholder="Enter header">
            <label for="customType">Custom Type:</label>
            <select id="customType">
                <option value="" disabled selected>Select input type</option>
                <option value="text">Text</option>
                <option value="decimal">Decimal</option>
                <option value="boolean">Boolean</option>
                <option value="numeric">Numeric</option>
            </select>
            <button onclick="addCustomInput()">Add Custom Input</button>
        </div>
    </div>

    <div class="container">
        <h3>Drag an Input Box to the Appropriate Field</h3>
        <h3>Add Form Details for <%= formName %></h3>
        <% for (int i = 0; i < cols; i++) { %>
            <div class="drop-zone" ondrop="drop(event)" ondragover="allowDrop(event)">
                <input type="text" class="header-input" placeholder="Enter field header" data-type="">
            </div>
        <% } %>
        <button onclick="submitForm()">Submit Form</button>
    </div>

    <script>
        function allowDrop(ev) {
            ev.preventDefault();
        }

        function drag(ev) {
            ev.dataTransfer.setData("text", ev.target.id);
            ev.dataTransfer.setData("type", ev.target.getAttribute("data-type"));
        }

        function drop(ev) {
            ev.preventDefault();
            var data = ev.dataTransfer.getData("text");
            var type = ev.dataTransfer.getData("type");
            var draggedElement = document.getElementById(data);
            var headerInput = ev.target.querySelector('.header-input');
            
            headerInput.value = draggedElement.textContent; 
            headerInput.setAttribute("data-type", type); 
        }

        function addCustomInput() {
            var customHeader = document.getElementById("customHeader").value.trim();
            var customType = document.getElementById("customType").value;

            if (customHeader && customType) {
                var dropZones = document.querySelectorAll(".drop-zone");

                for (var i = 0; i < dropZones.length; i++) {
                    var headerInput = dropZones[i].querySelector('.header-input');
                    if (headerInput.value.trim() === "") {
                        headerInput.value = customHeader;
                        headerInput.setAttribute("data-type", customType);
                        break;
                    }
                }
                
                document.getElementById("customHeader").value = "";
                document.getElementById("customType").selectedIndex = 0;
            } else {
                alert("Please enter a custom header and select a type.");
            }
        }

        function submitForm() {
            var formData = {};
            formData.formName = "<%= formName %>";
            var headerInputs = document.querySelectorAll(".header-input");

            for (var i = 0; i < headerInputs.length; i++) {
                var header = headerInputs[i].value.trim();
                var type = headerInputs[i].getAttribute("data-type");
                
                if (header !== "" && type) {
                    formData[header] = type;
                }
            }

            console.log(formData); 

            var jsonFormData = JSON.stringify(formData);

            var form = document.createElement("form");
            form.method = "POST";
            form.action = "GenerateTblServlet"; 

            var hiddenInput = document.createElement("input");
            hiddenInput.type = "hidden";
            hiddenInput.name = "formData";
            hiddenInput.value = jsonFormData;

            form.appendChild(hiddenInput);

            document.body.appendChild(form);
            form.submit();
        }
    </script>
</body>
</html>
