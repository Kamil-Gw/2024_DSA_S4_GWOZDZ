package homelibrary.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Servlet for managing notifications
 */
public class NotificationServlet extends DSAServlet {

    /**
     * Method to create an action button for the notification
     * @param id the id of the notification
     * @param type the type of the notification
     * @param status the status of the notification
     * @param buttonText the text to display on the button
     * @return the HTML for the action button
     */
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

    /**
     * Processes requests for both HTTP GET and POST methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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
                                            <th>Publication</th>
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
                                         """.formatted(sender, receiver, type, element, date, status.substring(0,1).toUpperCase() + status.substring(1)));
                        // TODO -> some date formatting would be nice

                        tableHtml.append("""
                                <td class="action-buttons">
                                """);

                        String combined = type + "-" + status;

                        switch (combined) {
                            case "reservation-pending":
                                if (sender.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "reservation", "cancelled", "Cancel"));
                                } else {
                                    tableHtml.append(createActionButton(id, "borrowing", "pending", "Accept"));
                                    tableHtml.append(createActionButton(id, "reservation", "rejected", "Reject"));
                                }
                                break;
                            case "reservation-rejected":
                                if (receiver.equals(userNickname)) {
                                    tableHtml.append("<i>Not acknowledged</i>");
                                } else {
                                    tableHtml.append(createActionButton(id, "reservation", "acknowledged", "Acknowledge"));
                                }
                                break;
                            case "reservation-accepted":
                                if (receiver.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "borrowing", "taken", "Confirm Borrow"));
                                } else {
                                    tableHtml.append("<i>Waiting for confirmation</i>");
                                }
                                break;
                            case "borrowing-pending":
                                if (receiver.equals(userNickname)) {
                                    tableHtml.append("<i>Waiting for receiver to borrow</i>");
                                } else {
                                    tableHtml.append(createActionButton(id, "borrowing", "cancelled", "Cancel"));
                                    tableHtml.append(createActionButton(id, "borrowing", "accepted", "Borrow"));
                                }
                                break;
                            case "borrowing-accepted":
                                if (receiver.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "borrowing", "taken", "Confirm Borrow"));
                                } else {
                                    tableHtml.append("<i>Waiting for confirmation</i>");
                                }
                                break;
                            case "borrowing-taken":
                                if (receiver.equals(userNickname)) {
                                    tableHtml.append("<i>Waiting for return</i>");
                                } else {
                                    tableHtml.append(createActionButton(id, "borrowing", "returned", "Return"));
                                    tableHtml.append(createActionButton(id, "renewal", "pending", "Renew"));
                                }
                                break;
                            case "borrowing-returned":
                                if (receiver.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "borrowing", "acknowledged", "Acknowledge"));
                                } else {
                                    tableHtml.append("<i>Not acknowledged</i>");
                                }
                                break;
                            case "renewal-pending":
                                if (receiver.equals(userNickname)) {
                                    tableHtml.append(createActionButton(id, "borrowing", "taken", "Reject"));
                                    tableHtml.append(createActionButton(id, "renewal", "accepted", "Confirm"));
                                } else {
                                    tableHtml.append(createActionButton(id, "borrowing", "taken", "Cancel"));
                                }
                                break;
                            default:
                                tableHtml.append("<i>Unknown action</i>");
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



