<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Department Table</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
     
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            padding: 20px;
        }

      
        #addDepartmentBtn {
            background-color:	 #1a1aff;
            color: white;
            padding: 10px 20px;
            font-size: 16px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            margin-bottom: 20px;
            transition: background-color 0.3s;
        }

        #addDepartmentBtn:hover {
            background-color:  #ccccff;
        }

      
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

       
        #tableHeader th {
            background-color: #e6ecff;
            color:  #ffffff;
            padding: 10px;
            text-align: left;
        }

     
        #dynamicTable tbody tr:nth-child(even) {
            background-color: #f2f2f2;
        }

        #dynamicTable tbody tr:nth-child(odd) {
            background-color: #ffffff;
        }

        #dynamicTable td, #dynamicTable th {
            padding: 12px 15px; 
            text-align: left;
            border: 1px solid #ddd; 
        }

     
        #dynamicTable tbody tr:hover {
            background-color: #ddd;
        }

       
        #tableHeader th {
            text-align: center;
            font-size: 18px;
            color: #333;
        }
    </style>
    <script>
        $(document).ready(function () {
            var tableName = "department";
            loadTableData();

            function loadTableData() {
                $.ajax({
                    url: 'FetchDBServlet?table_name=' + tableName, 
                    method: 'GET', 
                    success: function (response) { 
                        console.log('Response received from servlet:', response); 

                        $('#tableHeader').empty();
                        $('#dynamicTable tbody').empty();

                        if (response.length > 0) { 
                           
                            var headers = Object.keys(response[0]);
                            console.log('Table headers:', headers);  

                            headers.forEach(function (header) {
                                $('#tableHeader').append('<th>' + header + '</th>');
                            });

                            response.forEach(function (row) {
                                var rowHtml = '<tr>';
                                headers.forEach(function (header) {
                                    var cellValue = row[header] || '';  

                                 
                                    if (header === 'forms') {
                                        if (Object.keys(cellValue).length === 0) {
                                            cellValue = 'No forms available';  
                                        } else {
                                           
                                            cellValue = JSON.stringify(cellValue); 
                                        }
                                    }

                                    rowHtml += '<td>' + cellValue + '</td>';  
                                });
                                rowHtml += '</tr>';
                                $('#dynamicTable tbody').append(rowHtml);  
                            });
                        } else {
                            $('#tableHeader').append('<th colspan="6">No Data Available</th>');  
                            console.log('No data available in the response');
                        }
                    },
                    error: function (xhr, status, error) {
                        console.log('AJAX request failed:', status, error);
   console.log('Server response:', xhr.responseText);						
                        alert("Failed to load data."); 
                    }
					

                });
            }
        });

        
        function openDepartmentForm() {
            window.location.href = 'departmentform.jsp'; 
        }
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

   
    <button id="addDepartmentBtn" onclick="openDepartmentForm()">Add Department</button>

   
    <table id="dynamicTable">
        <thead>
            <tr id="tableHeader"></tr>
        </thead>
        <tbody></tbody>
    </table>

</body>
</html>
