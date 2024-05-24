package homelibrary.servlets;

import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 *
 * @author Kay Jay O'Nail
 */
public class ReservingServlet extends HttpServlet
{
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        assert (session != null);
        String senderId = (String) session.getAttribute("id");

        String publicationId = request.getParameter("publication-id");

        LocalDate dateSince = LocalDate.now();
        LocalDate dateUntil = dateSince.plusDays(14);

        String error = "";

        try
        {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);

            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 Statement statement = connection.createStatement())
            {
                String select = """
                        SELECT
                                p."owner_id" AS "owner"
                        FROM
                                app.publications p
                        WHERE
                                p."id" = %s
                        """.formatted(publicationId);

                ResultSet results = statement.executeQuery(select);
                if (results.next())
                {
                    String receiverId = String.valueOf(results.getInt("owner"));

                    String insert = """
                            INSERT INTO
                                    app.reservation_borrowing_requests
                                    (
                                        "sender_id",
                                        "receiver_id",
                                        "publication_id",
                                        "start_time",
                                        "end_time",
                                        "record_type",
                                        "request_status"
                                    )
                            VALUES
                                    (
                                        %1$s,
                                        %2$s,
                                        %3$s,
                                        '%4$s',
                                        '%5$s',
                                        'reservation',
                                        'pending'
                                    )
                            """.formatted(
                            senderId,
                            receiverId,
                            publicationId,
                            dateSince,
                            dateUntil
                    );
                    
                    statement.executeUpdate(insert);
                }
                else
                {
                    error = "Error at query execution.";
                }
            }
        }
        catch (SQLException sql)
        {
            error = sql.getMessage();
        }
        
        if (!error.isEmpty())
        {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/notifications");
            dispatcher.forward(request, response);
        }
        else
        {
            request.setAttribute("error-messages", error);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/home");
            dispatcher.forward(request, response);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}
