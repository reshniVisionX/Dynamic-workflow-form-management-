<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dynamic Form Builder</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f8f9fa;
            margin: 0;
            padding: 20px;
        }
        h2 {
            color: #0056b3;
        }
		.name{
			width:200px;
			height:30px;
			font-size:14px;
		}
        .main-form {
            width: 400px;
            padding: 20px;
            border: 1px solid #ccc;
            background-color: white;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .form-builder {
            display: flex;
            gap: 20px;
            margin-top: 20px;
			
        }
        .sidebar {
            width: 300px;
            background: white;
            padding: 15px;
            border-radius: 8px;
            border: 1px solid #ccc;
            height: fit-content;
        }
        .form-element {
            padding: 10px;
            margin: 5px 0;
            background: #f0f0f0;
            border: 1px solid #ddd;
            border-radius: 4px;
            cursor: move;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .form-element-type {
            color: #666;
            font-size: 0.9em;
        }
        .drop-area {
            flex-grow: 1;
            min-height: 100px;
            background: white;
            padding: 20px;
            border-radius: 8px;
            border: 3px dashed #15073C;
            background-color:#DEDEF8;
        }
        .dropped-element {
            position: relative;
            padding: 15px;
            margin: 10px 0;
            background: #f8f9fa;
            border: 1px solid #ddd;
            border-radius: 4px;
			  margin-left: auto;
        }
        .delete-btn {
            top: 10px;right:70px;
            background: #dc3545;
            color: white;
            border: none;
            border-radius: 20px;
            padding: 5px 10px;
            cursor: pointer;
			right:200px;float:right;
			width:30px;height:30px;
			
        }
        .field-name {
            width: calc(100% - 40px);
            margin-top: 10px;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .dragover {
            background: #e9ecef;
            border: 2px dashed #0056b3;
        }
        label {
            font-weight: bold;
            display: block;
            margin-top: 10px;
        }
        .butons {
            height:40px;
			width:200px;
            background-color: #152ed3;
			text-align:center;
            color: white;
            border: 2px solid  #25222F;
            border-radius: 4px;
            cursor: pointer;
            
			justify-content:center;
			position: absolute; left: 50%; transform: translate(-50%, -50%);
			margin-top:30px;
			margin-bottom:20px;
        }
        .butons:hover {
            background-color: #004494;
        }
		 .buttons {
            height:60px;
			width:250px;
            background-color: #152ed3;
			text-align:center;
            color: white;
            border: 2px solid  gray;
            border-radius: 2px;
            cursor: pointer;
			justify-content:center;
			box-shadow:2px 2px gray;
			
        }
        .butons:hover {
            background-color: #004494;
        }
		#errorMessage {
     padding: 10px;
     margin-top: 20px;
    border: 1px solid;
    border-radius: 5px;
    display: none; 
}

      
    </style>
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

    <h2>Form Builder</h2>
    <br>
    <form class="main-form">
       
        <input type="text" id="formName" class="name" name="form_name" placeholder="Enter form name" required>
        <label>Select Departments:</label>
		<div id="departmentContainer">
        </div>
        <label for="expiryDate">Form Expiry Date:</label>
        <input type="date" id="expiryDate" name="expiry_date">
    </form><br>

    <div class="form-builder">
        <div class="sidebar">
            <h3>Available Fields</h3>
           
        </div>

        <div class="drop-area" id="dropArea">
            <p style="text-align: center; color: #666;">Drag and drop form fields here</p>
        </div>
    </div>

    <button type="button" class="butons"  onclick="generateFormJSON()" >Generate Form</button>
  <br><br>
   <button type="button" id="button1" class="buttons" onclick="navigateToWorkflow()" disabled>Attach WorkFlow (disabled) </button>
 
<br><br>

<script>
 function navigateToWorkflow() {
     
        window.location.href = 'CustomWorkflow.jsp';
    }
document.addEventListener('DOMContentLoaded', function() {
    fetch('/EmployeeManagement/GetDepartmentsServlet')
    .then(response => response.json())
    .then(data => {
        const departmentContainer = document.getElementById('departmentContainer');
        data.departments.forEach(department => {
            const div = document.createElement('div');
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.name = 'dept_ids';
            checkbox.value = department.dept_id;
            const label = document.createElement('label');
            label.style.fontWeight = '100';
            label.textContent = department.dept_name;

            label.appendChild(checkbox);
            div.appendChild(label);
            departmentContainer.appendChild(div);
        });
        loadCustomtypes();
    })
    .catch(error => {
        console.error('Error fetching departments:', error);
        alert('Error loading departments');
    });

  function loadCustomtypes() {
    fetch('/EmployeeManagement/FetchDomainsServlet')
    .then(response => response.json())
    .then(data => {
        const sidebar = document.querySelector('.sidebar');
        const excludedFields = ["character_data","time_stamp","cardinal_number","sql_identifier","yes_or_no","daterange"]; 

        data.forEach(field => {
           
            if (excludedFields.includes(field.domain_name)) {
                return;
            }

            const fieldDiv = document.createElement('div');
            fieldDiv.classList.add('form-element');
            fieldDiv.draggable = true;
            fieldDiv.dataset.type = field.domain_name;
            
            if (field.enum_values && field.enum_values.length > 0) {
                fieldDiv.dataset.enumValues = JSON.stringify(field.enum_values);
            }

            const fieldLabel = document.createElement('span');
            fieldLabel.textContent = field.domain_name;

            const fieldType = document.createElement('span');
            fieldType.classList.add('form-element-type');
            fieldType.textContent = field.object_type;

            fieldDiv.appendChild(fieldLabel);
            fieldDiv.appendChild(fieldType);

            sidebar.appendChild(fieldDiv);

            fieldDiv.addEventListener('dragstart', handleDragStart);
            fieldDiv.addEventListener('dragend', handleDragEnd);
        });
    })
    .catch(error => {
        console.error('Error fetching fields:', error);
        alert('Error loading fields');
    });
}


    const dropArea = document.getElementById('dropArea');
    let draggedElement = null;

    dropArea.addEventListener('dragover', handleDragOver);
    dropArea.addEventListener('drop', handleDrop);
    dropArea.addEventListener('dragenter', handleDragEnter);
    dropArea.addEventListener('dragleave', handleDragLeave);

    function handleDragStart(e) {
        draggedElement = this;
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', this.dataset.type);
    }

    function handleDragEnd() {
        draggedElement = null;
    }

    function handleDragOver(e) {
        e.preventDefault();
        return false;
    }

    function handleDragEnter(e) {
        this.classList.add('dragover');
    }

    function handleDragLeave(e) {
        this.classList.remove('dragover');
    }

    function handleDrop(e) {
        e.preventDefault();
        this.classList.remove('dragover');
        const elementType = e.dataTransfer.getData('text/plain');
        createFormElement(elementType);
    }

   function createFormElement(type) {
        const wrapper = document.createElement('div');
        wrapper.className = 'dropped-element';
        wrapper.dataset.fieldType = type;

        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-btn';
        deleteBtn.innerHTML = ' X ';
        deleteBtn.onclick = function() {
            wrapper.remove();
        };

        const nameInput = document.createElement('input');
        nameInput.type = 'text';
        nameInput.className = 'field-name';
        nameInput.placeholder = 'Enter field name';
        nameInput.value = type;

        wrapper.appendChild(deleteBtn);
        wrapper.appendChild(nameInput);

   
        if (draggedElement && draggedElement.dataset.enumValues) {
            const enumValues = JSON.parse(draggedElement.dataset.enumValues);
            const enumContainer = document.createElement('div');
            enumContainer.className = 'enum-container';
            
            enumValues.forEach(enumValue => {
                const enumValueDiv = document.createElement('div');
                enumValueDiv.className = 'enum-value-container';
                
                const enumLabel = document.createElement('label');
                enumLabel.textContent = enumValue;
               
                enumValueDiv.appendChild(enumLabel);
                
                enumContainer.appendChild(enumValueDiv);
            });
            
            wrapper.appendChild(enumContainer);
        }

        dropArea.appendChild(wrapper);
    }
	
    function getDefaultFieldName(type) {
        return type || `field_${Date.now()}`;
    }
    
});
  function generateFormJSON() {
        const formName = document.getElementById('formName').value;
        const expiryDate = document.getElementById('expiryDate').value;
        const selectedDepts = Array.from(document.querySelectorAll('input[name="dept_ids"]:checked'))
            .map(cb => cb.value)
            .join(',');

        const formElements = document.querySelectorAll('.dropped-element');
        const fields = {};
        let duplicateError = false;

        formElements.forEach(element => {
            const fieldName = element.querySelector('.field-name').value.trim().toLowerCase();
            const fieldType = element.dataset.fieldType;

            if (!fieldName) {
                alert("Field name cannot be empty.");
                return;
            }

            if (fields.hasOwnProperty(fieldName)) {
                duplicateError = true;
                return;
            }
                fields[fieldName] = fieldType;
        });

        if (duplicateError) {
            alert("Enter unique column headers");
            return;
        }
		if (formName === "" || expiryDate === "" || selectedDepts === "" || Object.keys(fields).length === 0) {
           alert("FormBuilder fields can't be empty");
         return;
        }


        const formData = {
            form_name: formName,
            departments: selectedDepts,
            expiry_date: expiryDate,
            no_cols: Object.keys(fields).length,
            fields: fields
        };

        console.log('Form Data:', JSON.stringify(formData, null, 2));

        fetch('InsertCustomFormServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
   
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();  
})
.then(data => {
   
    document.getElementById("errorMessage").style.display = 'none';

    if (data.success) {
      
        displaySuccessMessage(data.message);
       // location.reload();
	    const button1 = document.getElementById("button1");
	    button1.disabled = false;  
		   button1.style.backgroundColor = "#28a745"; 

        button1.textContent = "Attach Workflow";
    } else {
     
        displayErrorMessage(data.message);
    }
})
.catch((error) => {
    console.error('Error:', error);
    displayErrorMessage('Error communicating with the server. Please try again later.');
});

  }
function displayErrorMessage(message) {
    const errorDiv = document.getElementById("errorMessage");
    errorDiv.textContent = message;  
    errorDiv.style.display = 'block'; 
	errorDiv.style.color = "red";
	errorDiv.style.backgroundColor = "#ffd6cc";
}
function displaySuccessMessage(message) {
    const errorDiv = document.getElementById("errorMessage");
    errorDiv.textContent = message;  
    errorDiv.style.display = 'block'; 
	errorDiv.style.color = "green";
	errorDiv.style.backgroundColor = "#e6ffe6";
}

</script>
<br><br>
<div id="errorMessage"></div>
<br>
<h2>Create custom types</h2><br
<%@ include file="customTypes.jsp" %>
<br><br>


</body>
</html>