package homelibrary.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;

@WebServlet(name = "NotificationManagementServlet", urlPatterns = {"/notification-management"})
public class NotificationManagementServlet extends DSAServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the query string from the request URL
        String queryString = request.getQueryString();

        // Split the query string by the question mark (?)
        String[] params = queryString.split("\\?");

        // Get the value of the "id" parameter
        String id = params[0].split("=")[1];

        // Get the value of the "status" parameter (without the "status=" prefix)
        String status = params[1].split("=")[1];

        // update notification status method
        updateNotificationStatus(id, status);

        // redirect to notification page
        response.sendRedirect("notifications");
    }


    private boolean updateNotificationStatus(String notificationId, String action) {
        String query = """
                    UPDATE app.reservation_borrowing_requests
                    SET request_status = '%s'::app.borrowing_record_status
                    WHERE id = %s;
                """.formatted(action, notificationId);

        System.out.println(query);

        try {
            executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void executeUpdate(String query) throws SQLException {
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate(query);
        }
    }
}
