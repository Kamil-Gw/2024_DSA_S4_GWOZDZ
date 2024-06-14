package homelibrary.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.ResultSet;

/**
 * Servlet for executing the management of a notification.
 */
@WebServlet(name = "NotificationManagementServlet", urlPatterns = {"/notification-management"})
public class NotificationManagementServlet extends DSAServlet {

    /**
     * Default constructor.
     */
    public NotificationManagementServlet() {
        super();
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

        updateNotificationStatus(id, type, status);

        // redirect to notification page
        response.sendRedirect("notifications");
    }


    /**
     * Updates the status of a notification
     *
     * @param notificationId the ID of the notification
     * @param recordType     the type of the record
     * @param requestStatus  the status of the request
     * @return true if the status was updated successfully, false otherwise
     */
    private boolean updateNotificationStatus(String notificationId, String recordType, String requestStatus) {
        // renewal accepted -> update record_type to borrowing, request_status to taken + get end time and update (add 2 weeks)

        if (recordType.equals("renewal") && requestStatus.equals("accepted")) {
            // get date of end time
            String query1 = """
                        SELECT end_time
                        FROM app.reservation_borrowing_requests
                        WHERE id = %s;
                    """.formatted(notificationId);

            try {
                ResultSet rs = executeQuery(query1);
                rs.next();
                String endTime = rs.getString("end_time");
                System.out.println("End time: " + endTime);

                String query2 = """
                            UPDATE app.reservation_borrowing_requests
                            SET end_time = '%s'::timestamp + interval '2 weeks',
                                record_type = 'borrowing'::app.record_type,
                                request_status = 'taken'::app.borrowing_record_status
                            WHERE id = %s;
                        """.formatted(endTime, notificationId);
                System.out.println(query2);

                try {
                    executeUpdate(query2);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
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
        }

        return true;
    }


}
