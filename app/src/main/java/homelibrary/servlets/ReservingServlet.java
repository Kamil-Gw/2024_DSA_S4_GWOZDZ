package homelibrary.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
        String receiverId = request.getParameter("owner-id");
        String stringDateSince = request.getParameter("data-since");
        String stringDateUntil = request.getParameter("data-until");
        String error = "";
        try
        {
            LocalDate dateToday = LocalDate.now();
            LocalDate dateSince = LocalDate.parse(stringDateSince);
            LocalDate dateUntil = LocalDate.parse(stringDateUntil);
            if (dateToday.compareTo(dateSince) > 0)
            {
                error = "The beginning date does not belong to future.";
            }
            else if (dateSince.compareTo(dateUntil) >= 0)
            {
                error = "The ending date is not after the beginning date.";
            }
        }
        catch (Exception e)
        {
            error = e.toString();
        }
        
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
                        """.formatted();
        
        try (PrintWriter out = response.getWriter())
        {
            out.println("""
                        <HTML>
                        <HEAD>
                            <TITLE>Home Library &middot; Reserving</TITLE>
                        </HEAD>
                        <BODY>
                            <P></P>
                        </BODY>
                        </HTML>
                        """);
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
