package homelibrary.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class NotificationServlet extends DSAServlet {

    private String createActionButton(String id, String type, String status, String buttonText) {
        String buttonClass;
        switch (status) {
            case "accepted":
                buttonClass = "accept-button";
                break;
            case "rejected":
                buttonClass = "reject-button";
                break;
            case "cancelled":
                buttonClass = "reject-button";
                break;
            default:
                buttonClass = "acknowledge-button";
        }

        String actionButton = """
                <a href="notification-management?id=%s?type=%s?status=%s" class="%s">%s</a>
                """.formatted(id, type, status, buttonClass, buttonText);
        return actionButton;
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String userId = getOwnerId(request);
        String userNickname = getNickname(request);
        String tableHtml = getNotificationsTable(userId, userNickname);

        try (PrintWriter out = response.getWriter()){
            out.println("""
                        <HTML>
                        <head>
                            <title>Notifications &middot; Home Library</title>
                            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                            <style>
                                .DefaultBox
                                {
                                    border: 2px solid black;
                                    margin: 20px;
                                    padding: 20px;
                                    width: 80%%;
                                }
                                table {
                                    width: 100%%;
                                    border: 1px solid;
                                    border-collapse: collapse;
                                }
                                th, td {
                                    padding: 8px;
                                    text-align: left;
                                }
                                th {
                                    background-color: #f2f2f2;
                                }
                                .action-buttons {
                                    display: flex;
                                    justify-content: space-around;
                                }
                                .accept-button, .reject-button, .acknowledge-button {
                                    padding: 5px 10px;
                                    border-radius: 5px;
                                    cursor: pointer;
                                }
                                .accept-button {
                                    background-color: #7cc27f;
                                    color: white;
                                }
                                .reject-button {
                                    background-color: #df6259;
                                    color: white;
                                }
                                .acknowledge-button {
                                    background-color: #3a9cbd;
                                    color: white;
                                }
                            </style>
                        </head>
                        <BODY>
                            <h1>Home Library &middot; Notifications</h1>
                            <div class="DefaultBox">
                            %s<BR/>
                            </div?>
                        </BODY>
                        </HTML>
                        """.formatted(tableHtml));
        }
    }

    private String getNotificationsTable(String userId, String userNickname) {
        StringBuilder tableHtml = new StringBuilder("");

        try
        {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);

            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 Statement statement = connection.createStatement()) {
                String query = """
                        SELECT
                            us.username AS "sender",
                            ur.username as "receiver",
                            t.record_type AS "type",
                            p.title,
                            t.start_time AS "date",
                            t.id,
                            t.request_status AS "status"
                        FROM
                            app.reservation_borrowing_requests t
                            INNER JOIN app.publications p on p.id = t.publication_id
                            INNER JOIN app.users ur on ur.id = t.receiver_id
                            INNER JOIN app.users us on us.id = t.sender_id
                        WHERE
                             t.request_status != 'acknowledged'::app.borrowing_record_status
                             AND
                             t.request_status != 'cancelled'::app.borrowing_record_status
                             AND
                             (ur.id = %s OR us.id = %s);
                        """.formatted(userId, userId);
                ResultSet results = statement.executeQuery(query);
                if (results.next())
                {
                    tableHtml.append("""
                                     <table border="1">
                                        <tr>
                                            <th>Sender</th>
                                            <th>Receiver</th>
                                            <th>Type</th>
                                            <th>Element (Publication)</th>
                                            <th>Date</th>
                                            <th>Status</th>
                                            <th>Action</th>
                                        </tr>
                                     """);
                    do
                    {
                        String sender = results.getString("sender");
                        String receiver = results.getString("receiver");
                        String type = results.getString("type");
                        String element = results.getString("title");
                        String date = results.getString("date");
                        String id =  results.getString("id");
                        String status = results.getString("status");

                        tableHtml.append("""
                                         <tr>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s Request</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                         """.formatted(sender, receiver, type, element, status.substring(0,1).toUpperCase() + status.substring(1), date));

//                        if (type.equals("reservation")) {
//                            if (sender.equals(userNickname)) {
//                                tableHtml.append("""
//                                                <td class="action-buttons">
//                                                    <a href="notification-management?id=%s?type=reservation?status=cancelled" class="reject-button">Cancel</a>
//                                                </td>
//                                                """.formatted(id));
//                            } else {
//                                tableHtml.append("""
//                                                <td class="action-buttons">
//                                                    <a href="notification-management?id=%s?type=borrowing?status=accepted" class="accept-button">Accept</a>
//                                                    <a href="notification-management?id=%s?type=reservation?status=rejected" class="reject-button">Reject</a>
//                                                </td>
//                                                """.formatted(id, id));
//                            }
//                        } else if (type.equals("borrowing")) {
//                            if (sender.equals(userNickname)) {
//                                tableHtml.append("""
//                                                <td class="action-buttons">
//                                                    <a href="notification-management?id=%s?status=cancelled" class="reject-button">Cancel</a>
//                                                </td>
//                                                """.formatted(id));
//                            } else {
//                                tableHtml.append("""
//                                                <td class="action-buttons">
//                                                    <a href="notification-management?id=%s?status=accepted" class="accept-button">Accept</a>
//                                                    <a href="notification-management?id=%s?status=rejected" class="reject-button">Reject</a>
//                                                </td>
//                                                """.formatted(id, id));
//                            }
//                        } else {
//                            tableHtml.append("""
//                                            <td class="action-buttons">
//                                                <a href="notification-management?id=%s?status=acknowledged" class="acknowledge-button">Acknowledge</a>
//                                            </td>
//                                            """.formatted(id));
//                        }

                        tableHtml.append("""
                                <td class="action-buttons">
                                """);

                        switch (status) {
                            case "pending":
                                if (sender.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "reservation", "cancelled", "Cancel"));
                                } else {
                                    tableHtml.append(createActionButton(id, "borrowing", "accepted", "Accept"));
                                    tableHtml.append(createActionButton(id, "reservation", "rejected", "Reject"));
                                }
                                break;
                            case "accepted":
                                if (sender.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "borrowing", "taken", "Confirm Borrowing"));
                                } else {
                                    tableHtml.append("<i>Marked as Accepted</i>");
                                }
                                break;
                            case "rejected":
                                tableHtml.append(createActionButton(id, "reservation", "acknowledged", "Acknowledge"));
                                break;
                            case "taken":
                                if (sender.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "borrowing", "returned", "Confirm Return"));
                                    tableHtml.append(createActionButton(id, "renewal", "pending", "Request Renewal"));
                                } else {
                                    tableHtml.append("<i>Marked as Taken</i>");
                                }
                                break;
                            case "returned":
                                if (sender.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "borrowing", "acknowledged", "Acknowledge"));
                                } else {
                                    tableHtml.append("<i>Marked as Returned</i>");
                                }
                                break;
                        }

                        tableHtml.append("""
                                </td>
                                </tr>
                                """);
                    }
                    while (results.next());
                    tableHtml.append("</table>");
                } else {
                    tableHtml.append("<p>You do not have any notifications yet.</p>");
                }
            }

        }  catch (SQLException sql) {
            tableHtml.append(sql);
        }

        return tableHtml.toString();
    }



}



