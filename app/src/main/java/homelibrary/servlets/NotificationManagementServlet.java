package homelibrary.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "NotificationManagementServlet", urlPatterns = {"/notification-management"})
public class NotificationManagementServlet extends DSAServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the query string from the request URL
        String queryString = request.getQueryString();

        // Split the query string by the question mark (?)
        String[] params = queryString.split("\\?");

        String id = null;
        String type = null;
        String status = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue[0].equals("id")) {
                id = keyValue[1];
            } else if (keyValue[0].equals("type")) {
                type = keyValue[1];
            } else if (keyValue[0].equals("status")) {
                status = keyValue[1];
            }
        }

//        System.out.println("id: " + id);
//        System.out.println("type: " + type);
//        System.out.println("status: " + status);

        updateNotificationStatus(id, type , status);

        // redirect to notification page
        response.sendRedirect("notifications");
    }


    private boolean updateNotificationStatus(String notificationId, String recordType, String requestStatus) {
        // TODO -> renewal request considerations
        String query = """
                    UPDATE app.reservation_borrowing_requests
                    SET record_type    = '%s'::app.record_type,
                        request_status = '%s'::app.borrowing_record_status
                    WHERE id = %s;
                """.formatted(recordType, requestStatus, notificationId);

        System.out.println(query);

        try {
            executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


}
