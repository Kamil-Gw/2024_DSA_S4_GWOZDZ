package homelibrary.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class NotificationServlet extends DSAServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String userId = getOwnerId(request);
        String tableHtml = getNotificationsTable(userId);

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

    private String getOwnerId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("id") : null;
    }

    private String getNotificationsTable(String userId) {
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
                            t.id
                        FROM
                            app.reservation_borrowing_requests t
                            INNER JOIN app.publications p on p.id = t.publication_id
                            INNER JOIN app.users ur on ur.id = t.receiver_id
                            INNER JOIN app.users us on us.id = t.sender_id
                        WHERE
                            ur.id = %s OR us.id = %s;
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

//                        TODO -> links with notifications update
                        tableHtml.append("""
                                         <tr>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td class="action-buttons">
                                                <a class="accept-button">Accept&nbsp;</a>
                                                <a class="reject-button">Reject&nbsp;</a>
                                                <a class="acknowledge-button">Acknowledge </a>
                                            </td>
                                         </tr>
                                         """.formatted(sender, receiver, type, element, date));
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
