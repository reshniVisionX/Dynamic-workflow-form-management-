<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Notifications</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            position: relative;
        }
        .notification {
            position: absolute;
            top: 10px;
            left: 50%;
            transform: translateX(-50%);
            border: 3px solid #0000ff;
            padding: 10px;
            margin: 5px 0;
            color: #0000ff;
            display: flex;
            justify-content: space-between;
            width: 300px;
			
			background-color:#e6e6ff;
        }
        .notification button {
            background: transparent;
            border: none;
            font-size: 16px;
            cursor: pointer;
            color: #888;
        }
    </style>
</head>
<body>
 

        <div id="notificationList"></div>
    </div>

    <script>
        fetch("NotificationServlet", { method: 'GET' })
            .then(response => response.json())
            .then(data => {
                const notifications = data.notifications;
                const notificationList = document.getElementById("notificationList");
                 console.log("Notifications ",notifications);
				 console.log(notifications.length);
                if (notifications && notifications.length > 0) {
                    let notificationIndex = 0;
                     console.log("The notifications are : ",notifications);
                    function showNextNotification() {
                        if (notificationIndex < notifications.length) {
                            const message = notifications[notificationIndex];
                            const notificationDiv = document.createElement("div");
                            notificationDiv.classList.add("notification");

                            const messageContent = document.createElement("span");
                            messageContent.innerText = message;
                            notificationDiv.appendChild(messageContent);

                            const closeButton = document.createElement("button");
                            closeButton.innerText = "X";
                            notificationDiv.appendChild(closeButton);

                            notificationList.appendChild(notificationDiv);

                            closeButton.addEventListener("click", function() {
                                notificationDiv.remove(); 
                                
                                setTimeout(showNextNotification, 300); 
                            });

                            notificationIndex++;
                        }
                    }

                    showNextNotification();
                }
            })
            .catch(error => {
                console.error("Error fetching notifications:", error);
            });
    </script>

</body>
</html>
