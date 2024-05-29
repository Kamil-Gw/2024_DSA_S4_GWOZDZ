package homelibrary.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Kay Jay O'Nail
 */
public class ChangingAccountSettingsServlet extends HttpServlet
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
        String newUsername = request.getParameter("new-username");
        String newPassword1 = request.getParameter("new-password-1");
        String newPassword2 = request.getParameter("new-password-2");
        String newEmail = request.getParameter("new-email");
        String newStatus = request.getParameter("new-status");
        String currentPassword = request.getParameter("current-password");

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            out.println("""
                        <HTML>
                        <BODY>
                            <P>%s</P>
                            <P>%s</P>
                            <P>%s</P>
                            <P>%s</P>
                            <P>%s</P>
                        </BODY>
                        </HTML>
                        """.formatted(
                    newUsername,
                    newPassword1,
                    newPassword2,
                    newEmail,
                    newStatus
            )
            );
        }
    }

    private void /* boolean? */ openConnection()
    {

    }

    private boolean verifyCurrentPassword(String userId, String password)
    {

    }

    private boolean verifyNewPassword(String password1, String password2)
    {

    }

    private boolean verifyNewUsername(String username)
    {

    }

    private boolean verifyEmail(String email)
    {

    }

    private void updateUsername(String newUsername)
    {
        
    }
    
    private void updatePassword(String newPassword)
    {
        
    }
    
    private void updateEmail(String newEmail)
    {
        
    }
    
    private void updateStatus(String newStatus)
    {
        
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
