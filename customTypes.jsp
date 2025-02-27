<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Custom Types</title>
   <style>
    body {
        font-family: Arial, sans-serif;
        margin: 0;
        padding: 20px;
        background-color: #f4f4f4;
    }

    h2 {
        color: #333;
    }

    form {
        background-color: white;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 4px 4px 6px rgba(0, 0, 0, 0.1);
        max-width: 500px;
        
    }

    input[type="text"], input[type="date"], button {
        width: 100%;
        padding: 10px;
        margin: 10px 0;
        border: 1px solid #ccc;
        border-radius: 4px;
        font-size: 16px;
        box-sizing: border-box;
    }

    input[type="text"]:focus, input[type="date"]:focus, button:focus {
        outline: none;
        border-color: #4CAF50;
    }

    .custbutton {
        background-color: 	 #3333ff;
        color: white;
        cursor: pointer;
        border: none;
        transition: background-color 0.3s ease;
    }

    .custbutton:hover {
        background-color: #6666ff;
    }

    #enum-values-container {
        display: flex;
        align-items: center;
        margin-top: 10px;
    }

    #enum-values {
        display: flex;
        flex-direction: column;
        margin-right: 20px;
    }

    #enum-values-box {
        margin-top: 10px;
        padding: 10px;
        border: 1px solid #ddd;
        min-height: 100px;
		min-width:200px;
        background-color:		 #e6ecff;
		border-box:2px 4px gray;
    }

    .enum-item {
        padding: 5px;
        margin: 5px;
        background-color: 	 #3333ff;
        border-radius: 5px;
        cursor: pointer;
        display: inline-block;
    }

    .error {
        margin-top: 20px;
        padding: 15px;
        border-radius: 5px;
        font-size: 16px;
        display: none; 
    }

    .error.green {
        color: green;
        border: 1px solid green;
        background-color: #d4edda;
    }

    .error.red {
        color: red;
        border: 1px solid red;
        background-color: #f8d7da;
    }

    .error.blue {
        color: blue;
        border: 1px solid blue;
        background-color: #cce5ff;
    }
</style>

</head>
<body>
    <form id="customTypeForm">
        <h2>Regex Check</h2>
        <input type="text" name="domain" placeholder="Enter the custom type name (e.g., email)" required>
        <input type="text" name="check" placeholder="Enter the expression to validate" required>
        <button type="button" id="reloadParent" class="custbutton" onclick="submitRegex()">Submit Regex</button><br>

        <h2>ENUM Check</h2>
        <input type="text" name="enum_name" placeholder="Enter the Enum name (e.g., color)" required>
        <div id="enum-values-container">
            <div id="enum-values">
                <input type="text" id="enum-value-input" placeholder="Enter a value" />
                <button type="button" id="reloadParent" class="custbutton" onclick="addEnumValue()">Add Option</button>
            </div>
            <div id="enum-values-box"></div>
        </div>
        <button type="button" class="custbutton" onclick="submitEnum()">Submit Enum</button><br>

        <h2>DATE Range</h2>
        <input type="text" name="date_name" placeholder="Enter the date name" required>
        Start Date:
        <input type="date" name="startdate" required>
        End Date:
        <input type="date" name="enddate" required>
        <button type="button" id="reloadParent" class="custbutton" onclick="submitDateRange()">Submit Date Range</button><br>
		 </form>
		<br>
	<br>
	<div class="error" ></div>
   
	
	

  <script>
    let enumValues = [];

    function addEnumValue() {
        const enumValue = document.getElementById('enum-value-input').value.trim();
        if (enumValue) {
            enumValues.push(enumValue); // Add to the array
            updateEnumDisplay();
            document.getElementById('enum-value-input').value = ''; // Clear input
        } else {
            printError({ status: "error", message: "Please enter a valid enum value." });
        }
    }

    function updateEnumDisplay() {
        const enumContainer = document.getElementById('enum-values-box');
        enumContainer.innerHTML = ''; // Clear the container

        enumValues.forEach((value, index) => {
            const div = document.createElement('div');
            div.classList.add('enum-item');
            div.textContent = value; // Display the enum value
            div.onclick = function () { removeEnumValue(index); }; // Attach remove handler
            enumContainer.appendChild(div);
        });
    }

    function removeEnumValue(index) {
        enumValues.splice(index, 1); // Remove the value at the given index
        updateEnumDisplay(); // Refresh the display
    }

    function submitRegex() {
		  console.log("submitRegex function called");
        const domain = document.querySelector('[name="domain"]').value;
        const check = document.querySelector('[name="check"]').value;

        if (!domain || !check) {
            printError({ status: "error", message: "Please fill out both fields." });
            return;
        }

        const data = {
            type: "regex",
            domain: domain,
            details: {
                check: check
            }
        };

        sendRequest(data);
    }

    function submitEnum() {
        const enumName = document.querySelector('[name="enum_name"]').value;

        if (!enumName) {
            printError({ status: "error", message: "Please enter a name for the Enum." });
            return;
        }

        if (enumValues.length === 0) {
            printError({ status: "error", message: "Please add at least one enum value." });
            return;
        }

        const data = {
            type: "enum",
            domain: enumName,
            details: {
                enum_values: enumValues
            }
        };

        sendRequest(data);
    }

    function submitDateRange() {
        const startDate = document.querySelector('[name="startdate"]').value;
        const endDate = document.querySelector('[name="enddate"]').value;
        const name = document.querySelector('[name="date_name"]').value;

        if (!name || !startDate || !endDate) {
            printError({ status: "error", message: "Please fill out all date range fields." });
            return;
        }

        if (new Date(startDate) > new Date(endDate)) {
            printError({ status: "error", message: "Start date must be earlier than end date." });
            return;
        }

        const data = {
            type: "date_range",
            domain: name,
            details: {
                start_date: startDate,
                end_date: endDate
            }
        };

        sendRequest(data);
    }

    function sendRequest(data) {
        console.log("Sending data:", data);
        fetch('InsertDomainServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => response.json())
        .then(result => {
            printError(result); 
        })
        .catch(error => {
            printError({ status: "error", message: "Error: " + error }); 
        });
    }

   function printError(response) {
    const errorDiv = document.querySelector('.error');
    
    if (response && response.status === "success") {
        errorDiv.style.color = "green";
        errorDiv.innerText = response.message || "Success!";
    } else if (response && response.status === "error") {
        errorDiv.style.color = "red";
        errorDiv.innerText = response.message || "An error occurred!";
    } else {
        errorDiv.style.color = "blue";
        errorDiv.innerText = response.message || "Unknown response";
    }

    // Show the error div
    errorDiv.style.display = 'block';
}

</script>

</body>
</html>
